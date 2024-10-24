/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.oam;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.oam.handler.CfmConfigReader;
import io.frinx.cli.unit.iosxr.oam.handler.CfmConfigWriter;
import io.frinx.cli.unit.iosxr.oam.handler.domain.CfmDomainConfigReader;
import io.frinx.cli.unit.iosxr.oam.handler.domain.CfmDomainConfigWriter;
import io.frinx.cli.unit.iosxr.oam.handler.domain.CfmDomainReader;
import io.frinx.cli.unit.iosxr.oam.handler.domain.CfmMaConfigReader;
import io.frinx.cli.unit.iosxr.oam.handler.domain.CfmMaConfigWriter;
import io.frinx.cli.unit.iosxr.oam.handler.domain.CfmMaReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.oam.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class IosXROamUnit extends AbstractUnit {
    public IosXROamUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR OAM unit";
    }

    @Override
    public void provideHandlers(
        @NotNull CustomizerAwareReadRegistryBuilder readRegistry,
        @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
        @NotNull Context context) {

        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder rreg, Cli cli) {
        rreg.add(IIDs.OA_CF_CONFIG, new CfmConfigReader(cli));
        rreg.add(IIDs.OA_CF_DO_DOMAIN, new CfmDomainReader(cli));
        rreg.add(IIDs.OA_CF_DO_DO_CONFIG, new CfmDomainConfigReader(cli));
        rreg.add(IIDs.OA_CF_DO_DO_MA_MA, new CfmMaReader(cli));
        rreg.add(IIDs.OA_CF_DO_DO_MA_MA_CONFIG, new CfmMaConfigReader(cli));
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder wreg, Cli cli) {
        wreg.addNoop(IIDs.OAM);
        wreg.addNoop(IIDs.OA_CFM);
        wreg.add(IIDs.OA_CF_CONFIG, new CfmConfigWriter(cli));
        wreg.addNoop(IIDs.OA_CF_DOMAINS);
        wreg.addNoop(IIDs.OA_CF_DO_DOMAIN);
        wreg.addAfter(IIDs.OA_CF_DO_DO_CONFIG, new CfmDomainConfigWriter(cli), IIDs.OA_CF_CONFIG);
        wreg.addNoop(IIDs.OA_CF_DO_DO_MAS);
        wreg.addNoop(IIDs.OA_CF_DO_DO_MA_MA);
        wreg.addAfter(IIDs.OA_CF_DO_DO_MA_MA_CONFIG, new CfmMaConfigWriter(cli), IIDs.OA_CF_DO_DO_CONFIG);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
            IIDs.FRINX_OAM,
            IIDs.FRINX_OAM_TYPES);
    }
}