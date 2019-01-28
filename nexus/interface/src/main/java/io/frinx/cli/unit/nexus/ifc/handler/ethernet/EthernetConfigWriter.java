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

package io.frinx.cli.unit.nexus.ifc.handler.ethernet;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.nexus.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigWriter implements CliWriter<Config> {

    private static final String LACP_OPERATION_MODE_TEMPLATE = " mode"
            + "{% if ($lacp_aug.lacp_mode == ACTIVE) %} active"
            + "{% elseIf ($lacp_aug.lacp_mode == PASSIVE) %} passive"
            + "{% endif %}";

    private static final String IFC_ETHERNET_CONFIG_TEMPLATE = "interface {$ifc_name}\n"
            + "{% if ($aggregate_id) %}channel-group {$aggregate_id}"
            + "{% if ($lacp_aug.lacp_mode) %}"
            + LACP_OPERATION_MODE_TEMPLATE
            + "{% endif %}"
            + "\n"
            + "{% else %}no channel-group\n"
            + "{% endif %}"
            + "root";

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();

        checkIfcType(ifcName);

        configureLAG(ifcName, id, dataAfter);
    }

    private void configureLAG(String ifcName, InstanceIdentifier<Config> id, Config dataAfter)
            throws WriteFailedException.CreateFailedException {

        Config1 aggregationAug = dataAfter.getAugmentation(Config1.class);
        LacpEthConfigAug lacpAug = dataAfter.getAugmentation(LacpEthConfigAug.class);

        Long aggregateId = getAggregateId(aggregationAug);

        if (lacpAug != null && lacpAug.getLacpMode() != null) {
            Preconditions.checkArgument(aggregateId != null,
                    "Missing aggregate-id, cannot configure LACP mode on non LAG enabled interface %s", ifcName);
        }

        blockingWriteAndRead(cli, id, dataAfter,
                fT(IFC_ETHERNET_CONFIG_TEMPLATE,
                        "ifc_name", ifcName,
                        "lacp_aug", lacpAug,
                        "aggregate_id", aggregateId));
    }

    private static Long getAggregateId(final Config1 aggregationAug) {
        if (aggregationAug == null || aggregationAug.getAggregateId() == null) {
            return null;
        }

        String aggregateIfcName = aggregationAug.getAggregateId();

        return Long.valueOf(aggregateIfcName);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        Config1 aggregationAug = dataAfter.getAugmentation(Config1.class);
        LacpEthConfigAug lacpAug = dataAfter.getAugmentation(LacpEthConfigAug.class);

        if (aggregationAug == null && lacpAug == null) {
            // TODO Probably not needed after CCASP-172 is fixed
            deleteCurrentAttributes(id, dataBefore, writeContext);
        } else {
            writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class)
                .getName();

        checkIfcType(ifcName);

        blockingDeleteAndRead(cli, id,
                f("interface %s", ifcName),
                "no channel-group",
                "root");
    }

    private static void checkIfcType(String ifcName) {
        Preconditions.checkState(InterfaceConfigReader.parseType(ifcName) == EthernetCsmacd.class,
                "Cannot change ethernet configuration for non ethernet interface %s", ifcName);
    }

}
