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

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate.bfd;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BfdConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public BfdConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();

        checkIfcType(ifcName);

        IpAddress destinationAddress = dataAfter.getDestinationAddress();
        Ipv4Address destinationIpv4 = destinationAddress != null ? destinationAddress.getIpv4Address() : null;

        blockingWriteAndRead(cli, id, dataAfter,
                f("interface %s", ifcName),
                "bfd mode ietf",
                "bfd address-family ipv4 fast-detect",
                dataAfter.getMultiplier() != null ? f("bfd address-family ipv4 multiplier %s", dataAfter
                        .getMultiplier()) : "no bfd address-family ipv4 multiplier",
                dataAfter.getMinInterval() != null ? f("bfd address-family ipv4 minimum-interval %s", dataAfter
                        .getMinInterval()) : "no bfd address-family ipv4 minimum-interval",
                destinationIpv4 != null ? f("bfd address-family ipv4 destination %s", destinationIpv4.getValue()) :
                        "no bfd address-family ipv4 destination",
                "root");
    }

    private void checkIfcType(String ifcName) {
        Preconditions.checkArgument(new AggregateConfigReader(cli).isLAGInterface(ifcName),
                "Cannot configure bfd on non-LAG interface %s", ifcName);
    }


    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();

        checkIfcType(ifcName);

        blockingDeleteAndRead(cli, id,
                f("interface %s", ifcName),
                "no bfd mode ietf",
                "no bfd address-family ipv4 fast-detect",
                "no bfd address-family ipv4 multiplier",
                "no bfd address-family ipv4 minimum-interval",
                "no bfd address-family ipv4 destination",
                "root");
    }
}
