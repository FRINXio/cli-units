/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.DEFAULTINSTANCE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class DefaultSwitchedVlanConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_TRUNK_TEMPLATE = """
            configure terminal
            {% loop in $data as $vlan %}vlan {$vlan.vlan_id.value}
            {% if ($delete) %}no {% endif %}tagged {$ifc}
            {% endloop %}end""";

    private static final String WRITE_ACCESS_TEMPLATE = """
            configure terminal
            vlan {$vlanid.value}
            {% if ($delete) %}no {% endif %}untagged {$ifc}
            end""";

    private static final int DEFAULT_VLAN = 1;

    private final Cli cli;

    public DefaultSwitchedVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                 @NotNull Config config,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!getCheck().canProcess(instanceIdentifier, writeContext, false)) {
            return false;
        }

        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        Preconditions.checkArgument(EthernetCsmacd.class.equals(Util
                        .parseType(ifcName)),
                f("Interface '%s' must be of type Ethernet", ifcName));

        List<NetworkInstance> networkInstances = writeContext.readAfter(IIDs.NETWORKINSTANCES)
                .get()
                .getNetworkInstance();

        long exists = networkInstances.stream()
                .filter(i -> DEFAULTINSTANCE.class.equals(i.getConfig().getType()))
                .flatMap(i -> i.getInterfaces().getInterface().stream())
                .filter(i -> i.getId().equals(ifcName))
                .count();
        Preconditions.checkArgument(exists > 0, f("Interface '%s' must be part of DEFAULT network instance", ifcName));

        Set<VlanId> collect = networkInstances.stream()
                .filter(i -> DEFAULTINSTANCE.class.equals(i.getConfig().getType()))
                .flatMap(i -> i.getVlans().getVlan().stream())
                .map(Vlan::getVlanId)
                .collect(Collectors.toSet());
        Preconditions.checkArgument(isAllVlanIdsInDefaultNI(config, collect),
                f("All vlans used in interface '%s' must be part of DEFAULT network instance", ifcName));

        blockingWriteAndRead(getCommand(config, ifcName, false), cli, instanceIdentifier, config);

        return true;
    }

    @VisibleForTesting
    String getCommand(@NotNull Config config, String anInterface, boolean delete) {
        String command = "";
        if (VlanModeType.TRUNK.equals(config.getInterfaceMode())) {
            command = fT(WRITE_TRUNK_TEMPLATE, "data", io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def.Vlan
                            .parseVlanRanges(config.getTrunkVlans()),
                    "ifc", anInterface,
                    "delete", delete ? Chunk.TRUE : null);

            if (config.getNativeVlan() != null) {
                command += "\n"
                        + fT(WRITE_ACCESS_TEMPLATE, "vlanid", config.getNativeVlan(),
                        "ifc", anInterface,
                        "delete", delete ? Chunk.TRUE : null);
            }
        } else if (config.getAccessVlan().getValue() != 1) {
            command = fT(WRITE_ACCESS_TEMPLATE, "vlanid", config.getAccessVlan(),
                    "ifc", anInterface,
                    "delete", delete ? Chunk.TRUE : null);
        }
        return command;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> id,
                                                  @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        boolean handled = deleteCurrentAttributesWResult(id, dataBefore, writeContext);
        if (!handled) {
            return false;
        }
        return writeCurrentAttributesWResult(id, dataAfter, writeContext);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                                  @NotNull Config config,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {

        if (!getCheck().canProcess(instanceIdentifier, writeContext, true)) {
            return false;
        }

        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(getCommand(config, ifcName, true), cli, instanceIdentifier);

        return true;
    }

    private static boolean isAllVlanIdsInDefaultNI(Config config, Set<VlanId> collect) {
        boolean result = false;
        if (VlanModeType.ACCESS.equals(config.getInterfaceMode()) && config.getAccessVlan() != null) {
            result = collect.contains(config.getAccessVlan());
        } else if (VlanModeType.TRUNK.equals(config.getInterfaceMode())) {
            if (config.getNativeVlan() != null) {
                result = collect.contains(config.getNativeVlan());
            }
            if (config.getTrunkVlans() != null) {
                long count = config.getTrunkVlans().stream()
                        .map(VlanSwitchedConfig.TrunkVlans::getVlanId)
                        .filter(collect::contains)
                        .count();
                result = count == config.getTrunkVlans().size();
            }
        }
        return result;
    }

    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}