/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ospf;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaConfigReader;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaConfigWriter;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceConfigReader;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceConfigWriter;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceEnableBfdConfigReader;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceEnableBfdConfigWriter;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceMplsSyncConfigReader;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceMplsSyncConfigWriter;
import io.frinx.cli.unit.iosxr.ospf.handler.AreaInterfaceReader;
import io.frinx.cli.unit.iosxr.ospf.handler.GlobalConfigReader;
import io.frinx.cli.unit.iosxr.ospf.handler.GlobalConfigWriter;
import io.frinx.cli.unit.iosxr.ospf.handler.MaxMetricTimerConfigReader;
import io.frinx.cli.unit.iosxr.ospf.handler.MaxMetricTimerConfigWriter;
import io.frinx.cli.unit.iosxr.ospf.handler.MaxMetricTimerReader;
import io.frinx.cli.unit.iosxr.ospf.handler.OspfAreaReader;
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
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR OSPF unit";
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
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigWriter(cli),
                IIDs.NE_NE_PR_PR_CONFIG);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_GL_TIMERS);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_GL_TI_AUG_TIMERS1);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_GL_TI_AUG_TIMERS1_MAXMETRICTIMERS);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_GL_TI_AUG_TIMERS1_MA_MAXMETRICTIMER);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_GL_TI_MA_CONFIG);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_OS_GL_TI_AUG_TIMERS1_MA_MA_CONFIG,
                new MaxMetricTimerConfigWriter(cli), IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AREA);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigWriter(cli),
                IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigWriter(cli),
                IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG);

        writeRegistry.addNoop(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDEXTAUG);
        writeRegistry.addNoop(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDEXTAUG_ENABLEBFD);
        writeRegistry.add(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDEXTAUG_EN_CONFIG,
                new AreaInterfaceEnableBfdConfigWriter(cli));

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MPLS);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MP_IGPLDPSYNC);
        writeRegistry.addAfter(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MP_IG_CONFIG,
                new AreaInterfaceMplsSyncConfigWriter(cli), IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG);

        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF);
        writeRegistry.addNoop(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_IN_CONFIG);
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_GL_TI_AUG_TIMERS1_MA_MAXMETRICTIMER, new MaxMetricTimerReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_GL_TI_AUG_TIMERS1_MA_MA_CONFIG, new MaxMetricTimerConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AREA, new OspfAreaReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigReader());
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new AreaInterfaceReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigReader(cli));
        readRegistry.add(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MP_IG_CONFIG, new AreaInterfaceMplsSyncConfigReader(cli));
        readRegistry.add(
                io.frinx.openconfig.openconfig.bfd.IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_AUG_OSPFAREAIFBFDEXTAUG_EN_CONFIG,
                new AreaInterfaceEnableBfdConfigReader(cli));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                IIDs.FRINX_CISCO_OSPF_EXTENSION,
                io.frinx.openconfig.openconfig.bfd.IIDs.FRINX_BFD_EXTENSION,
                $YangModuleInfoImpl.getInstance());
    }
}