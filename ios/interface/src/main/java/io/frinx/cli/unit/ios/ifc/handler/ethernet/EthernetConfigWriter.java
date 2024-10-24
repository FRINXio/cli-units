/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.ifc.handler.ethernet;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ETHERNETSPEED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigWriter implements CliWriter<Config> {

    private static final String IFC_ETHERNET_CONFIG_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            {% if ($port_speed) %}{$port_speed}
            {% endif %}{% if ($channel_id) %}channel-group {$channel_id} mode {$channel-group_id}
            {% else %}no channel-group
            {% endif %}end""";

    private static final String IFC_ETHERNET_CONFIG_DELETE_TEMPLATE = """
            configure terminal
            interface {$ifc_name}
            no speed
            no channel-group
            end""";

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = Objects.requireNonNull(id.firstKeyOf(Interface.class)).getName();
        Long channelId = getChannelId(dataAfter);
        String channelGroup = getChannelGroup(dataAfter, ifcName, channelId);

        blockingWriteAndRead(cli, id, dataAfter,
                fT(IFC_ETHERNET_CONFIG_TEMPLATE,
                        "ifc_name", ifcName,
                        "port_speed", getSpeed(null, dataAfter),
                        "channel_id", channelId,
                        "channel-group_id", channelGroup));
    }

    private String getSpeed(Config dataBefore, Config dataAfter) {
        Class<? extends ETHERNETSPEED> speedBefore = dataBefore == null ? null : dataBefore.getPortSpeed();
        Class<? extends ETHERNETSPEED> speedAfter = dataAfter == null ? null : dataAfter.getPortSpeed();
        if (!Objects.equals(speedBefore, speedAfter) && dataAfter != null) {
            String speed = Util.getSpeedName(dataAfter.getPortSpeed());
            if (speed == null) {
                return "no speed";
            }
            return "speed " + speed;
        }
        return null;
    }

    private String getChannelGroup(Config dataAfter, String ifcName, Long channelId) {
        LacpEthConfigAug lacpAug = dataAfter.getAugmentation(LacpEthConfigAug.class);

        if (lacpAug != null && lacpAug.getLacpMode() != null) {
            Preconditions.checkArgument(channelId != null,
                    "Missing aggregate-id, cannot configure LACP mode on non LAG enabled interface %s", ifcName);
        }

        LacpActivityType lacpMode = lacpAug != null ? lacpAug.getLacpMode() : null;
        return lacpMode != null ? lacpMode.getName().toLowerCase(Locale.ROOT) : "";
    }

    private static Long getChannelId(Config dataAfter) {
        Config1 aggregationAug = dataAfter.getAugmentation(Config1.class);
        if (aggregationAug == null || aggregationAug.getAggregateId() == null) {
            return null;
        }
        String aggregateIfcName = aggregationAug.getAggregateId();
        return Long.valueOf(aggregateIfcName);
    }

    @Override
    public void updateCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                        @NotNull final Config dataBefore,
                                        @NotNull final Config dataAfter,
                                        @NotNull final WriteContext writeContext) throws WriteFailedException {
        final String ifcName = Objects.requireNonNull(id.firstKeyOf(Interface.class)).getName();
        Long channelId = getChannelId(dataAfter);
        String channelGroup = getChannelGroup(dataAfter, ifcName, channelId);

        blockingWriteAndRead(cli, id, dataAfter,
                fT(IFC_ETHERNET_CONFIG_TEMPLATE,
                        "ifc_name", ifcName,
                        "port_speed", getSpeed(dataBefore, dataAfter),
                        "channel_id", channelId,
                        "channel-group_id", channelGroup));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = Objects.requireNonNull(id.firstKeyOf(Interface.class)).getName();
        blockingDeleteAndRead(cli, id,
                fT(IFC_ETHERNET_CONFIG_DELETE_TEMPLATE,
                        "ifc_name", ifcName));
    }
}