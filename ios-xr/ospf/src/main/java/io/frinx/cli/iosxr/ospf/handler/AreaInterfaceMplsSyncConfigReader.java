/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.mpls.IgpLdpSyncBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.mpls.igp.ldp.sync.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.mpls.igp.ldp.sync.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceMplsSyncConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_OSPF_INT = "do show running-config router ospf %s area %s interface %s";
    private static final Pattern MPLS_SYNC_LINE = Pattern.compile("mpls ldp sync(?<disable> disable)*");
    private final Cli cli;

    public AreaInterfaceMplsSyncConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((IgpLdpSyncBuilder) parentBuilder).setConfig(readValue);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        final String ospfId = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        final String areaId = AreaInterfaceReader.areaIdToString(instanceIdentifier.firstKeyOf(Area.class).getIdentifier());
        String output = blockingRead(String.format(SHOW_OSPF_INT, ospfId, areaId, interfaceId), cli, instanceIdentifier, readContext);
        parseMplsSync(output, configBuilder);
    }

    @VisibleForTesting
    public static void parseMplsSync(String output, ConfigBuilder configBuilder) {
        NEWLINE.splitAsStream(output)
                .map(MPLS_SYNC_LINE::matcher)
                .filter(Matcher::find)
                .findAny()
                .ifPresent(matcher -> {
                    if (matcher.group("disable") == null) {
                        configBuilder.setEnabled(true);
                    } else {
                        configBuilder.setEnabled(false);
                    }
                });
    }
}
