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
package io.frinx.cli.unit.huawei.qos.handler.classifier;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosClassifierConfig.Operation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosClassifierAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.VrpQosClassifierAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.classifier.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.classifier.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClassifierConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_CL_CONFIG = "display traffic classifier user-defined %s";
    private static final Pattern SH_CL_OPERATION = Pattern.compile("Operator: (?<operation>\\w+)");
    private static final Pattern IF_MATCH_RULE_AND_EX =
            Pattern.compile("Rule\\(.\\) : if-match (?<rule>\\w+) (?<extension>.+)");
    private static final Pattern SINGLE_RULE = Pattern.compile("Rule\\(.\\) : if-match (?<singlerule>\\w+)");

    private Cli cli;

    public ClassifierConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        final String showCommand = String.format(SH_CL_CONFIG, name);
        parseRuleAndRuleId(blockingRead(showCommand, cli, instanceIdentifier, readContext), configBuilder);
        configBuilder.setName(name);
    }

    @VisibleForTesting
    static void parseRuleAndRuleId(String output, ConfigBuilder config) {
        VrpQosClassifierAugBuilder vrpQCAB = new VrpQosClassifierAugBuilder();

        ParsingUtils.parseField(output, 0,
                SH_CL_OPERATION::matcher,
                matcher -> matcher.group("operation"),
                v -> vrpQCAB.setOperation(Operation.valueOf(v)));

        ParsingUtils.parseField(output, 0,
                SINGLE_RULE::matcher,
                matcher -> matcher.group("singlerule"),
                vrpQCAB::setClassifierRule);

        ParsingUtils.parseField(output, 0,
                IF_MATCH_RULE_AND_EX::matcher,
                matcher -> matcher.group("rule"),
                vrpQCAB::setClassifierRule);

        ParsingUtils.parseField(output, 0,
                IF_MATCH_RULE_AND_EX::matcher,
                matcher -> matcher.group("extension"),
                vrpQCAB::setRuleSetting);

        config.addAugmentation(VrpQosClassifierAug.class, vrpQCAB.build());
    }
}