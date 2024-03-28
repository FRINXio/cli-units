/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.saos6.aaa;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos6.aaa.handler.AuthenticationUserConfigReader;
import io.frinx.cli.unit.saos6.aaa.handler.AuthenticationUserReader;
import io.frinx.cli.unit.saos6.aaa.handler.AuthenticationUserWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.aaa.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosAaaUnit extends AbstractUnit {

    public SaosAaaUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Sets.newHashSet(SaosDevices.SAOS_6);
    }

    @Override
    protected String getUnitName() {
        return "SAOS-6 AAA (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_AAA,
                IIDs.FRINX_CIENA_AAA_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        Cli cli = context.getTransport();

        provideWriters(writeRegistry, cli);
        provideReaders(readRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.AA_AU_USERS);
        writeRegistry.subtreeAdd(IIDs.AA_AU_US_USER, new AuthenticationUserWriter(cli),
                Sets.newHashSet(IIDs.AA_AU_US_US_CONFIG,
                        IIDs.AA_AU_US_US_CO_AUG_AAACIENAUSERAUG));
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.AA_AU_US_USER, new AuthenticationUserReader(cli));
        readRegistry.add(IIDs.AA_AU_US_US_CONFIG, new AuthenticationUserConfigReader(cli));
    }
}