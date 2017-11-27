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
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        checkIfcType(ifcName);

        // TODO we should probably check if the logical aggregate interface
        // exists
        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("interface %s" , ifcName),
                f("bundle id %s mode on",
                        getBundleId(dataAfter.getAugmentation(Config1.class).getAggregateId())),
                "commit",
                "end");
    }

    private static Pattern AGGREGATE_IFC_NAME = Pattern.compile("Bundle-Ether(?<id>\\d+)");

    private static String getBundleId(String aggregateIfcName) {
        Matcher aggregateIfcNameMatcher = AGGREGATE_IFC_NAME.matcher(aggregateIfcName.trim());

        Preconditions.checkArgument(aggregateIfcNameMatcher.matches(),
                "aggregate-id %s should reference LAG interface", aggregateIfcName);

        return aggregateIfcNameMatcher.group("id");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();

        checkIfcType(ifcName);

        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("interface %s", ifcName),
                "no bundle",
                "commit",
                "end");
    }

    private static void checkIfcType(String ifcName) {
        Preconditions.checkState(InterfaceConfigReader.parseType(ifcName) == EthernetCsmacd.class,
                "Cannot change ethernet configuration for non ethernet interface %s", ifcName);
    }
}
