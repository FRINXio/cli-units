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

package io.frinx.cli.unit.huawei.system;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.huawei.system.handler.connection.STelnetConfigReader;
import io.frinx.cli.unit.huawei.system.handler.connection.STelnetConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.connection.SshConfigReader;
import io.frinx.cli.unit.huawei.system.handler.connection.SshConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.connection.TelnetConfigReader;
import io.frinx.cli.unit.huawei.system.handler.connection.TelnetConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.terminal.TerminalConfigReader;
import io.frinx.cli.unit.huawei.system.handler.terminal.TerminalConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.terminal.TerminalReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.system.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpSystemUnit extends AbstractUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    public VrpSystemUnit(@Nonnull TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_SYSTEM, IIDs.FRINX_OPENCONFIG_EXTENSIONS,
                IIDs.FRINX_HUAWEI_TERMINAL_EXTENSION, IIDs.FRINX_HUAWEI_CONNECTION_EXTENSION,
                $YangModuleInfoImpl.getInstance());
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
        writeRegistry.add(IIDs.SY_SS_CONFIG, new SshConfigWriter(cli));
        writeRegistry.add(IIDs.SY_TE_CONFIG, new TelnetConfigWriter(cli));
        writeRegistry.addNoop(IIDs.SY_AUG_STELNETHUAWEIAUG_STELNETSERVER);
        writeRegistry.add(IIDs.SY_AUG_STELNETHUAWEIAUG_ST_CONFIG, new STelnetConfigWriter(cli));

        writeRegistry.addNoop(IIDs.SY_AUG_TERMINALHUAWEISCHEMASAUG_TE_TERMINAL);
        writeRegistry.subtreeAdd(IIDs.SY_AUG_TERMINALHUAWEISCHEMASAUG_TE_TE_CONFIG, new TerminalConfigWriter(cli),
                Sets.newHashSet(IIDs.SY_AUG_TERMINALHUAWEISCHEMASAUG_TE_TE_CO_ACL));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.SY_SS_CONFIG, new SshConfigReader(cli));
        readRegistry.add(IIDs.SY_TE_CONFIG, new TelnetConfigReader(cli));
        readRegistry.add(IIDs.SY_AUG_STELNETHUAWEIAUG_ST_CONFIG, new STelnetConfigReader(cli));

        readRegistry.add(IIDs.SY_AUG_SYSTEM1_TE_TERMINAL, new TerminalReader(cli));
        readRegistry.add(IIDs.SY_AUG_SYSTEM1_TE_TE_CONFIG, new TerminalConfigReader(cli));
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }

    @Override
    protected String getUnitName() {
        return "VRP AAA (Openconfig) translate unit";
    }
}
