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

package io.frinx.cli.unit.iosxr.bfd.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.IfBfdExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConfigWriter implements CliWriter<Config> {

    private static final String BFD_ADD_CONFIG_TEMPLATE = """
            interface {$ifc_name}
             bfd mode ietf
             bfd address-family ipv4 fast-detect
            {% if ($bfd_multiplier) %} bfd address-family ipv4 multiplier {$bfd_multiplier}
            {% else %} no bfd address-family ipv4 multiplier
            {% endif %}{% if ($bfd_min_tx_interval) %} bfd address-family ipv4 minimum-interval {$bfd_min_tx_interval}
            {% else %} no bfd address-family ipv4 minimum-interval
            {% endif %}{% if ($bfd_remote_address) %} bfd address-family ipv4 destination {$bfd_remote_address.value}
            {% else %} no bfd address-family ipv4 destination
            {% endif %}root""";
    private static final String BFD_REMOVE_CONFIG_TEMPLATE = """
            interface {$ifc_name}
             no bfd mode ietf
             no bfd address-family ipv4 fast-detect
             no bfd address-family ipv4 multiplier
             no bfd address-family ipv4 minimum-interval
             no bfd address-family ipv4 destination
            root""";

    private final Cli cli;

    public ConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config, @NotNull WriteContext writeContext)
            throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getId();
        checkInterface(ifcName, writeContext);
        writeConfig(instanceIdentifier, config, ifcName);
    }

    void writeConfig(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config config,
                             String ifcName) throws WriteFailedException.CreateFailedException {
        IpAddress remoteAddress = config.getAugmentation(IfBfdExtAug.class).getRemoteAddress();
        Ipv4Address remoteIpv4Address = remoteAddress != null ? remoteAddress.getIpv4Address() : null;

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(BFD_ADD_CONFIG_TEMPLATE,
                        "ifc_name", ifcName,
                        "bfd_multiplier", config.getDetectionMultiplier(),
                        "bfd_min_tx_interval", config.getDesiredMinimumTxInterval(),
                        "bfd_remote_address", remoteIpv4Address));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
                                        @NotNull Config dataAfter, @NotNull WriteContext writeContext)
            throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config, @NotNull WriteContext writeContext)
            throws WriteFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getId();
        checkInterface(ifcName, writeContext);
        deleteConfig(instanceIdentifier, ifcName);
    }

    void deleteConfig(@NotNull InstanceIdentifier<Config> instanceIdentifier, String ifcName)
            throws WriteFailedException.DeleteFailedException {
        blockingDeleteAndRead(cli, instanceIdentifier,
                fT(BFD_REMOVE_CONFIG_TEMPLATE,
                        "ifc_name", ifcName));
    }

    private void checkInterface(@NotNull String ifcName, WriteContext writeContext) {
        Optional<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
                .interfaces.Interface> interfaceOptional = writeContext.readAfter(IIDs.INTERFACES
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
                        .interfaces.Interface.class, new InterfaceKey(ifcName)));
        Preconditions.checkArgument(interfaceOptional.isPresent(), "Cannot change BFD "
                + "configuration for non-existing bundle interface %s", ifcName);
        Preconditions.checkArgument(new AggregateConfigReader(cli).isLAGInterface(ifcName),
                "Cannot change BFD configuration for non-bundle interface %s", ifcName);
    }
}