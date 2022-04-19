/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.qos;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.ios.init.IosDevices;
import io.frinx.cli.unit.ios.qos.handler.classifier.ClassifierConfigReader;
import io.frinx.cli.unit.ios.qos.handler.classifier.ClassifierReader;
import io.frinx.cli.unit.ios.qos.handler.classifier.ClassifierWriter;
import io.frinx.cli.unit.ios.qos.handler.classifier.ConditionsReader;
import io.frinx.cli.unit.ios.qos.handler.classifier.TermReader;
import io.frinx.cli.unit.ios.qos.handler.ifc.EgressInterfaceConfigReader;
import io.frinx.cli.unit.ios.qos.handler.ifc.EgressInterfaceConfigWriter;
import io.frinx.cli.unit.ios.qos.handler.ifc.IngressInterfaceConfigReader;
import io.frinx.cli.unit.ios.qos.handler.ifc.IngressInterfaceConfigWriter;
import io.frinx.cli.unit.ios.qos.handler.ifc.InterfaceConfigReader;
import io.frinx.cli.unit.ios.qos.handler.ifc.InterfaceReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.InputConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.InputConfigWriter;
import io.frinx.cli.unit.ios.qos.handler.scheduler.InputReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.OneRateTwoColorConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.OneRateTwoColorConformActionConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.OneRateTwoColorExceedActionConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.OneRateTwoColorWriter;
import io.frinx.cli.unit.ios.qos.handler.scheduler.SchedulerConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.SchedulerConfigWriter;
import io.frinx.cli.unit.ios.qos.handler.scheduler.SchedulerPolicyReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.SchedulerPolicyWriter;
import io.frinx.cli.unit.ios.qos.handler.scheduler.SchedulerReader;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.qos.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class QoSUnit extends AbstractUnit {

    public QoSUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosDevices.IOS_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS QoS unit";
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
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_MULTIPLECOS,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_MU_COSSETS,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_MU_CO_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_MU_CO_ELEMENTS,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_MU_CO_EL_ELEMENT,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_MU_CO_EL_EL_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_ACCESSGROUP,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_AC_ACLSETS,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_AC_AC_CONFIG,
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
                        IIDs.QO_SC_SC_SC_SC_ON_EX_CO_AUG_QOSEXCEEDACTIONAUG));
        writeRegistry.addNoop(IIDs.QO_IN_INTERFACE);
        writeRegistry.addNoop(IIDs.QO_IN_IN_CONFIG);
        writeRegistry.subtreeAdd(IIDs.QO_IN_IN_IN_CONFIG, new IngressInterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_IN_CO_AUG_QOSINGRESSINTERFACEAUG));
        writeRegistry.subtreeAdd(IIDs.QO_IN_IN_OU_CONFIG, new EgressInterfaceConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_OU_CO_AUG_QOSEGRESSINTERFACEAUG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.QO_CL_CLASSIFIER, new ClassifierReader(cli));
        readRegistry.add(IIDs.QO_CL_CL_CONFIG, new ClassifierConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TERM, new TermReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_CONFIG));
        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TE_CONDITIONS, new ConditionsReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV4CONDITIONAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SCHEDULERPOLICY, new SchedulerPolicyReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_CONFIG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SCHEDULER, new SchedulerReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_CONFIG));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_IN_INPUT, new InputReader(cli));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_CONFIG, new SchedulerConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_CO_AUG_QOSSERVICEPOLICYAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_IN_IN_CO_AUG_QOSCOSAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_CO_AUG_QOSMAXQUEUEDEPTHBPSAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ON_CO_CONFIG, new OneRateTwoColorConformActionConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_CO_CO_AUG_QOSCONFORMACTIONAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ON_EX_CONFIG, new OneRateTwoColorExceedActionConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_EX_CO_AUG_QOSEXCEEDACTIONAUG));
        readRegistry.add(IIDs.QO_IN_INTERFACE, new InterfaceReader(cli));
        readRegistry.add(IIDs.QO_IN_IN_CONFIG, new InterfaceConfigReader());
        readRegistry.subtreeAdd(IIDs.QO_IN_IN_IN_CONFIG, new IngressInterfaceConfigReader(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_IN_CO_AUG_QOSINGRESSINTERFACEAUG));
        readRegistry.subtreeAdd(IIDs.QO_IN_IN_OU_CONFIG, new EgressInterfaceConfigReader(cli),
                Sets.newHashSet(IIDs.QO_IN_IN_OU_CO_AUG_QOSEGRESSINTERFACEAUG));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(), IIDs.FRINX_QOS_EXTENSION);
    }

}