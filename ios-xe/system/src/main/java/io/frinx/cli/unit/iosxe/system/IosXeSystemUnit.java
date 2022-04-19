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
package io.frinx.cli.unit.iosxe.system;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxe.init.IosXeDevices;
import io.frinx.cli.unit.iosxe.system.handler.ntp.NtpAccessGroupConfigReader;
import io.frinx.cli.unit.iosxe.system.handler.ntp.NtpAccessGroupConfigWriter;
import io.frinx.cli.unit.iosxe.system.handler.ntp.NtpInterfaceConfigReader;
import io.frinx.cli.unit.iosxe.system.handler.ntp.NtpInterfaceConfigWriter;
import io.frinx.cli.unit.iosxe.system.handler.ntp.NtpServerConfigReader;
import io.frinx.cli.unit.iosxe.system.handler.ntp.NtpServerConfigWriter;
import io.frinx.cli.unit.iosxe.system.handler.ntp.NtpServerReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.system.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;


public class IosXeSystemUnit extends AbstractUnit {
    public IosXeSystemUnit(@Nonnull final TranslationUnitCollector translationRegistry) {
        super(translationRegistry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(IosXeDevices.IOS_XE_16);
    }

    @Override
    protected String getUnitName() {
        return "IOS XE System (Openconfig) translation unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(IIDs.FRINX_OPENCONFIG_SYSTEM,
                IIDs.FRINX_OPENCONFIG_EXTENSIONS, IIDs.FRINX_HUAWEI_CONNECTION_EXTENSION,
                IIDs.FRINX_CISCO_NTP_EXTENSION);
    }

    @Override
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry, @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.addNoop(IIDs.SYSTEM);
        writeRegistry.addNoop(IIDs.SY_NTP);
        writeRegistry.addNoop(IIDs.SY_NT_SERVERS);
        writeRegistry.addNoop(IIDs.SY_NT_SE_SERVER);
        writeRegistry.subtreeAdd(IIDs.SY_NT_SE_SE_CONFIG, new NtpServerConfigWriter(cli),
                Sets.newHashSet(IIDs.SY_NT_SE_SE_CO_AUG_VRFCISCOAUG,
                        IIDs.SY_NT_SE_SE_CO_AUG_VRFCISCOAUG));
        writeRegistry.addNoop(IIDs.SY_NT_AUG_NTPCISCOAUG);
        writeRegistry.addNoop(IIDs.SY_NT_AUG_NTPCISCOAUG_SOURCEINTERFACE);
        writeRegistry.add(IIDs.SY_NT_AUG_NTPCISCOAUG_SO_CONFIG, new NtpInterfaceConfigWriter(cli));
        writeRegistry.add(IIDs.SY_NT_AUG_NTPCISCOAUG_AC_CONFIG, new NtpAccessGroupConfigWriter(cli));


    }

    private void provideReaders(CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.SY_NT_SE_SERVER, new NtpServerReader(cli));
        readRegistry.add(IIDs.SY_NT_SE_SE_CONFIG, new NtpServerConfigReader(cli));
        readRegistry.add(IIDs.SY_NT_AUG_NTPCISCOAUG_SO_CONFIG, new NtpInterfaceConfigReader(cli));
        readRegistry.add(IIDs.SY_NT_AUG_NTPCISCOAUG_AC_CONFIG, new NtpAccessGroupConfigReader(cli));
    }
}
