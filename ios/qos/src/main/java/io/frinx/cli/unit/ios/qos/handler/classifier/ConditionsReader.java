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
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.CosValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpValueBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.CosBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.cos.CosList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.cos.CosListBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.dscp.config.Dscp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.dscp.config.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.dscp.config.dscp.DscpList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.dscp.config.dscp.DscpListBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConditionsReader implements CliConfigReader<Conditions, ConditionsBuilder> {

    private static final String MATCH = "Match";

    private static final Pattern QOS_LINE = Pattern.compile("Match qos-group (?<qos>.+)");

    private static final Pattern DSCP_LINE = Pattern.compile("Match ip  dscp (?<dscp>.+)");
    private static final Pattern DSCP_VALUE_LINE =
            Pattern.compile("^([1-5]?[0-9]|6[0-3]|af[1-4][1-3]|cs[1-7]|default|ef)$", Pattern.MULTILINE);

    private static final Pattern COS_LINE = Pattern.compile("Match cos (?<cos>.+)");
    private static final Pattern COS_VALUE_LINE = Pattern.compile("^[0-7]$", Pattern.MULTILINE);

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
        filterParsing(Util.deleteBrackets(output), line, conditionsBuilder);
    }

    @VisibleForTesting
    public static void filterParsing(String output, String line, ConditionsBuilder conditionsBuilder) {
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
        final QosConditionAugBuilder augBuilder = new QosConditionAugBuilder();
        augBuilder.setQosGroup(parseQosGroups(output));
        augBuilder.setDscp(parseDscp(output));
        augBuilder.setCos(parseCos(output));
        conditionsBuilder.addAugmentation(QosConditionAug.class, augBuilder.build());
    }

    public static List<QosGroup> parseQosGroups(String output) {
        final List<QosGroup> qosGroups = new ArrayList<>();
        ParsingUtils.parseFields(output, 0, QOS_LINE::matcher,
            m -> m.group("qos"),
            s -> qosGroups.add(QosGroupBuilder.getDefaultInstance(s)));
        return qosGroups.size() > 0 ? qosGroups : null;
    }

    public static Dscp parseDscp(String output) {
        final DscpBuilder dscpBuilder = new DscpBuilder();
        dscpBuilder.setDscpList(parseDscpOutput(output));
        return dscpBuilder.getDscpList().size() > 0 ? dscpBuilder.build() : null;
    }

    private static List<DscpList> parseDscpOutput(String output) {
        final List<DscpList> dscpLists = new ArrayList<>();
        ParsingUtils.parseFields(output, 0, DSCP_LINE::matcher,
            m -> m.group("dscp"),
            s -> dscpLists.add(parseDscpLine(s)));
        return dscpLists;
    }

    private static DscpList parseDscpLine(String line) {
        final DscpListBuilder dscpListBuilder = new DscpListBuilder();
        dscpListBuilder.setDscpValueList(parseDscpValues(line));
        return dscpListBuilder.build();
    }

    private static List<DscpValue> parseDscpValues(String line) {
        final List<DscpValue> dscpValues = new ArrayList<>();
        ParsingUtils.parseFields(line.replace("  ", "\n"), 0, DSCP_VALUE_LINE::matcher,
            m -> m.group(),
            s -> dscpValues.add(DscpValueBuilder.getDefaultInstance(s)));
        return dscpValues;
    }

    public static Cos parseCos(String output) {
        final CosBuilder cosBuilder = new CosBuilder();
        cosBuilder.setCosList(parseCosOutput(output));
        return cosBuilder.getCosList().size() > 0 ? cosBuilder.build() : null;
    }

    private static List<CosList> parseCosOutput(String output) {
        final List<CosList> cosLists = new ArrayList<>();
        ParsingUtils.parseFields(output, 0, COS_LINE::matcher,
            m -> m.group("cos"),
            s -> cosLists.add(parseCosLine(s)));
        return cosLists;
    }

    private static CosList parseCosLine(String line) {
        final CosListBuilder cosListBuilder = new CosListBuilder();
        cosListBuilder.setInner(line.contains("inner"));
        cosListBuilder.setCosValueList(parseCosValues(line));
        return cosListBuilder.build();
    }

    private static List<CosValue> parseCosValues(String line) {
        final List<CosValue> cosValues = new ArrayList<>();
        ParsingUtils.parseFields(line.replace("  ", "\n"), 0, COS_VALUE_LINE::matcher,
            m -> m.group(),
            s -> cosValues.add(CosValue.getDefaultInstance(s)));
        return cosValues;
    }

}