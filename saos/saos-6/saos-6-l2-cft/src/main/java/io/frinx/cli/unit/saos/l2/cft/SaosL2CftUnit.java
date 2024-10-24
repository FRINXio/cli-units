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

package io.frinx.cli.unit.saos.l2.cft;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos.l2.cft.handler.L2CftConfigReader;
import io.frinx.cli.unit.saos.l2.cft.handler.L2CftConfigWriter;
import io.frinx.cli.unit.saos.l2.cft.handler.profile.L2CftProfileConfigReader;
import io.frinx.cli.unit.saos.l2.cft.handler.profile.L2CftProfileConfigWriter;
import io.frinx.cli.unit.saos.l2.cft.handler.profile.L2CftProfileProtocolConfigReader;
import io.frinx.cli.unit.saos.l2.cft.handler.profile.L2CftProfileProtocolConfigWriter;
import io.frinx.cli.unit.saos.l2.cft.handler.profile.L2CftProfileProtocolReader;
import io.frinx.cli.unit.saos.l2.cft.handler.profile.L2CftProfileReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.l2.cft.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosL2CftUnit extends AbstractUnit {

    public SaosL2CftUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Set.of(SaosDevices.SAOS_6);
    }

    @Override
    protected String getUnitName() {
        return "Saos-6 L2-cft (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                IIDs.FRINX_OPENCONFIG_L2_CFT,
                $YangModuleInfoImpl.getInstance()
        );
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();

        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.L2CFT);
        writeRegistry.add(IIDs.L2_CONFIG, new L2CftConfigWriter(cli));
        writeRegistry.addNoop(IIDs.L2_PROFILES);
        writeRegistry.addNoop(IIDs.L2_PR_PROFILE);
        writeRegistry.add(IIDs.L2_PR_PR_CONFIG, new L2CftProfileConfigWriter(cli));
        writeRegistry.addNoop(IIDs.L2_PR_PR_PROTOCOLS);
        writeRegistry.addNoop(IIDs.L2_PR_PR_PR_PROTOCOL);
        writeRegistry.add(IIDs.L2_PR_PR_PR_PR_CONFIG, new L2CftProfileProtocolConfigWriter(cli));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.L2_CONFIG, new L2CftConfigReader(cli));
        readRegistry.add(IIDs.L2_PR_PROFILE, new L2CftProfileReader(cli));
        readRegistry.add(IIDs.L2_PR_PR_CONFIG, new L2CftProfileConfigReader());
        readRegistry.add(IIDs.L2_PR_PR_PR_PROTOCOL, new L2CftProfileProtocolReader(cli));
        readRegistry.add(IIDs.L2_PR_PR_PR_PR_CONFIG, new L2CftProfileProtocolConfigReader(cli));
    }
}