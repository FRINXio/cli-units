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

package io.frinx.cli.iosxr.qos;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericListReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.qos.handler.classifier.ActionConfigReader;
import io.frinx.cli.iosxr.qos.handler.classifier.ActionsWriter;
import io.frinx.cli.iosxr.qos.handler.classifier.ClassifierConfigReader;
import io.frinx.cli.iosxr.qos.handler.classifier.ClassifierReader;
import io.frinx.cli.iosxr.qos.handler.classifier.ClassifierWriter;
import io.frinx.cli.iosxr.qos.handler.classifier.ConditionsReader;
import io.frinx.cli.iosxr.qos.handler.classifier.RemarkConfigReader;
import io.frinx.cli.iosxr.qos.handler.classifier.TermReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.InputConfigReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.InputConfigWriter;
import io.frinx.cli.iosxr.qos.handler.scheduler.InputReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.OneRateTwoColorConfigReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.OneRateTwoColorConfigWriter;
import io.frinx.cli.iosxr.qos.handler.scheduler.SchedulerPolicyReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.SchedulerPolicyWriter;
import io.frinx.cli.iosxr.qos.handler.scheduler.SchedulerReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.qos.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.TermsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.actions.RemarkBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.ClassifiersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.OneRateTwoColorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.InputsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.SchedulerPoliciesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.SchedulersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.top.QosBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class XRQoSUnit implements TranslateUnit {

    private static final InstanceIdentifier<Conditions> CONDITIONS_ID = InstanceIdentifier.create(Conditions.class);

    private static final InstanceIdentifier<Classifier> CLASSIFIER_ID = InstanceIdentifier.create(Classifier.class);

    private static final InstanceIdentifier<Actions> ACTIONS_ID = InstanceIdentifier.create(Actions.class);

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public XRQoSUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IosXrDevices.IOS_XR_ALL, this);
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
    public void provideHandlers(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull Context context) {
        Cli cli = context.getTransport();
        provideReaders(readRegistry, cli);
        provideWriters(writeRegistry, cli);
    }

    private void provideWriters(CustomizerAwareWriteRegistryBuilder writeRegistry, Cli cli) {
        writeRegistry.add(new GenericWriter<>(IIDs.QOS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_CLASSIFIERS, new NoopCliWriter<>()));
        writeRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_CONFIG, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TERMS, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TERM, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CONFIG, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CONDITIONS, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG,
                        CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_MPLS, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_MP_CONFIG, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IPV4, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV4CONDITIONAUG,
                        CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IPV6, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QOS_CLA_CLA_TER_TER_CON_IPV_CONFIG, CLASSIFIER_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV6CONDITIONAUG, CLASSIFIER_ID)),
                new GenericWriter<>(IIDs.QO_CL_CLASSIFIER, new ClassifierWriter(cli)));
        writeRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_AC_CONFIG, ACTIONS_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_AC_REMARK, ACTIONS_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_AC_RE_CONFIG, ACTIONS_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_AC_RE_CO_AUG_QOSREMARKQOSGROUPAUG,
                        ACTIONS_ID)),
                new GenericWriter<>(IIDs.QO_CL_CL_TE_TE_ACTIONS, new ActionsWriter(cli)));

        writeRegistry.add(new GenericWriter<>(IIDs.QO_SCHEDULERPOLICIES, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_SC_SCHEDULERPOLICY, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_SC_SC_SC_SCHEDULER, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_SC_SC_CONFIG, new SchedulerPolicyWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_SC_SC_SC_SC_CONFIG, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_SC_SC_SC_SC_INPUTS, new NoopCliWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_SC_SC_SC_SC_IN_INPUT, new NoopCliListWriter<>()));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigWriter(cli)));
        writeRegistry.add(new GenericWriter<>(IIDs.QO_SC_SC_SC_SC_ONERATETWOCOLOR, new NoopCliWriter<>()));

        writeRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_SC_SC_SC_SC_ON_CO_AUG_QOSMAXQUEUEDEPTHMSAUG,
                        InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos
                                .rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config.class))),
                new GenericWriter<>(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigWriter(cli)));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.addStructuralReader(IIDs.QOS, QosBuilder.class);
        readRegistry.addStructuralReader(IIDs.QO_CLASSIFIERS, ClassifiersBuilder.class);
        readRegistry.add(new GenericListReader<>(IIDs.QO_CL_CLASSIFIER, new ClassifierReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.QO_CL_CL_CONFIG, new ClassifierConfigReader()));
        readRegistry.addStructuralReader(IIDs.QO_CL_CL_TERMS, TermsBuilder.class);

        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CONFIG, InstanceIdentifier.create(Term.class))
        ), new GenericListReader<>(IIDs.QO_CL_CL_TE_TERM, new TermReader(cli)));

        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG, CONDITIONS_ID),

                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IPV4, CONDITIONS_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG, CONDITIONS_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV4CONDITIONAUG, CONDITIONS_ID),

                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IPV6, CONDITIONS_ID),
                RWUtils.cutIdFromStart(IIDs.QOS_CLA_CLA_TER_TER_CON_IPV_CONFIG, CONDITIONS_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV6CONDITIONAUG, CONDITIONS_ID),

                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_MPLS, CONDITIONS_ID),
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_MP_CONFIG, CONDITIONS_ID)
        ), new GenericConfigReader<>(IIDs.QO_CL_CL_TE_TE_CONDITIONS, new ConditionsReader(cli)));

        readRegistry.addStructuralReader(IIDs.QO_CL_CL_TE_TE_ACTIONS, ActionsBuilder.class);
        readRegistry.add(new GenericConfigReader<>(IIDs.QO_CL_CL_TE_TE_AC_CONFIG, new ActionConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.QO_CL_CL_TE_TE_AC_REMARK, RemarkBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_AC_RE_CO_AUG_QOSREMARKQOSGROUPAUG,
                        InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos
                                .rev161216.qos.common.remark.actions.Config.class))
        ), new GenericConfigReader<>(IIDs.QO_CL_CL_TE_TE_AC_RE_CONFIG, new RemarkConfigReader(cli)));

        readRegistry.addStructuralReader(IIDs.QO_SCHEDULERPOLICIES, SchedulerPoliciesBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_SC_SC_CONFIG, InstanceIdentifier.create(SchedulerPolicy.class))),
                new GenericListReader<>(IIDs.QO_SC_SCHEDULERPOLICY, new SchedulerPolicyReader(cli)));
        readRegistry.addStructuralReader(IIDs.QO_SC_SC_SCHEDULERS, SchedulersBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_SC_SC_SC_SC_CONFIG, InstanceIdentifier.create(Scheduler.class))
        ), new GenericListReader<>(IIDs.QO_SC_SC_SC_SCHEDULER, new SchedulerReader(cli)));
        readRegistry.addStructuralReader(IIDs.QO_SC_SC_SC_SC_INPUTS, InputsBuilder.class);
        readRegistry.add(new GenericListReader<>(IIDs.QO_SC_SC_SC_SC_IN_INPUT, new InputReader(cli)));
        readRegistry.add(new GenericConfigReader<>(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigReader(cli)));
        readRegistry.addStructuralReader(IIDs.QO_SC_SC_SC_SC_ONERATETWOCOLOR, OneRateTwoColorBuilder.class);
        readRegistry.subtreeAdd(Sets.newHashSet(
                RWUtils.cutIdFromStart(IIDs.QO_SC_SC_SC_SC_ON_CO_AUG_QOSMAXQUEUEDEPTHMSAUG,
                        InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos
                                .rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config.class))
        ), new GenericConfigReader<>(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension
                        .rev180304.$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS XR QoS unit";
    }
}
