/*
 * Copyright © 2018 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.unit.brocade.ifc.handler.subifc.ip4;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class Ipv4ConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public Ipv4ConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String MOD_TEMPLATE = "configure terminal\n" +
            "interface {$ifcName}\n" +
            "{% if ($delete) %}no {%endif %}ip address {$ip.ip.value} {$info.netmask}\n" +
            "end";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId != SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }

        SubnetUtils.SubnetInfo info = getSubnetInfo(config);

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(MOD_TEMPLATE,
                        "ifcName", getIfcName(instanceIdentifier),
                        "config", config,
                        "info", info));
    }

    private static SubnetUtils.SubnetInfo getSubnetInfo(@Nonnull Config config) {
        return new SubnetUtils(config.getIp().getValue() + "/" + config.getPrefixLength()).getInfo();
    }

    private static String getIfcName(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subIfcIndex = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();
        Preconditions.checkArgument(subIfcIndex == SubinterfaceReader.ZERO_SUBINTERFACE_ID, "Only subinterface " + SubinterfaceReader.ZERO_SUBINTERFACE_ID + "  can have IP");
        return ifcName;
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config before,
                                        @Nonnull Config after,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            deleteCurrentAttributes(instanceIdentifier, before, writeContext);
            writeCurrentAttributes(instanceIdentifier, after, writeContext);
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(instanceIdentifier, before, after, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            SubnetUtils.SubnetInfo info = getSubnetInfo(config);

            try {
                blockingWriteAndRead(cli, instanceIdentifier, config,
                        f(MOD_TEMPLATE,
                                "ifcName", getIfcName(instanceIdentifier),
                                "ip", config,
                                "netMask", info));
            } catch (WriteFailedException.CreateFailedException e) {
                throw new WriteFailedException.DeleteFailedException(instanceIdentifier, e);
            }
        } else {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }
    }
}
