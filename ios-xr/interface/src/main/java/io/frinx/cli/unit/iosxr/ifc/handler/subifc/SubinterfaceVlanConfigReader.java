/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;
import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.getSubinterfaceName;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.vlan.rev160526.VlanLogicalConfig;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.vlan.rev160526.vlan.logical.top.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.vlan.rev160526.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.vlan.rev160526.vlan.logical.top.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.vlan.types.rev160526.VlanId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceVlanConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public SubinterfaceVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        // Only parse configuration for non 0 subifc
        if (subKey.getIndex() == ZERO_SUBINTERFACE_ID) {
            return;
        }

        String subIfcName = getSubinterfaceName(id);

        String output = blockingRead(String.format("sh run interface %s", subIfcName), cli, id, ctx);
        parseVlanTag(output, builder);
    }

    private static final Pattern VLAN_TAG_LINE = Pattern.compile("encapsulation dot1Q (?<tag>[0-9]+)");

    @VisibleForTesting
    static void parseVlanTag(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output,
                VLAN_TAG_LINE::matcher,
                matcher -> matcher.group("tag"),
                tag -> builder.setVlanId(new VlanLogicalConfig.VlanId(new VlanId(Integer.valueOf(tag)))));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder,
                      @Nonnull Config readValue) {
        ((VlanBuilder) parentBuilder).setConfig(readValue);
    }
}
