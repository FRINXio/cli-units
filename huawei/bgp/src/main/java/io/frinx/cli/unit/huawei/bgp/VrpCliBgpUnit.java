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

package io.frinx.cli.unit.huawei.bgp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.huawei.bgp.handler.local.aggregates.BgpLocalAggregateConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.local.aggregates.BgpLocalAggregateConfigWriter;
import io.frinx.cli.unit.huawei.bgp.handler.local.aggregates.BgpLocalAggregateReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborAfiSafiReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborPolicyConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpCliBgpUnit extends AbstractUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    public VrpCliBgpUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigWriter(cli), IIDs.NE_NE_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigWriter(cli),
            IIDs.NE_NE_PR_PR_BG_GL_CONFIG);

        // Neighbor writer, handle also subtrees
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborWriter(cli),
            Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG,
                    InstanceIdentifier.create(Neighbor.class)),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_TRANSPORT,
                    InstanceIdentifier.create(Neighbor.class)),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_TR_CONFIG,
                    InstanceIdentifier.create(Neighbor.class)),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_APPLYPOLICY,
                    InstanceIdentifier.create(Neighbor.class)),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AP_CONFIG,
                    InstanceIdentifier.create(Neighbor.class)),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AFISAFIS,
                    InstanceIdentifier.create(Neighbor.class)),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI,
                    InstanceIdentifier.create(Neighbor.class)),
                RWUtils.cutIdFromStart(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AF_CONFIG,
                    InstanceIdentifier.create(Neighbor.class))
            ), IIDs.NE_NE_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_LOCALAGGREGATES);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_LO_AGGREGATE);

        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new BgpLocalAggregateConfigWriter(cli),
                IIDs.NE_NE_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_CONFIG,
                IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, IIDs.NE_NE_PR_PR_OS_GL_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI,new GlobalAfiSafiReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigReader());

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborReader(cli));

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, new NeighborAfiSafiReader(cli),
                Sets.newHashSet(InstanceIdentifier.create(AfiSafi.class).child(Config.class))
        );

        readRegistry.add(IIDs.NE_NE_PR_PR_BG_NE_NE_AP_CONFIG, new NeighborPolicyConfigReader(cli));

        // Local aggregates
        readRegistry.add(IIDs.NE_NE_PR_PR_LO_AGGREGATE, new BgpLocalAggregateReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_LO_AG_CONFIG, new BgpLocalAggregateConfigReader());
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }

    @Override
    protected String getUnitName() {
        return "VRP BGP (Openconfig) translate unit";
    }
}
