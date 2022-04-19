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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.access.group.config.AccessGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.access.group.config.access.group.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.access.group.config.access.group.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.access.group.config.access.group.acl.sets.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.MultipleCosBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.multiple.cos.CosSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.multiple.cos.CosSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.multiple.cos.cos.sets.ElementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.multiple.cos.cos.sets.elements.Element;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.multiple.cos.config.multiple.cos.cos.sets.elements.ElementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConditionsReader implements CliConfigReader<Conditions, ConditionsBuilder> {

    private static final String MATCH = "Match";
    private static final Pattern QOS_LINE = Pattern.compile("Match qos-group (?<qos>.+)");
    private static final Pattern COS_LINE = Pattern.compile("Match cos (?<cos>.+)");
    private static final Pattern ACL_LINE = Pattern.compile("Match access-group name (?<acl>.*)");

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
        parseAccess(output, augBuilder);

        if (augBuilder.getCos() != null || augBuilder.getQosGroup() != null) {
            conditionsBuilder.addAugmentation(QosConditionAug.class, augBuilder.build());
        }
        if (augBuilder.getMultipleCos() != null) {
            conditionsBuilder.addAugmentation(QosConditionAug.class, augBuilder.build());
        }
        if (augBuilder.getAccessGroup() != null) {
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
        final MultipleCosBuilder cosBuilder = new MultipleCosBuilder();
        Matcher matcher = COS_LINE.matcher(output);
        final List<CosSets> list = new ArrayList<>();
        int id = 1;
        boolean isCos = false;
        while (matcher.find()) {
            isCos = true;
            final CosSetsBuilder cosSetsBuilder = new CosSetsBuilder();
            fillInCos(matcher.group("cos"), cosSetsBuilder);
            cosSetsBuilder.setId(id);
            id++;
            list.add(cosSetsBuilder.build());
        }
        cosBuilder.setCosSets(list);
        if (isCos) {
            augBuilder.setMultipleCos(cosBuilder.build());
        }
    }

    private static void fillInCos(String line, CosSetsBuilder cosSetsBuilder) {
        final ElementsBuilder cosElementsBuilder = new ElementsBuilder();
        cosElementsBuilder.setInner(line.contains("inner"));
        Matcher matcher = Pattern.compile("\\s+\\d").matcher(line);
        final List<Element> list = new ArrayList<>();
        while (matcher.find()) {
            final ElementBuilder element = new ElementBuilder();
            element.setId(Cos.getDefaultInstance(matcher.group().trim()));
            list.add(element.build());
        }
        cosElementsBuilder.setElement(list);
        cosSetsBuilder.setElements(cosElementsBuilder.build());
    }

    private static void parseAccess(String output, QosConditionAugBuilder augBuilder) {
        final AccessGroupBuilder accessGroupBuilder = new AccessGroupBuilder();
        Matcher matcher = ACL_LINE.matcher(output);
        final List<AclSets> list = new ArrayList<>();
        int id = 1;
        boolean isAcl = false;
        while (matcher.find()) {
            isAcl = true;
            final AclSetsBuilder aclSetsBuilder = new AclSetsBuilder();
            fillInAccess(matcher.group("acl"), aclSetsBuilder);
            aclSetsBuilder.setId(id);
            id++;
            list.add(aclSetsBuilder.build());
        }
        accessGroupBuilder.setAclSets(list);
        if (isAcl) {
            augBuilder.setAccessGroup(accessGroupBuilder.build());
        }
    }

    private static void fillInAccess(String line, AclSetsBuilder aclSetsBuilder) {
        final ConfigBuilder aclBuilder = new ConfigBuilder();
        aclBuilder.setName(line);
        aclSetsBuilder.setConfig(aclBuilder.build());
    }

}