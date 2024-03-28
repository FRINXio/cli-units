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

package io.frinx.cli.unit.saos.platform;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos.platform.handler.SaosComponentConfigReader;
import io.frinx.cli.unit.saos.platform.handler.SaosComponentReader;
import io.frinx.cli.unit.saos.platform.handler.SaosComponentStateReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.platform.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosPlatformUnit extends AbstractUnit {

    public SaosPlatformUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Set.of(SaosDevices.SAOS_6);
    }

    @Override
    protected String getUnitName() {
        return "SAOS 6 Platform unit";
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        var cli = context.getTransport();
        provideReaders(readRegistry, cli);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.CO_COMPONENT, new SaosComponentReader(cli));
        readRegistry.add(IIDs.CO_CO_CONFIG, new SaosComponentConfigReader());
        readRegistry.add(IIDs.CO_CO_STATE, new SaosComponentStateReader(cli));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_CIENA_PLATFORM_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }
}