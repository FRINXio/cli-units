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

package io.frinx.cli.iosxr.ospfv3;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.IosXrDevices;
import io.frinx.cli.iosxr.ospfv3.handler.StubRouterConfigReader;
import io.frinx.cli.iosxr.ospfv3.handler.StubRouterConfigWriter;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ProtocolOspfv3ExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ProtocolOspfv3ExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.StubRouter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.StubRouterBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.top.Ospfv3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.top.Ospfv3Builder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfV3Unit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    // AUGMENTATION IIDs
    private static final InstanceIdentifier<ProtocolOspfv3ExtAug> NE_NE_PR_PR_EXT_AUG =
            IIDs.NE_NE_PR_PROTOCOL.augmentation(ProtocolOspfv3ExtAug.class);

    // OspfV3 IIDs
    private static final InstanceIdentifier<Ospfv3> NE_NE_PR_PR_OSPFV3 =
            NE_NE_PR_PR_EXT_AUG.child(Ospfv3.class);
    // Global IIDs
    private static final InstanceIdentifier<Global> NE_NE_PR_PR_OS_GLOBAL = NE_NE_PR_PR_OSPFV3.child(Global.class);
    // Global/Config IIDs
    private static final InstanceIdentifier<Config> NE_NE_PR_PR_OS_GL_CONFIG =
            NE_NE_PR_PR_OS_GLOBAL.child(Config.class);
    // StubRouter IIDs
    private static final InstanceIdentifier<StubRouter> NE_NE_PR_PR_OS_GL_CO_STUBROUTER =
            NE_NE_PR_PR_OS_GL_CONFIG.child(StubRouter.class);
    // StubRouter/Config IIDs
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
        .ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.Config>
        NE_NE_PR_PR_OS_GL_CO_ST_CONFIG =
        NE_NE_PR_PR_OS_GL_CO_STUBROUTER.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
        .ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.Config.class);


    public OspfV3Unit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder readRegistry,
            @Nonnull ModifiableWriterRegistryBuilder writeRegistry, @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rreg, Cli cli) {
        rreg.addStructuralReader(NE_NE_PR_PR_EXT_AUG, ProtocolOspfv3ExtAugBuilder.class);
        rreg.addStructuralReader(NE_NE_PR_PR_OSPFV3, Ospfv3Builder.class);
        rreg.addStructuralReader(NE_NE_PR_PR_OS_GLOBAL, GlobalBuilder.class);
        rreg.addStructuralReader(NE_NE_PR_PR_OS_GL_CONFIG,
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .ospfv3.rev180817.ospfv3.global.structural.global.ConfigBuilder.class);
        rreg.addStructuralReader(NE_NE_PR_PR_OS_GL_CO_STUBROUTER, StubRouterBuilder.class);
        rreg.add(new GenericConfigReader<>(NE_NE_PR_PR_OS_GL_CO_ST_CONFIG, new StubRouterConfigReader(cli)));
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wreg, Cli cli) {
        wreg.addAfter(new GenericWriter<>(NE_NE_PR_PR_OS_GL_CONFIG, new NoopCliWriter<>()),
                IIDs.NE_NE_PR_PR_CONFIG);
        wreg.add(new GenericWriter<>(NE_NE_PR_PR_OS_GL_CO_STUBROUTER, new NoopCliWriter<>()));
        wreg.add(new GenericWriter<>(NE_NE_PR_PR_OS_GL_CO_ST_CONFIG, new StubRouterConfigWriter(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                $YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .ospfv3.types.rev180817.$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS XR OSPFv3 unit";
    }
}
