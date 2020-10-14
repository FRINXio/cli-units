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
import io.frinx.cli.unit.ios.qos.handler.scheduler.InputConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.InputConfigWriter;
import io.frinx.cli.unit.ios.qos.handler.scheduler.InputReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.OneRateTwoColorConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.OneRateTwoColorConfigWriter;
import io.frinx.cli.unit.ios.qos.handler.scheduler.SchedulerPolicyReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.SchedulerPolicyWriter;
import io.frinx.cli.unit.ios.qos.handler.scheduler.SchedulerReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.TwoRateThreeColorConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.TwoRateThreeColorConformActionConfigReader;
import io.frinx.cli.unit.ios.qos.handler.scheduler.TwoRateThreeColorExceedActionConfigReader;
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
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_DSCP,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_DS_DSCPLIST,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_COS,
                        IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG_CO_COSLIST));
        writeRegistry.addNoop(IIDs.QO_SCHEDULERPOLICIES);
        writeRegistry.addNoop(IIDs.QO_SC_SCHEDULERPOLICY);
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SCHEDULER);
        writeRegistry.add(IIDs.QO_SC_SC_CONFIG, new SchedulerPolicyWriter(cli));
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_CONFIG);
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_INPUTS);
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_IN_INPUT);
        writeRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_IN_IN_CO_AUG_QOSCOSAUG));
        writeRegistry.add(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigWriter(cli));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.QO_CL_CLASSIFIER, new ClassifierReader(cli));
        readRegistry.add(IIDs.QO_CL_CL_CONFIG, new ClassifierConfigReader());
        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TERM, new TermReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_CONFIG));
        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TE_CONDITIONS, new ConditionsReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SCHEDULERPOLICY, new SchedulerPolicyReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_CONFIG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SCHEDULER, new SchedulerReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_CONFIG));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_IN_INPUT, new InputReader(cli));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigReader(cli));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigReader(cli));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_TW_CONFIG, new TwoRateThreeColorConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_TW_CO_CONFIG, new TwoRateThreeColorConformActionConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_TW_CO_CO_AUG_QOSCONFORMACTIONAUG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_TW_EX_CONFIG, new TwoRateThreeColorExceedActionConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_TW_EX_CO_AUG_QOSEXCEEDACTIONAUG));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(), IIDs.FRINX_QOS_EXTENSION);
    }

}