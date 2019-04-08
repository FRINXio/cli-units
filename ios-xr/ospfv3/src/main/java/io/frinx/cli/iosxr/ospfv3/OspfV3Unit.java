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
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.fd.honeycomb.translate.spi.builder.CheckRegistry;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.ospfv3.handler.StubRouterConfigReader;
import io.frinx.cli.iosxr.ospfv3.handler.StubRouterConfigWriter;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.openconfig.openconfig.ospfv3.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF3;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfV3Unit implements TranslateUnit {

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

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

    private static final CheckRegistry CHECK_REGISTRY = ChecksMap.getOpenconfigCheckRegistry();

    private static Function<InstanceIdentifier<? extends DataObject>, Check> ospfv3TypeCheck() {
        return id -> BasicCheck.checkPath(new ProtocolKey(OSPF3.class, null));
    }

    static {
        CHECK_REGISTRY.add(IIDs.NE_NE_PR_PR_AUG_PROTOCOLOSPFV3EXTAUG_OSPFV3, ospfv3TypeCheck());
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        readRegistry.setCheckRegistry(CHECK_REGISTRY);
        provideReaders(readRegistry, cli);
        writeRegistry.setCheckRegistry(CHECK_REGISTRY);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder rreg, Cli cli) {
        rreg.add(IIDs.NE_NE_PR_PR_AUG_PROTOCOLOSPFV3EXTAUG_OS_GL_CO_ST_CONFIG, new StubRouterConfigReader(cli));
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder wreg, Cli cli) {
        wreg.addNoop(IIDs.NE_NE_PR_PR_AUG_PROTOCOLOSPFV3EXTAUG_OS_GL_CONFIG);
        wreg.addNoop(IIDs.NE_NE_PR_PR_AUG_PROTOCOLOSPFV3EXTAUG_OS_GL_CO_STUBROUTER);
        wreg.add(IIDs.NE_NE_PR_PR_AUG_PROTOCOLOSPFV3EXTAUG_OS_GL_CO_ST_CONFIG, new StubRouterConfigWriter(cli));
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
