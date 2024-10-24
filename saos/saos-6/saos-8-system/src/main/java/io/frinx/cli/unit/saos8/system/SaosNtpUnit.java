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

package io.frinx.cli.unit.saos8.system;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.saos.init.SaosDevices;
import io.frinx.cli.unit.saos8.system.handler.NtpAuthenticationConfigReader;
import io.frinx.cli.unit.saos8.system.handler.NtpAuthenticationReader;
import io.frinx.cli.unit.saos8.system.handler.NtpAuthenticationStateReader;
import io.frinx.cli.unit.saos8.system.handler.NtpServerConfigReader;
import io.frinx.cli.unit.saos8.system.handler.NtpServerReader;
import io.frinx.cli.unit.saos8.system.handler.NtpServerStateReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.system.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class SaosNtpUnit extends AbstractUnit {

    public SaosNtpUnit(@NotNull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Sets.newHashSet(SaosDevices.SAOS_8);
    }

    @Override
    protected String getUnitName() {
        return "SAOS-8 NTP (Openconfig) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_SYSTEM,
                IIDs.FRINX_CIENA_NTP_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public void provideHandlers(@NotNull CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull Context context) {
        var cli = context.getTransport();

        provideReaders(readRegistry, cli);
    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.SY_NT_SE_SERVER, new NtpServerReader(cli));
        readRegistry.add(IIDs.SY_NT_SE_SE_CONFIG, new NtpServerConfigReader());
        readRegistry.add(IIDs.SY_NT_SE_SE_STATE, new NtpServerStateReader(cli));

        readRegistry.add(IIDs.SY_NT_NT_NTPKEY, new NtpAuthenticationReader(cli));
        readRegistry.add(IIDs.SY_NT_NT_NT_CONFIG, new NtpAuthenticationConfigReader());
        readRegistry.add(IIDs.SY_NT_NT_NT_STATE, new NtpAuthenticationStateReader(cli));
    }
}