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

package io.frinx.cli.unit.junos.ospf;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.junos.init.JunosDevices;
import io.frinx.cli.unit.junos.ospf.handler.AreaConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceBfdConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceBfdConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceTimersConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.AreaInterfaceTimersConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.MaxMetricConfigReader;
import io.frinx.cli.unit.junos.ospf.handler.MaxMetricConfigWriter;
import io.frinx.cli.unit.junos.ospf.handler.OspfAreaReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfUnit extends AbstractUnit {

    public OspfUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return JunosDevices.JUNOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "Junos OSPF unit";
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
        writeRegistry.add(IIDs.NE_NE_PR_PR_OS_GL_TI_MA_CONFIG, new MaxMetricConfigWriter(cli));
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AREA);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigWriter(cli),
                IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE);
        writeRegistry.subtreeAddAfter(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CO_AUG_OSPFAREAIFCONFAUG),
                IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG);

        writeRegistry.addNoop(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG);
        writeRegistry.addNoop(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG_BFD);
        writeRegistry.add(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG_BF_CONFIG,
                new AreaInterfaceBfdConfigWriter(cli));
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_TIMERS);
        writeRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_TI_CONFIG, new AreaInterfaceTimersConfigWriter(cli));
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_IN_CONFIG);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_GL_TI_MA_CONFIG, new MaxMetricConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AREA, new OspfAreaReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigReader());
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new AreaInterfaceReader(cli));
        readRegistry.subtreeAdd(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigReader(cli),
                Sets.newHashSet(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CO_AUG_OSPFAREAIFCONFAUG));
        readRegistry.add(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDCONFAUG_BF_CONFIG,
                new AreaInterfaceBfdConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_TI_CONFIG, new AreaInterfaceTimersConfigReader(cli));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                IIDs.FRINX_CISCO_OSPF_EXTENSION,
                io.frinx.openconfig.openconfig.bfd.IIDs.FRINX_BFD_EXTENSION,
                io.frinx.openconfig.openconfig.interfaces.IIDs.FRINX_BFD,
                IIDs.FRINX_OSPF_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }
}