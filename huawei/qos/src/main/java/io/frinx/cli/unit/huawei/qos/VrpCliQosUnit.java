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
package io.frinx.cli.unit.huawei.qos;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.huawei.qos.handler.behavior.BehaviorConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.behavior.BehaviorConfigWriter;
import io.frinx.cli.unit.huawei.qos.handler.behavior.BehaviorReader;
import io.frinx.cli.unit.huawei.qos.handler.classifier.ClassifierConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.classifier.ClassifierReader;
import io.frinx.cli.unit.huawei.qos.handler.classifier.ClassifierWriter;
import io.frinx.cli.unit.huawei.qos.handler.classifier.ConditionsReader;
import io.frinx.cli.unit.huawei.qos.handler.classifier.TermReader;
import io.frinx.cli.unit.huawei.qos.handler.ifc.EgressInterfaceConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.ifc.EgressInterfaceConfigWriter;
import io.frinx.cli.unit.huawei.qos.handler.ifc.IngressInterfaceConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.ifc.IngressInterfaceConfigWriter;
import io.frinx.cli.unit.huawei.qos.handler.ifc.InterfaceConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.ifc.InterfaceReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.InputConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.InputConfigWriter;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.InputReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.OneRateTwoColorConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.OneRateTwoColorConformActionConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.OneRateTwoColorExceedActionConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.OneRateTwoColorWriter;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.OneRateTwoColorYellowActionConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.SchedulerConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.SchedulerConfigWriter;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.SchedulerPolicyReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.SchedulerPolicyWriter;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.SchedulerReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.TwoRateThreeColorConfigReader;
import io.frinx.cli.unit.huawei.qos.handler.scheduler.TwoRateThreeColorWriter;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.qos.IIDs;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class VrpCliQosUnit extends AbstractUnit {

    private static final Device HUAWEI = new DeviceIdBuilder()
            .setDeviceType("vrp")
            .setDeviceVersion("*")
            .build();

    public VrpCliQosUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected String getUnitName() {
        return "VRP QoS unit";
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
        writeRegistry.addNoop(IIDs.QOS);
        writeRegistry.addNoop(IIDs.QO_CLASSIFIERS);
        writeRegistry.subtreeAdd(IIDs.QO_CL_CLASSIFIER, new ClassifierWriter(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_CONFIG,
                        IIDs.QO_CL_CL_TERMS,
                        IIDs.QO_CL_CL_TE_TERM,
                        IIDs.QO_CL_CL_TE_TE_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CONDITIONS,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_COS,
                        IIDs.QO_CL_CL_TE_TE_CO_IPV4,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV4CONDITIONAUG));
        writeRegistry.addNoop(IIDs.QO_SCHEDULERPOLICIES);
        writeRegistry.addNoop(IIDs.QO_SC_SCHEDULERPOLICY);
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SCHEDULER);
        writeRegistry.add(IIDs.QO_SC_SC_CONFIG, new SchedulerPolicyWriter(cli));
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_INPUTS);
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_IN_INPUT);
        writeRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_CONFIG, new SchedulerConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_CO_AUG_QOSSERVICEPOLICYAUG));
        writeRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_IN_IN_CO_AUG_QOSCOSAUG));
        writeRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ONERATETWOCOLOR, new OneRateTwoColorWriter(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_CONFIG,
                        IIDs.QO_SC_SC_SC_SC_ON_CO_AUG_QOSMAXQUEUEDEPTHBPSAUG,
                        IIDs.QO_SC_SC_SC_SC_ON_CO_CONFIG,
                        IIDs.QO_SC_SC_SC_SC_ON_CO_CO_AUG_QOSCONFORMACTIONAUG,
                        IIDs.QO_SC_SC_SC_SC_ON_EX_CONFIG,
                        IIDs.QO_SC_SC_SC_SC_ON_EX_CO_AUG_QOSEXCEEDACTIONAUG,
                        IIDs.QO_SC_SC_SC_SC_ON_AUG_VRPYELLOWACTION_YE_CONFIG));
        writeRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_TWORATETHREECOLOR, new TwoRateThreeColorWriter(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_TW_CONFIG,
                        IIDs.QO_SC_SC_SC_SC_TW_CO_AUG_QOSTWOCOLORCONFIG));
        writeRegistry.addNoop(IIDs.QO_IN_INTERFACE);
        writeRegistry.addNoop(IIDs.QO_IN_IN_CONFIG);
        writeRegistry.subtreeAdd(IIDs.QO_IN_IN_IN_CONFIG, new IngressInterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_IN_CO_AUG_QOSINGRESSINTERFACEAUG));
        writeRegistry.subtreeAdd(IIDs.QO_IN_IN_OU_CONFIG, new EgressInterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_OU_CO_AUG_QOSEGRESSINTERFACEAUG));

        writeRegistry.add(IIDs.QO_AUG_VRPQOSBEHAVIORAUG_BE_BE_CONFIG, new BehaviorConfigWriter(cli));
    }

    private void provideReaders(@NotNull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.QO_AUG_VRPQOSBEHAVIORAUG_BE_BEHAVIOR, new BehaviorReader(cli));
        readRegistry.add(IIDs.QO_AUG_VRPQOSBEHAVIORAUG_BE_BE_CONFIG, new BehaviorConfigReader(cli));

        readRegistry.add(IIDs.QO_CL_CLASSIFIER, new ClassifierReader(cli));
        readRegistry.subtreeAdd(IIDs.QO_CL_CL_CONFIG, new ClassifierConfigReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_CO_AUG_VRPQOSCLASSIFIERAUG));

        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TERM, new TermReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_CONFIG));
        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TE_CONDITIONS, new ConditionsReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV4CONDITIONAUG));

        readRegistry.add(IIDs.QO_IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.QO_IN_IN_CONFIG, new InterfaceConfigReader());
        readRegistry.subtreeAdd(IIDs.QO_IN_IN_IN_CONFIG, new IngressInterfaceConfigReader(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_IN_CO_AUG_QOSINGRESSINTERFACEAUG));
        readRegistry.subtreeAdd(IIDs.QO_IN_IN_OU_CONFIG, new EgressInterfaceConfigReader(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_OU_CO_AUG_QOSEGRESSINTERFACEAUG));

        readRegistry.subtreeAdd(IIDs.QO_SC_SCHEDULERPOLICY, new SchedulerPolicyReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_CONFIG, IIDs.QO_SC_SC_SC_SC_CO_AUG_VRPQOSSCHEDULERCONFAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SCHEDULER, new SchedulerReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_CONFIG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_CONFIG, new SchedulerConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_CO_AUG_QOSSERVICEPOLICYAUG,
                        IIDs.QO_SC_SC_SC_SC_CO_AUG_VRPQOSSCHEDULERCONFAUG));

        readRegistry.add(IIDs.QO_SC_SC_SC_SC_IN_INPUT, new InputReader(cli));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_IN_IN_CO_AUG_VRPQOSSCHEDULERINPUTAUG));

        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_CO_AUG_QOSMAXQUEUEDEPTHMSAUG,
                        IIDs.QO_SC_SC_SC_SC_ON_CO_AUG_VRPQOSSCHEDULERCOLORAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ON_CO_CONFIG, new OneRateTwoColorConformActionConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_CO_CO_AUG_QOSCONFORMACTIONAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ON_EX_CONFIG, new OneRateTwoColorExceedActionConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_EX_CO_AUG_QOSEXCEEDACTIONAUG));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_ON_AUG_VRPYELLOWACTION_YE_CONFIG,
                new OneRateTwoColorYellowActionConfigReader(cli));

        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_TW_CONFIG, new TwoRateThreeColorConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_TW_CO_AUG_QOSTWOCOLORCONFIG));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(), IIDs.FRINX_QOS_EXTENSION);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(HUAWEI);
    }
}