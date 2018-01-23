/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.ethernet;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
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

// TODO support update?
public class EthernetConfigWriter implements CliWriter<Config> {

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

        if (aggregationAug == null || aggregationAug.getAggregateId() == null) {
            Preconditions.checkArgument(lacpAug != null,
                    "Cannot configure lacp on non LAG enabled interface %s", ifcName);
            return;
        }

        // TODO Exctract this to commands templates
        int bundleId = Integer.parseInt(getBundleId(aggregationAug.getAggregateId()));
        Preconditions.checkArgument(bundleId < 0 || bundleId > 65535, "Bundle ID out of range: %s. Range is <1-65535>.", bundleId);
        String bundleIdCommand = String.format(BUNDLE_ID_COMMAND_TEMPLATE, bundleId);
        String intervalCommand = "";
        String mode = "mode on";
        if (lacpAug != null) {
            mode = lacpAug.getLacpMode() == LacpActivityType.ACTIVE ? "mode active" : "mode passive";
            intervalCommand = lacpAug.getInterval() != null && lacpAug.getInterval() == LacpPeriodType.FAST
                    ? "lacp period short" : "no lacp period short";
        }

        // TODO we should probably check if the logical aggregate interface
        // exists
        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("interface %s" , ifcName),
                bundleIdCommand + " " + mode,
                intervalCommand,
                "commit",
                "end");
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
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();

        checkIfcType(ifcName);

        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("interface %s", ifcName),
                "no bundle id",
                "no lacp period short",
                "commit",
                "end");
    }

    private static void checkIfcType(String ifcName) {
        Preconditions.checkState(InterfaceConfigReader.parseType(ifcName) == EthernetCsmacd.class,
                "Cannot change ethernet configuration for non ethernet interface %s", ifcName);
    }
}
