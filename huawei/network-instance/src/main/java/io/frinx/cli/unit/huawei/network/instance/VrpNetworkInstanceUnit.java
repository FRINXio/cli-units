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

package io.frinx.cli.unit.huawei.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.huawei.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.huawei.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.huawei.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.ifc.L3VrfInterfaceReader;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.ifc.L3VrfInterfaceWriter;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.protocol.ProtocolConfigReader;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.protocol.ProtocolConfigWriter;
import io.frinx.cli.unit.huawei.network.instance.handler.vrf.protocol.ProtocolReader;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpNetworkInstanceUnit implements TranslateUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public VrpNetworkInstanceUnit(@Nonnull final TranslationUnitCollector registry) {
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
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
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
        // No handling required on the network instance level
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);
        writeRegistry.addAfter(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli),
                /*handle after ifc configuration*/ io.frinx.openconfig.openconfig.interfaces.IIDs.IN_IN_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PROTOCOL);
        writeRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigWriter());

        // Interfaces for VRF
        writeRegistry.addNoop(IIDs.NE_NE_INTERFACES);
        writeRegistry.add(IIDs.NE_NE_IN_INTERFACE, new L3VrfInterfaceWriter(cli));
        writeRegistry.addNoop(IIDs.NE_NE_IN_IN_CONFIG);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        // VRFs
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));

        // Interfaces for VRF
        readRegistry.add(IIDs.NE_NE_IN_INTERFACE, new L3VrfInterfaceReader(cli));

        // Protocols for VRF
        readRegistry.add(IIDs.NE_NE_PR_PROTOCOL, new ProtocolReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_CONFIG, new ProtocolConfigReader());
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228
                .$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "VRP Network Instance (Openconfig) translate unit";
    }
}
