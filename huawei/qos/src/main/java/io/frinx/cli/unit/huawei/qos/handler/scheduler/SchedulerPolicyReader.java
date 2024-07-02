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
package io.frinx.cli.unit.huawei.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerPolicyConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosSchedulerPolicyConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerPolicyReader implements CliConfigListReader<SchedulerPolicy, SchedulerPolicyKey,
        SchedulerPolicyBuilder> {

    public static final String SH_POLICY_MAPS = "display current-configuration | include traffic policy";
    public static final Pattern POLICY_NAME_LINE = Pattern.compile("traffic policy (?<name>.+)");
    public static final Pattern POLICY_NAME_SHORT = Pattern.compile("(?<policyName>.+) match-order config");
    private static final Pattern TRAFFIC_BEHAVIOR = Pattern.compile("Behavior: (?<behavior>.+)");
    private static final Pattern TRAFFIC_CLASSIFIER = Pattern.compile("Classifier: (?<classifier>.+)");
    private static final String POLICY_DATA = "display traffic policy user-defined %s";

    private Cli cli;

    public SchedulerPolicyReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<SchedulerPolicyKey> getAllIds(@NotNull InstanceIdentifier<SchedulerPolicy> instanceIdentifier,
                                              @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_POLICY_MAPS, cli, instanceIdentifier, readContext);
        return getSchedulerKeys(output);
    }

    @VisibleForTesting
    public static List<SchedulerPolicyKey> getSchedulerKeys(String output) {
        return ParsingUtils.parseFields(output, 0, POLICY_NAME_LINE::matcher,
            matcher -> matcher.group("name"), SchedulerPolicyKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<SchedulerPolicy> instanceIdentifier, @NotNull
            SchedulerPolicyBuilder schedulerPolicyBuilder, @NotNull ReadContext readContext) throws
            ReadFailedException {
        final String fullPolicyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        final String showPolicyData = String.format(POLICY_DATA, getShortPolicyName(fullPolicyName));
        final String policyData = blockingRead(showPolicyData, cli, instanceIdentifier, readContext);
        schedulerPolicyBuilder.setName(fullPolicyName);
        setPolicyData(policyData, schedulerPolicyBuilder);
    }

    private static void setPolicyData(String policyData, SchedulerPolicyBuilder schedulerPolicyBuilder) {
        VrpQosSchedulerPolicyConfAugBuilder vrq = new VrpQosSchedulerPolicyConfAugBuilder();

        ParsingUtils.parseField(policyData, 0,
                TRAFFIC_CLASSIFIER::matcher,
                matcher -> matcher.group("classifier"),
                vrq::setTrafficClassifier);

        ParsingUtils.parseField(policyData, 0,
                TRAFFIC_BEHAVIOR::matcher,
                matcher -> matcher.group("behavior"),
                vrq::setTrafficBehavior);

        schedulerPolicyBuilder.addAugmentation(VrpQosSchedulerPolicyConfAug.class, vrq.build());
    }

    private static String getShortPolicyName(@NotNull String fullName) {
        return ParsingUtils.parseField(fullName, 0,
                POLICY_NAME_SHORT::matcher,
                matcher -> matcher.group("policyName")).orElse(null);
    }
}