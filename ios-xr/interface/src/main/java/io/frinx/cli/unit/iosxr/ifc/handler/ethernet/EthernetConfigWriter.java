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

package io.frinx.cli.unit.iosxr.ifc.handler.ethernet;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EthernetConfigWriter implements CliWriter<Config> {

    private static final String CONFIGURE_LAG = "interface {$ifcName}\n" +
            "bundle id {$bundleId} {% if ($lacpAug) %}{% if ($lacpAug.lacp_mode == $lacpAct) %}mode active\n{% else %}mode passive\n{%endif%}" +
            "{% if ($lacpAug.interval == $periodType) %}lacp period short\n{% else %}no lacp period short\n{%endif%}{% else %}mode on\n\n{%endif%}" +
            "exit";
    private static final String DELETE_CURR_ATTR = "interface {$ifcName}\n" +
            "no bundle id\n" +
            "no lacp period short\n" +
            "exit";

    private static final String BUNDLE_ID_COMMAND_TEMPLATE = "bundle id %s";

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        checkIfcType(ifcName);

        configureLAG(ifcName, id, dataAfter);
    }

    private void configureLAG(String ifcName, InstanceIdentifier<Config> id, Config dataAfter)
            throws WriteFailedException.CreateFailedException {

        Config1 aggregationAug = dataAfter.getAugmentation(Config1.class);
        LacpEthConfigAug lacpAug = dataAfter.getAugmentation(LacpEthConfigAug.class);

        // TODO Exctract this to commands templates
        if (aggregationAug == null || aggregationAug.getAggregateId() == null) {
            Preconditions.checkArgument(lacpAug == null,
                    "Cannot configure lacp on non LAG enabled interface %s", ifcName);
            return;
        }

        int bundleId = Integer.parseInt(getBundleId(aggregationAug.getAggregateId()));

        // TODO we should probably check if the logical aggregate interface
        // exists
        blockingWriteAndRead(cli, id, dataAfter, fT(CONFIGURE_LAG,
                "ifcName", ifcName,
                "bundleId", bundleId,
                "lacpAug", lacpAug,
                "lacpAct", LacpActivityType.ACTIVE,
                "periodType", LacpPeriodType.FAST));
    }

    private static Pattern AGGREGATE_IFC_NAME = Pattern.compile("Bundle-Ether(?<id>\\d+)");

    // TODO This should return long
    private static String getBundleId(String aggregateIfcName) {
        Matcher aggregateIfcNameMatcher = AGGREGATE_IFC_NAME.matcher(aggregateIfcName.trim());

        Preconditions.checkArgument(aggregateIfcNameMatcher.matches(),
                "aggregate-id %s should reference LAG interface", aggregateIfcName);

        return aggregateIfcNameMatcher.group("id");
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

        String ifcName = id.firstKeyOf(Interface.class).getName();

        checkIfcType(ifcName);

        blockingDeleteAndRead(cli, id, fT(DELETE_CURR_ATTR,
                "ifcName", ifcName));
    }

    private static void checkIfcType(String ifcName) {
        Preconditions.checkState(InterfaceConfigReader.parseType(ifcName) == EthernetCsmacd.class,
                "Cannot change ethernet configuration for non ethernet interface %s", ifcName);
    }
}
