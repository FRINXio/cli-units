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
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
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
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.openconfig.openconfig.qos.IIDs;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public class XRQoSUnit extends AbstractUnit {

    public XRQoSUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return IosXrDevices.IOS_XR_ALL;
    }

    @Override
    protected String getUnitName() {
        return "IOS XR QoS unit";
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
                        IIDs.QO_CL_CL_TE_TE_CO_MPLS,
                        IIDs.QO_CL_CL_TE_TE_CO_MP_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CO_IPV4,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV4CONDITIONAUG,
                        IIDs.QO_CL_CL_TE_TE_CO_IPV6,
                        IIDs.QOS_CLA_CLA_TER_TER_CON_IPV_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV6CONDITIONAUG));
        writeRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TE_ACTIONS, new ActionsWriter(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_AC_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_AC_REMARK,
                        IIDs.QO_CL_CL_TE_TE_AC_RE_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_AC_RE_CO_AUG_QOSREMARKQOSGROUPAUG));

        writeRegistry.addNoop(IIDs.QO_SCHEDULERPOLICIES);
        writeRegistry.addNoop(IIDs.QO_SC_SCHEDULERPOLICY);
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SCHEDULER);
        writeRegistry.add(IIDs.QO_SC_SC_CONFIG, new SchedulerPolicyWriter(cli));
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_CONFIG);
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_INPUTS);
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_IN_INPUT);
        writeRegistry.add(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigWriter(cli));
        writeRegistry.addNoop(IIDs.QO_SC_SC_SC_SC_ONERATETWOCOLOR);

        writeRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigWriter(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_CO_AUG_QOSMAXQUEUEDEPTHMSAUG));
    }

    private void provideReaders(@Nonnull CustomizerAwareReadRegistryBuilder readRegistry, Cli cli) {
        readRegistry.add(IIDs.QO_CL_CLASSIFIER, new ClassifierReader(cli));
        readRegistry.add(IIDs.QO_CL_CL_CONFIG, new ClassifierConfigReader());

        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TERM, new TermReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_CONFIG));

        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TE_CONDITIONS, new ConditionsReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_CO_AUG_QOSCONDITIONAUG,
                        IIDs.QO_CL_CL_TE_TE_CO_IPV4,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV4CONDITIONAUG,
                        IIDs.QO_CL_CL_TE_TE_CO_IPV6,
                        IIDs.QOS_CLA_CLA_TER_TER_CON_IPV_CONFIG,
                        IIDs.QO_CL_CL_TE_TE_CO_IP_CO_AUG_QOSIPV6CONDITIONAUG,
                        IIDs.QO_CL_CL_TE_TE_CO_MPLS,
                        IIDs.QO_CL_CL_TE_TE_CO_MP_CONFIG));

        readRegistry.add(IIDs.QO_CL_CL_TE_TE_AC_CONFIG, new ActionConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.QO_CL_CL_TE_TE_AC_RE_CONFIG, new RemarkConfigReader(cli),
                Sets.newHashSet(IIDs.QO_CL_CL_TE_TE_AC_RE_CO_AUG_QOSREMARKQOSGROUPAUG));

        readRegistry.subtreeAdd(IIDs.QO_SC_SCHEDULERPOLICY, new SchedulerPolicyReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_CONFIG));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SCHEDULER, new SchedulerReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_CONFIG));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_IN_INPUT, new InputReader(cli));
        readRegistry.add(IIDs.QO_SC_SC_SC_SC_IN_IN_CONFIG, new InputConfigReader(cli));
        readRegistry.subtreeAdd(IIDs.QO_SC_SC_SC_SC_ON_CONFIG, new OneRateTwoColorConfigReader(cli),
                Sets.newHashSet(IIDs.QO_SC_SC_SC_SC_ON_CO_AUG_QOSMAXQUEUEDEPTHMSAUG));
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet($YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension
                        .rev180304.$YangModuleInfoImpl.getInstance());
    }
}
