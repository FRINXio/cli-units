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

package io.frinx.cli.iosxr.ospf;

import static io.frinx.cli.iosxr.IosXrDevices.IOS_XR_ALL;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.ospf.handler.AreaConfigReader;
import io.frinx.cli.iosxr.ospf.handler.AreaConfigWriter;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceConfigReader;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceConfigWriter;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceEnableBfdConfigReader;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceEnableBfdConfigWriter;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceMplsSyncConfigReader;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceMplsSyncConfigWriter;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceReader;
import io.frinx.cli.iosxr.ospf.handler.GlobalConfigReader;
import io.frinx.cli.iosxr.ospf.handler.GlobalConfigWriter;
import io.frinx.cli.iosxr.ospf.handler.InterfaceRefReader;
import io.frinx.cli.iosxr.ospf.handler.MaxMetricTimerConfigReader;
import io.frinx.cli.iosxr.ospf.handler.MaxMetricTimerConfigWriter;
import io.frinx.cli.iosxr.ospf.handler.MaxMetricTimerReader;
import io.frinx.cli.iosxr.ospf.handler.OspfAreaReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.OspfAreaIfBfdExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.OspfAreaIfBfdExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.enable.EnableBfd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.enable.EnableBfdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.enable.enable.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.ref.InterfaceRefBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.Timers1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.Timers1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.MaxMetricTimers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.MaxMetricTimersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.MaxMetricTimer;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.MplsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.mpls.IgpLdpSyncBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.TimersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.AreasBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class OspfUnit implements TranslateUnit {

    private static final InstanceIdentifier<Timers1> TIMERS1 = IIDs.NE_NE_PR_PR_OS_GL_TIMERS.augmentation(Timers1.class);
    private static final InstanceIdentifier<MaxMetricTimers> MAX_METRIC_TIMERS = TIMERS1.child(MaxMetricTimers.class);
    private static final InstanceIdentifier<MaxMetricTimer> MAX_METRIC_TIMER = MAX_METRIC_TIMERS.child(MaxMetricTimer.class);
    private static final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.max.metric.timer.Config> CONFIG_IID =
            MAX_METRIC_TIMER.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.max.metric.timer.Config.class);
    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public OspfUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }

    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull Context context) {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(ModifiableWriterRegistryBuilder wRegistry, Cli cli) {
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_CONFIG);
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_GL_TIMERS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(TIMERS1, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(MAX_METRIC_TIMERS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(MAX_METRIC_TIMER, new NoopCliWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(CONFIG_IID, new MaxMetricTimerConfigWriter(cli)), IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AREA, new NoopCliListWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_OS_GL_CONFIG);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new NoopCliListWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE.augmentation(OspfAreaIfBfdExtAug.class), new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE.augmentation(OspfAreaIfBfdExtAug.class)
                .child(EnableBfd.class), new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE.augmentation(OspfAreaIfBfdExtAug.class)
                .child(EnableBfd.class)
                .child(Config.class), new AreaInterfaceEnableBfdConfigWriter(cli)));

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MPLS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MP_IGPLDPSYNC, new NoopCliWriter<>()));
        wRegistry.addAfter(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MP_IG_CONFIG, new AreaInterfaceMplsSyncConfigWriter(cli)),
                IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG);

        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_IN_CONFIG, new NoopCliWriter<>()));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OSPFV2, Ospfv2Builder.class);
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_GLOBAL, GlobalBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_GL_CONFIG, new GlobalConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_GL_TIMERS, TimersBuilder.class);
        rRegistry.addStructuralReader(TIMERS1, Timers1Builder.class);
        rRegistry.addStructuralReader(MAX_METRIC_TIMERS, MaxMetricTimersBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(MAX_METRIC_TIMER, new MaxMetricTimerReader(cli)));
        rRegistry.add(new GenericConfigReader<>(CONFIG_IID, new MaxMetricTimerConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AREAS, AreasBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_OS_AR_AREA, new OspfAreaReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_CONFIG, new AreaConfigReader()));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_INTERFACES, InterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE, new AreaInterfaceReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_CONFIG, new AreaInterfaceConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MPLS, MplsBuilder.class);
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MP_IGPLDPSYNC, IgpLdpSyncBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_MP_IG_CONFIG, new AreaInterfaceMplsSyncConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE.augmentation(OspfAreaIfBfdExtAug.class), OspfAreaIfBfdExtAugBuilder.class);
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE.augmentation(OspfAreaIfBfdExtAug.class)
                .child(EnableBfd.class), EnableBfdBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_INTERFACE.augmentation(OspfAreaIfBfdExtAug.class)
                .child(EnableBfd.class)
                .child(Config.class), new AreaInterfaceEnableBfdConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_INTERFACEREF, InterfaceRefBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.NE_NE_PR_PR_OS_AR_AR_IN_IN_IN_CONFIG, new InterfaceRefReader()));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.$YangModuleInfoImpl.getInstance(),
                $YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS XR OSPF unit";
    }
}
