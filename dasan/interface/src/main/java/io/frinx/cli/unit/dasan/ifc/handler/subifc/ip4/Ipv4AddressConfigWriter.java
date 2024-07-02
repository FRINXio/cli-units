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

package io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4AddressConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public Ipv4AddressConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @NotNull InstanceIdentifier<Config> instanceIdentifier,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        writeOrUpdateIpv4Address(instanceIdentifier, dataAfter, false);
    }

    @Override
    public void updateCurrentAttributes(
            @NotNull InstanceIdentifier<Config> instanceIdentifier,
            @NotNull Config dataBefore,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        writeOrUpdateIpv4Address(instanceIdentifier, dataAfter, true);
    }

    @Override
    public void deleteCurrentAttributes(
            @NotNull InstanceIdentifier<Config> instanceIdentifier,
            @NotNull Config dataBefore,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String vlanId = instanceIdentifier.firstKeyOf(Interface.class).getName().replace("Vlan", "br");
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        Preconditions.checkArgument(subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID,
                "Unable to manage IP for subinterface: %s", subId);

        blockingDeleteAndRead(cli, instanceIdentifier,
                "configure terminal",
                f("interface %s", vlanId),
                "no ip address",
                "end");
    }

    @VisibleForTesting
    void writeOrUpdateIpv4Address(
            InstanceIdentifier<Config> instanceIdentifier,
            Config config,
            boolean isPresent) throws WriteFailedException {

        String vlanId = instanceIdentifier.firstKeyOf(Interface.class).getName().replace("Vlan", "br");

        Preconditions.checkArgument(Ipv4AddressConfigReader.checkSubId(instanceIdentifier),
                "Unable to manage IP for subinterface: %s",
                instanceIdentifier.firstKeyOf(Subinterface.class).getIndex());

        // Dasan does not allow to update IP address,
        // so we need to delete the existing data and put new data.
        blockingWriteAndRead(cli, instanceIdentifier, config,
                "configure terminal",
                f("interface %s", vlanId),
                isPresent ? "no ip address" : "",
                f("ip address %s/%d", config.getIp().getValue(), config.getPrefixLength()),
                "end");
    }
}