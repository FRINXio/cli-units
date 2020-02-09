/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos.network.instance.handler.NetworkInstanceConfigReader;
import io.frinx.cli.unit.saos.network.instance.handler.NetworkInstanceConfigWriter;
import io.frinx.cli.unit.saos.network.instance.handler.NetworkInstanceReader;
import io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan.DefaultVlanConfigReader;
import io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan.DefaultVlanConfigWriter;
import io.frinx.cli.unit.saos.network.instance.handler.vrf.vlan.DefaultVlanReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosNetworkInstanceUnit extends AbstractUnit {

    public SaosNetworkInstanceUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return SaosDevices.SAOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Saos-6 Network Instance (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_NETWORK_INSTANCE);
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
        writeRegistry.addNoop(IIDs.NE_NETWORKINSTANCE);
        writeRegistry.addAfter(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigWriter(cli));
        writeRegistry.addNoop(IIDs.NE_NE_VL_VLAN);
        writeRegistry.addAfter(IIDs.NE_NE_VL_VL_CONFIG, new DefaultVlanConfigWriter(cli), IIDs.NE_NE_CONFIG);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NETWORKINSTANCE, new NetworkInstanceReader(cli));
        readRegistry.add(IIDs.NE_NE_CONFIG, new NetworkInstanceConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_VL_VLAN, new DefaultVlanReader(cli));
        readRegistry.add(IIDs.NE_NE_VL_VL_CONFIG, new DefaultVlanConfigReader(cli));
    }
}