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

import static io.frinx.cli.iosxr.IosXrDevices.IOS_XR_ALL;
import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.read.GenericListReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.qos.handler.classifier.ActionConfigReader;
import io.frinx.cli.iosxr.qos.handler.classifier.ActionConfigWriter;
import io.frinx.cli.iosxr.qos.handler.classifier.ClassifierConfigReader;
import io.frinx.cli.iosxr.qos.handler.classifier.ClassifierReader;
import io.frinx.cli.iosxr.qos.handler.classifier.ClassifierWriter;
import io.frinx.cli.iosxr.qos.handler.classifier.ConditionsReader;
import io.frinx.cli.iosxr.qos.handler.classifier.RemarkConfigReader;
import io.frinx.cli.iosxr.qos.handler.classifier.RemarkConfigWriter;
import io.frinx.cli.iosxr.qos.handler.classifier.TermReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.InputConfigReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.InputReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.OneRateTwoColorConfigReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.SchedulerPolicyReader;
import io.frinx.cli.iosxr.qos.handler.scheduler.SchedulerReader;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.utils.NoopCliWriter;
import io.frinx.openconfig.openconfig.qos.IIDs;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv6ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthMsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosRemarkQosGroupAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.TermsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
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

    private final TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public XRQoSUnit(@Nonnull final TranslationUnitCollector registry) {
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
        wRegistry.add(new GenericWriter<>(IIDs.QOS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.QO_CLASSIFIERS, new NoopCliWriter<>()));
        wRegistry.subtreeAdd(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_CONFIG, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TERMS, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TERM, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CONFIG, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CONDITIONS, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CONDITIONS.augmentation(QosConditionAug.class), CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_MPLS, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_MP_CONFIG, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IPV4, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG.augmentation(QosIpv4ConditionAug.class), CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IPV6, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QOS_CLA_CLA_TER_TER_CON_IPV_CONFIG, CLASSIFIER_ID),
            RWUtils.cutIdFromStart(IIDs.QOS_CLA_CLA_TER_TER_CON_IPV_CONFIG.augmentation(QosIpv6ConditionAug.class), CLASSIFIER_ID)),
            new GenericWriter<>(IIDs.QO_CL_CLASSIFIER, new ClassifierWriter(cli)));
        wRegistry.add(new GenericWriter<>(IIDs.QO_CL_CL_TE_TE_ACTIONS, new NoopCliWriter<>()));
        wRegistry.add(new GenericWriter<>(IIDs.QO_CL_CL_TE_TE_AC_CONFIG, new ActionConfigWriter(cli)));
        wRegistry.add(new GenericWriter<>(IIDs.QO_CL_CL_TE_TE_AC_REMARK, new NoopCliWriter<>()));
        wRegistry.subtreeAdd(Sets.newHashSet(
               RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_AC_RE_CONFIG.augmentation(QosRemarkQosGroupAug.class),
                   InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.common.remark.actions.Config.class))
        ), new GenericWriter<>(IIDs.QO_CL_CL_TE_TE_AC_RE_CONFIG, new RemarkConfigWriter(cli)));
    }

    private void provideReaders(@Nonnull ModifiableReaderRegistryBuilder rRegistry, Cli cli) {
        rRegistry.addStructuralReader(IIDs.QOS, QosBuilder.class);
        rRegistry.addStructuralReader(IIDs.QO_CLASSIFIERS, ClassifiersBuilder.class);
        rRegistry.add(new GenericListReader<>(IIDs.QO_CL_CLASSIFIER, new ClassifierReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.QO_CL_CL_CONFIG, new ClassifierConfigReader()));
        rRegistry.addStructuralReader(IIDs.QO_CL_CL_TERMS, TermsBuilder.class);

        rRegistry.subtreeAdd(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CONFIG, InstanceIdentifier.create(Term.class))
        ), new GenericListReader<>(IIDs.QO_CL_CL_TE_TERM, new TermReader(cli)));

        rRegistry.subtreeAdd(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CONDITIONS.augmentation(QosConditionAug.class), CONDITIONS_ID),

            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IPV4, CONDITIONS_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG, CONDITIONS_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG.augmentation(QosIpv4ConditionAug.class), CONDITIONS_ID),

            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_IPV6, CONDITIONS_ID),
            RWUtils.cutIdFromStart(IIDs.QOS_CLA_CLA_TER_TER_CON_IPV_CONFIG, CONDITIONS_ID),
            RWUtils.cutIdFromStart(IIDs.QOS_CLA_CLA_TER_TER_CON_IPV_CONFIG.augmentation(QosIpv6ConditionAug.class), CONDITIONS_ID),

            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_MPLS, CONDITIONS_ID),
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_CO_MP_CONFIG, CONDITIONS_ID)
        ), new GenericConfigReader<>(IIDs.QO_CL_CL_TE_TE_CONDITIONS, new ConditionsReader(cli)));

        rRegistry.addStructuralReader(IIDs.QO_CL_CL_TE_TE_ACTIONS, ActionsBuilder.class);
        rRegistry.add(new GenericConfigReader<>(IIDs.QO_CL_CL_TE_TE_AC_CONFIG, new ActionConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.QO_CL_CL_TE_TE_AC_REMARK, RemarkBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.QO_CL_CL_TE_TE_AC_RE_CONFIG.augmentation(QosRemarkQosGroupAug.class),
                InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.common.remark.actions.Config.class))
        ), new GenericConfigReader<>(IIDs.QO_CL_CL_TE_TE_AC_RE_CONFIG, new RemarkConfigReader(cli)));

        rRegistry.addStructuralReader(IIDs.QO_SCHEDULERPOLICIES, SchedulerPoliciesBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.QO_SC_SC_CONFIG, InstanceIdentifier.create(SchedulerPolicy.class))),
            new GenericListReader<>(IIDs.QO_SC_SCHEDULERPOLICY, new SchedulerPolicyReader(cli)));
        rRegistry.addStructuralReader(IIDs.QO_SC_SC_SCHEDULERS, SchedulersBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.QO_SC_SC_SC_SC_CONFIG, InstanceIdentifier.create(Scheduler.class))
        ), new GenericListReader<>(IIDs.QO_SC_SC_SC_SCHEDULER, new SchedulerReader(cli)));
        rRegistry.addStructuralReader(IIDs.QO_SC_SC_SC_SC_INPUTS, InputsBuilder.class);
        rRegistry.add(new GenericListReader<>(IIDs.QO_SC_SC_SC_SC_IN_INPUT, new InputReader(cli)));
        rRegistry.add(new GenericConfigReader<>(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigReader(cli)));
        rRegistry.addStructuralReader(IIDs.QO_SC_SC_SC_SC_ONERATETWOCOLOR, OneRateTwoColorBuilder.class);
        rRegistry.subtreeAdd(Sets.newHashSet(
            RWUtils.cutIdFromStart(IIDs.QO_SC_SC_SC_SC_ON_CONFIG.augmentation(QosMaxQueueDepthMsAug.class),
                InstanceIdentifier.create(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config.class))
        ), new GenericConfigReader<>(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigReader(cli)));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.$YangModuleInfoImpl.getInstance());
    }

    @Override
    public String toString() {
        return "IOS XR QoS unit";
    }
}
