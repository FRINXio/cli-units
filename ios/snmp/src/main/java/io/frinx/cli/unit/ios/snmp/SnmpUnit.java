/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.snmp;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.ios.snmp.handler.CommunityConfigReader;
import io.frinx.cli.unit.ios.snmp.handler.CommunityReader;
import io.frinx.cli.unit.ios.snmp.handler.CommunityWriter;
import io.frinx.cli.unit.ios.snmp.handler.ViewConfigReader;
import io.frinx.cli.unit.ios.snmp.handler.ViewReader;
import io.frinx.cli.unit.ios.snmp.handler.ViewWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.snmp.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SnmpUnit extends AbstractUnit {

    public SnmpUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS SNMP unit";
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
        writeRegistry.addNoop(IIDs.SNMP);

        writeRegistry.addNoop(IIDs.SN_VIEWS);
        writeRegistry.subtreeAdd(IIDs.SN_VI_VIEW, new ViewWriter(cli),
                Sets.newHashSet(IIDs.SN_VI_VI_CONFIG,
                        IIDs.SN_VI_VI_CO_MIB));

        writeRegistry.addNoop(IIDs.SN_COMMUNITIES);
        writeRegistry.subtreeAdd(IIDs.SN_CO_COMMUNITY, new CommunityWriter(cli),
                Sets.newHashSet(IIDs.SN_CO_CO_CONFIG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.SN_VI_VIEW, new ViewReader(cli));
        readRegistry.add(IIDs.SN_VI_VI_CONFIG, new ViewConfigReader(cli));

        readRegistry.add(IIDs.SN_CO_COMMUNITY, new CommunityReader(cli));
        readRegistry.add(IIDs.SN_CO_CO_CONFIG, new CommunityConfigReader(cli));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(), IIDs.FRINX_SNMP);
    }

}