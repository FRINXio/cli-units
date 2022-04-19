/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.ethernet;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfEthCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ETHERNETSPEED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class EthernetConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPLATE = "configure terminal\n"
            + "interface {$ifc_name}\n"
            + "{% if ($port_speed) %}{$port_speed}\n{% endif %}"
            + "{% if ($channel_id) %}channel-group {$channel_id} mode {$channel-group_id}\n"
            + "{% else %}{% if ($channel_before) %}no channel-group\n{% endif %}{% endif %}"
            + "{% if ($rate) %}lacp rate {$rate}\n{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "interface {$ifc_name}\n"
            + "{% if ($port_speed) %}no speed\n{% endif %}"
            + "{% if ($channel_before) %}no channel-group\n{% endif %}"
            + "{% if ($rate) %}no lacp rate\n{% endif %}"
            + "end";

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        final Long channelId = getChannelId(dataAfter);
        final String channelGroup = getChannelGroup(dataAfter, ifcName, channelId);
        final String rate = getLacpRate(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
                fT(WRITE_UPDATE_TEMPLATE,
                        "ifc_name", ifcName,
                        "port_speed", getSpeed(null, dataAfter),
                        "channel_id", channelId,
                        "channel-group_id", channelGroup,
                        "rate", rate));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        final Long channelId = getChannelId(dataAfter);
        final String channelGroup = getChannelGroup(dataAfter, ifcName, channelId);
        final String rate = getLacpRate(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
                fT(WRITE_UPDATE_TEMPLATE,
                        "ifc_name", ifcName,
                        "port_speed", getSpeed(dataBefore, dataAfter),
                        "channel_id", channelId,
                        "channel_before", dataBefore.getAugmentation(Config1.class),
                        "channel-group_id", channelGroup,
                        "rate", rate));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();

        blockingDeleteAndRead(cli, id,
                fT(DELETE_TEMPLATE,
                        "ifc_name", ifcName,
                        "channel_before", dataBefore.getAugmentation(Config1.class),
                        "rate", getLacpRate(dataBefore),
                        "port_speed", dataBefore.getPortSpeed()
                        ));
    }

    private String getSpeed(Config dataBefore, Config dataAfter) {
        final Class<? extends ETHERNETSPEED> speedBefore = dataBefore == null ? null : dataBefore.getPortSpeed();
        final Class<? extends ETHERNETSPEED> speedAfter = dataAfter == null ? null : dataAfter.getPortSpeed();

        if (!Objects.equals(speedBefore, speedAfter) && dataAfter != null) {
            final String speed = Util.getSpeedName(dataAfter.getPortSpeed());
            if (speed.isEmpty()) {
                return "speed auto";
            }
            return "speed " + speed;
        }

        return null;
    }

    private String getChannelGroup(Config dataAfter, String ifcName, Long channelId) {
        final LacpEthConfigAug lacpAug = dataAfter.getAugmentation(LacpEthConfigAug.class);

        if (lacpAug != null && lacpAug.getLacpMode() != null) {
            Preconditions.checkArgument(channelId != null,
                    "Missing aggregate-id, cannot configure LACP mode on non LAG enabled interface %s", ifcName);
        }

        final LacpActivityType lacpMode = lacpAug != null ? lacpAug.getLacpMode() : null;
        return lacpMode != null ? lacpMode.getName().toLowerCase() : "";
    }

    private static Long getChannelId(Config dataAfter) {
        final Config1 aggregationAug = dataAfter.getAugmentation(Config1.class);
        if (aggregationAug == null || aggregationAug.getAggregateId() == null) {
            return null;
        }
        final String aggregateIfcName = aggregationAug.getAggregateId();
        return Long.valueOf(aggregateIfcName);
    }

    private String getLacpRate(Config datatAfter) {
        final IfEthCiscoExtAug aug = datatAfter.getAugmentation(IfEthCiscoExtAug.class);
        if (aug == null || aug.getLacpRate() == null) {
            return null;
        }
        return aug.getLacpRate().getName().toLowerCase();
    }
}
