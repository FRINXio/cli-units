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

package io.frinx.cli.iosxr.qos.handler.classifier;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.mpls.header.top.MplsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.mpls.header.top.mpls.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Precedence;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.TermBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConditionsReader implements CliConfigReader<Conditions, ConditionsBuilder> {

    private static final Pattern QOS_LINE = Pattern.compile("match qos-group (?<qos>.+)");
    private static final Pattern MPLS_LINE = Pattern.compile("match mpls experimental topmost (?<mpls>.+)");
    private static final Pattern PREC_LINE = Pattern.compile("match precedence(?! ipv4| ipv6) (?<prec>.+)");
    private static final String MATCH = "match";

    private Cli cli;

    public ConditionsReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConditionsBuilder getBuilder(@Nonnull InstanceIdentifier<Conditions> instanceIdentifier) {
        return new ConditionsBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Conditions> instanceIdentifier, @Nonnull ConditionsBuilder conditionsBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        if (name.endsWith(ClassifierReader.DEFAULT_CLASS_SUFFIX)) {
            // we are reading class-default that does not have any matches, skip it
            return;
        }
        String line = instanceIdentifier.firstKeyOf(Term.class).getId();
        String output = blockingRead(f(TermReader.SH_TERMS_ALL, name), cli, instanceIdentifier, readContext);

        if (output.equals("")) {
            output = blockingRead(f(TermReader.SH_TERMS_ANY, name), cli, instanceIdentifier, readContext);
        }
        filterParsing(output, line, conditionsBuilder);
    }

    @VisibleForTesting
    public static void filterParsing(String output, String line, ConditionsBuilder conditionsBuilder) {
        ClassMapType type = ClassMapType.parseOutput(line);
        // if we have match-all type, all conditions will be parsed into one term
        if (ClassMapType.MATCH_ALL.equals(type)) {
            parseConditions(output, conditionsBuilder);
        } else {
            // if we have a number as term id, just the condition on the specific line will be parsed
            // skip class-map def
            int skip = Integer.valueOf(line);
            final String optLine = ParsingUtils.NEWLINE.splitAsStream(output).filter(p -> p.contains(MATCH)).skip(skip)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Term with ID " + line + " does not exist."));
            parseConditions(optLine, conditionsBuilder);
        }
    }

    private static void parseConditions(String output, ConditionsBuilder conditionsBuilder) {
        // although we might have only one line of output, we don't know what type of match we have, try all
        QosConditionAugBuilder augBuilder = new QosConditionAugBuilder();
        parseQos(output, augBuilder);
        parsePrecedence(output, augBuilder);
        conditionsBuilder.addAugmentation(QosConditionAug.class, augBuilder.build());
        Ipv4Util.parseIpv4(output, conditionsBuilder);
        Ipv6Util.parseIpv6(output, conditionsBuilder);
        parseMpls(output, conditionsBuilder);
    }

    private static void parseQos(String output, QosConditionAugBuilder augBuilder) {
        ParsingUtils.parseField(output, QOS_LINE::matcher,
            matcher -> matcher.group("qos"),
            v -> augBuilder.setQosGroup(Integer.valueOf(v)));
    }

    private static void parseMpls(String output, ConditionsBuilder conditionsBuilder) {
        MplsBuilder builder = new MplsBuilder();
        ConfigBuilder mplsCfgBuilder = new ConfigBuilder();
        ParsingUtils.parseField(output, MPLS_LINE::matcher,
            matcher -> matcher.group("mpls"),
            v -> mplsCfgBuilder.setTrafficClass(Short.valueOf(v)));

        if (mplsCfgBuilder.getTrafficClass() != null) {
            builder.setConfig(mplsCfgBuilder.build());
            conditionsBuilder.setMpls(builder.build());
        }
    }

    private static void parsePrecedence(String output, QosConditionAugBuilder augBuilder) {
        ParsingUtils.parseField(output, 0, PREC_LINE::matcher,
                matcher -> matcher.group("prec"), p -> augBuilder.setPrecedences(parsePrecedence(p)));
    }

    public static List<Precedence> parsePrecedence(String fullPrecs) {
        String[] precs = fullPrecs.split(" ");
        List<Precedence> parsedPrecs = new ArrayList<>();
        for (String prec : precs) {
            if (StringUtils.isNumeric(prec)) {
                parsedPrecs.add(Precedence.forValue(Integer.valueOf(prec.trim())));
            } else {
                parsedPrecs.add(precedenceForValue(prec.trim()));
            }
        }
        return parsedPrecs;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Conditions conditions) {
        ((TermBuilder) builder).setConditions(conditions);
    }

    private static Precedence precedenceForValue(String name) {
        switch(name) {
            case "routine":
                return Precedence.Routine;
            case "priority":
                return Precedence.Priority;
            case "immediate":
                return Precedence.Immediate;
            case "flash":
                return Precedence.Flash;
            case "flash-override":
                return Precedence.FlashOverride;
            case "critical":
                return Precedence.Critical;
            case "internet":
                return Precedence.Internet;
            case "network":
                return Precedence.Network;
            default:
                throw new IllegalArgumentException("Unknown precedence " + name);
        }
    }
}
