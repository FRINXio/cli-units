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

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip6;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;



public class Ipv6ConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public Ipv6ConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface %s\n"
            + "%s\n"
            + "end";
    private static final Pattern LINK_LOCAL = Pattern.compile("[Ff][Ee][89AaBb].*");

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class)
                .getIndex();

        if (subId != SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }

        blockingWriteAndRead(cli, instanceIdentifier, config,
                f(WRITE_TEMPLATE,
                        getIfcName(instanceIdentifier),
                        createAddressCommand(config)));
    }

    private static String createAddressCommand(Config config) {
        String ip = config.getIp()
                .getValue();
        if (isLinkLocal(ip)) {
            return "ipv6 address " + ip + " link-local";
        } else {
            return "ipv6 address " + ip + "/" + config.getPrefixLength()
                    .toString();
        }
    }

    /*
     * Check if address belongs to subnet FE80::/10
     */
    private static boolean isLinkLocal(String ip) {
        return LINK_LOCAL.matcher(ip)
                .find();
    }


    private static String getIfcName(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class)
                .getName();
        Long subIfcIndex = instanceIdentifier.firstKeyOf(Subinterface.class)
                .getIndex();
        Preconditions.checkArgument(subIfcIndex == SubinterfaceReader.ZERO_SUBINTERFACE_ID,
                "Only subinterface " + SubinterfaceReader.ZERO_SUBINTERFACE_ID + " can have IP");
        return ifcName;
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config before,
                                        @Nonnull Config after,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            writeCurrentAttributes(instanceIdentifier, after, writeContext);
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(instanceIdentifier, before, after, e);
        }
    }

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "interface %s\n"
            + "no %s\n"
            + "end";

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class)
                .getIndex();

        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {

            try {
                blockingWriteAndRead(cli, instanceIdentifier, config,
                        f(DELETE_TEMPLATE,
                                getIfcName(instanceIdentifier),
                                createAddressCommand(config)));
            } catch (WriteFailedException.CreateFailedException e) {
                throw new WriteFailedException.DeleteFailedException(instanceIdentifier, e);
            }
        } else {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }
    }
}
