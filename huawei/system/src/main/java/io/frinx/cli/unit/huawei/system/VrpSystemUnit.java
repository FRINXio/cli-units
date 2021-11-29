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
import io.frinx.cli.unit.huawei.system.handler.factory.FactoryConfigurationConfigReader;
import io.frinx.cli.unit.huawei.system.handler.factory.FactoryConfigurationConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.global.config.BannerConfigReader;
import io.frinx.cli.unit.huawei.system.handler.global.config.BannerConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.global.config.ClockDaylightSavingTimeConfigReader;
import io.frinx.cli.unit.huawei.system.handler.global.config.ClockDaylightSavingTimeConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.global.config.ClockTimezoneConfigReader;
import io.frinx.cli.unit.huawei.system.handler.global.config.ClockTimezoneConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.global.config.SystemNameConfigReader;
import io.frinx.cli.unit.huawei.system.handler.global.config.SystemNameConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.http.HttpStatusConfigReader;
import io.frinx.cli.unit.huawei.system.handler.http.HttpStatusConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.ntp.NtpConfigReader;
import io.frinx.cli.unit.huawei.system.handler.ntp.NtpConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.ntp.NtpInterfaceConfigReader;
import io.frinx.cli.unit.huawei.system.handler.ntp.NtpInterfaceConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.ntp.NtpServerConfigReader;
import io.frinx.cli.unit.huawei.system.handler.ntp.NtpServerConfigWriter;
import io.frinx.cli.unit.huawei.system.handler.ntp.NtpServerReader;
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
                IIDs.FRINX_HUAWEI_GLOBAL_CONFIG_EXTENSION,
                IIDs.FRINX_HUAWEI_HTTP_STATUS_EXTENSION,
                IIDs.FRINX_HUAWEI_FACTORY_CONFIG_EXTENSION, $YangModuleInfoImpl.getInstance());
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

        writeRegistry.addNoop(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_BANNER);
        writeRegistry.add(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_BA_CONFIG, new BannerConfigWriter(cli));

        writeRegistry.addNoop(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SYSTEMNAME);
        writeRegistry.add(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_CONFIG, new SystemNameConfigWriter(cli));

        writeRegistry.addNoop(IIDs.SY_AUG_HTTPSERVERSTATUSHUAWEIAUG_HTTPSERVERSTATUS);
        writeRegistry.add(IIDs.SY_AUG_HTTPSERVERSTATUSHUAWEIAUG_HT_CONFIG, new HttpStatusConfigWriter(cli));

        writeRegistry.addNoop(IIDs.SY_AUG_FACTORYCONFIGURATIONSTATUSHUAWEIAUG_FACTORYCONFIGURATIONSTATUS);
        writeRegistry.add(IIDs.SY_AUG_FACTORYCONFIGURATIONSTATUSHUAWEIAUG_FA_CONFIG,
                new FactoryConfigurationConfigWriter(cli));

        writeRegistry.addNoop(IIDs.SY_NTP);
        writeRegistry.addNoop(IIDs.SY_NT_SERVERS);
        writeRegistry.addNoop(IIDs.SY_NT_SE_SERVER);
        writeRegistry.add(IIDs.SY_NT_CONFIG, new NtpConfigWriter(cli));
        writeRegistry.add(IIDs.SY_NT_SE_SE_CONFIG, new NtpServerConfigWriter(cli));
        writeRegistry.add(IIDs.SY_AUG_NTPINTHUAWEIAUG_NT_CONFIG, new NtpInterfaceConfigWriter(cli));
        writeRegistry.add(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_TI_CONFIG, new ClockTimezoneConfigWriter(cli));
        writeRegistry.add(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_DA_CONFIG,
                new ClockDaylightSavingTimeConfigWriter(cli));
        writeRegistry.addNoop(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_DA_CO_ENDDAY);
        writeRegistry.addNoop(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_DA_CO_STARTDAY);
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.SY_SS_CONFIG, new SshConfigReader(cli));
        readRegistry.add(IIDs.SY_TE_CONFIG, new TelnetConfigReader(cli));
        readRegistry.add(IIDs.SY_AUG_STELNETHUAWEIAUG_ST_CONFIG, new STelnetConfigReader(cli));

        readRegistry.add(IIDs.SY_AUG_SYSTEM1_TE_TERMINAL, new TerminalReader(cli));
        readRegistry.add(IIDs.SY_AUG_SYSTEM1_TE_TE_CONFIG, new TerminalConfigReader(cli));

        readRegistry.add(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_BA_CONFIG, new BannerConfigReader(cli));
        readRegistry.add(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_CONFIG, new SystemNameConfigReader(cli));

        readRegistry.add(IIDs.SY_AUG_HTTPSERVERSTATUSHUAWEIAUG_HT_CONFIG, new HttpStatusConfigReader(cli));

        readRegistry.add(IIDs.SY_AUG_FACTORYCONFIGURATIONSTATUSHUAWEIAUG_FA_CONFIG,
                new FactoryConfigurationConfigReader(cli));

        readRegistry.add(IIDs.SY_NT_CONFIG, new NtpConfigReader(cli));
        readRegistry.add(IIDs.SY_NT_SE_SE_CONFIG, new NtpServerConfigReader(cli));
        readRegistry.add(IIDs.SY_NT_SE_SERVER, new NtpServerReader(cli));
        readRegistry.add(IIDs.SY_AUG_NTPINTHUAWEIAUG_NT_CONFIG, new NtpInterfaceConfigReader(cli));
        readRegistry.add(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_TI_CONFIG, new ClockTimezoneConfigReader(cli));
        readRegistry.add(IIDs.SY_AUG_GLOBALCONFIGHUAWEIAUG_SY_DA_CONFIG,
                new ClockDaylightSavingTimeConfigReader(cli));
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
