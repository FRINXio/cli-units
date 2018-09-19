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
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborAfiSafiReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborPolicyConfigReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.unit.huawei.bgp.handler.neighbor.NeighborWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpCliBgpUnit implements TranslateUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();


    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public VrpCliBgpUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(HUAWEI, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance());
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final TranslateUnit.Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigWriter(cli)),
                IIDs.NE_NE_CONFIG);

        writeRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI, new NoopCliWriter<>()));
        writeRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG, new GlobalAfiSafiConfigWriter(
                cli)),
                IIDs.NE_NE_PR_PR_BG_GL_CONFIG);

        // Neighbor writer, handle also subtrees
        writeRegistry.subtreeAddAfter(
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
                                InstanceIdentifier.create(Neighbor.class))),
                new GenericListWriter<>(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborWriter(cli)),
                Sets.newHashSet(IIDs.NE_NE_CONFIG, IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BGP, BgpBuilder.class);

        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_GLOBAL, GlobalBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_GL_CONFIG, new GlobalConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_GL_AFISAFIS,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202
                        .bgp.global.base.AfiSafisBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AFISAFI,
                new GlobalAfiSafiReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_GL_AF_AF_CONFIG,
                new GlobalAfiSafiConfigReader()));

        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NEIGHBORS, NeighborsBuilder.class);
        readRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_NE_NEIGHBOR, new NeighborReader(cli)));

        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_CONFIG, new NeighborConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_AFISAFIS, AfiSafisBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(InstanceIdentifier.create(AfiSafi.class).child(Config.class)),
                new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AF_AFISAFI, new NeighborAfiSafiReader(cli)));

        readRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_BG_NE_NE_APPLYPOLICY, ApplyPolicyBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_BG_NE_NE_AP_CONFIG,
                new NeighborPolicyConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "VRP BGP (Openconfig) translate unit";
    }
}
