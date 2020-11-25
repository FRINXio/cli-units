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

package io.frinx.cli.unit.ios.qos.handler.classifier;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.qos.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.CosBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConditionsReader implements CliConfigReader<Conditions, ConditionsBuilder> {

    private static final String MATCH = "Match";
    private static final Pattern QOS_LINE = Pattern.compile("Match qos-group (?<qos>.+)");
    private static final Pattern COS_LINE = Pattern.compile("Match cos (?<cos>.+)");

    private Cli cli;

    public ConditionsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Conditions> instanceIdentifier,
                                      @Nonnull ConditionsBuilder conditionsBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        if (name.endsWith(ClassifierReader.DEFAULT_CLASS_SUFFIX)) {
            // we are reading class-default that does not have any matches, skip it
            return;
        }

        final String line = instanceIdentifier.firstKeyOf(Term.class).getId();
        final String output = blockingRead(f(TermReader.SH_TERMS, name), cli, instanceIdentifier, readContext);
        filterParsing(output, line, conditionsBuilder);
    }

    @VisibleForTesting
    public static void filterParsing(String output, String line, ConditionsBuilder conditionsBuilder) {
        output = Util.deleteBrackets(output);
        final ClassMapType type = ClassMapType.parseOutput(line);
        // if we have match-all type, all conditions will be parsed into one term
        if (ClassMapType.MATCH_ALL.equals(type)) {
            parseConditions(output, conditionsBuilder);
        } else {
            // if we have a number as term id, just the condition on the specific line will be parsed
            int skip = Integer.valueOf(line) - 1; // first Match statement is on line 0
            final String optLine = ParsingUtils.NEWLINE.splitAsStream(output)
                    .filter(p -> p.contains(MATCH))
                    .skip(skip)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Term with ID " + line + " does not exist."));
            parseConditions(optLine, conditionsBuilder);
        }
    }

    private static void parseConditions(String output, ConditionsBuilder conditionsBuilder) {
        // although we might have only one line of output, we don't know what type of match we have, try all
        try {
            fillInAug(output, conditionsBuilder);
            Ipv4Util.parseIpv4(output, conditionsBuilder);
        } catch (IllegalArgumentException e) {
            LOG.warn(e.getMessage());
        }
    }

    private static void fillInAug(String output, ConditionsBuilder conditionsBuilder) {
        final QosConditionAugBuilder augBuilder = new QosConditionAugBuilder();
        parseQosGroup(output, augBuilder);
        parseCos(output, augBuilder);

        if (augBuilder.getCos() != null || augBuilder.getQosGroup() != null) {
            conditionsBuilder.addAugmentation(QosConditionAug.class, augBuilder.build());
        }
    }

    private static void parseQosGroup(String output, QosConditionAugBuilder augBuilder) {
        ParsingUtils.parseField(output, 0, QOS_LINE::matcher,
            m -> m.group("qos"),
            s -> fillInQos(s, augBuilder));
    }

    private static void fillInQos(String line, QosConditionAugBuilder augBuilder) {
        // although list is used, only 1 qos-group can be configured on device
        final List<QosGroup> qosGroups = new ArrayList<>();
        qosGroups.add(QosGroupBuilder.getDefaultInstance(line));
        augBuilder.setQosGroup(qosGroups);
    }

    private static void parseCos(String output, QosConditionAugBuilder augBuilder) {
        ParsingUtils.parseField(output, 0, COS_LINE::matcher,
            m -> m.group("cos"),
            s -> fillInCos(s, augBuilder));
    }

    private static void fillInCos(String line, QosConditionAugBuilder augBuilder) {
        final CosBuilder cosBuilder = new CosBuilder();
        cosBuilder.setInner(line.contains("inner"));
        Matcher matcher = Pattern.compile("\\s+\\d").matcher(line);
        while (matcher.find()) {
            if (cosBuilder.getCos() == null) {
                cosBuilder.setCos(Cos.getDefaultInstance(matcher.group().trim()));
            } else {
                throw new IllegalArgumentException("COS line contains more than 1 number value");
            }
        }
        augBuilder.setCos(cosBuilder.build());
    }

}