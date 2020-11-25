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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClassifierWriter implements CliWriter<Classifier> {

    private static final String MATCH_QOS = "{% if ($aug.qos_group) %}"
            + "{% loop in $aug.qos_group as $qos %}"
            + "{% if ($qos.uint32) %}match qos-group {$qos.uint32}\n{% endif %}"
            + "{% endloop %}"
            + "{% endif %}";

    private static final String MATCH_COS = "{% if ($cos) %}{$cos}\n{% endif %}";

    private static final String MATCH_DSCP = "{% if ($dscp) %}{$dscp}\n{% endif %}";

    private static final String WRITE_ALL_TEMPLATE = "{% if ($conditions) %}"
            + "configure terminal\n"
            + "class-map match-{$class_type} {$class_name}\n"
            + MATCH_QOS
            + MATCH_COS
            + MATCH_DSCP
            + "end{% endif %}";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no class-map %s\n"
            + "end";

    private Cli cli;

    public ClassifierWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Classifier> instanceIdentifier,
                                       @Nonnull Classifier classifier,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String className = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        if (classifier.getTerms() == null || classifier.getTerms().getTerm() == null
                || className.endsWith(ClassifierReader.DEFAULT_CLASS_SUFFIX)) {
            return;
        }
        // first determine class-map type by counting terms
        List<Term> terms = classifier.getTerms().getTerm();
        // even if we have one term, it can be 'match-any', so check the term name as well
        if (terms.size() == 1 && ClassMapType.MATCH_ALL.getStringValue().equals(terms.get(0).getId())) {
            Conditions conditions = terms.get(0).getConditions();
            writeMatch(instanceIdentifier, classifier, className, conditions, ClassMapType.MATCH_ALL);
            return;
        }
        writeMatchAny(instanceIdentifier, classifier, className, terms);
    }

    private void writeMatch(InstanceIdentifier<Classifier> instanceIdentifier,
                            Classifier classifier,
                            String className,
                            Conditions conditions,
                            ClassMapType type) throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, classifier,
                fT(WRITE_ALL_TEMPLATE,
                "class_name", className,
                "class_type", type.getStringValue(),
                "conditions", conditions,
                "aug", conditions.getAugmentation(QosConditionAug.class),
                // cos has separate method, because I was unable to get "inner's" bool value in Chunk
                "cos", getCosCommand(conditions),
                "dscp", getDscpCommand(conditions)));
    }

    private void writeMatchAny(InstanceIdentifier<Classifier> instanceIdentifier,
                               Classifier classifier,
                               String className,
                               List<Term> terms) throws WriteFailedException.CreateFailedException {
        // match-any terms need to be sorted, write term1 condition first, term2 second, etc.
        List<Term> sortedTerms = sortTerms(terms);
        int checkCount = 1;
        for (Term term : sortedTerms) {
            Preconditions.checkArgument(checkCount == Integer.parseInt(term.getId()),
                    "Unexpected term id, expected " + checkCount + " but was " + term.getId());
            if (term.getConditions() == null) {
                return;
            }
            Conditions conditions = term.getConditions();
            // in match-any only one line will be returned
            writeMatch(instanceIdentifier, classifier, className, conditions, ClassMapType.MATCH_ANY);
            checkCount++;
        }
    }

    private List<Term> sortTerms(List<Term> terms) {
        return terms.stream()
                .peek(t -> Preconditions.checkArgument(StringUtils.isNumeric(t.getId()),
                        "Term ID must be a number but was " + t.getId()))
                .sorted(Comparator.comparingInt(t -> Integer.valueOf(t.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Classifier> id,
                                        @Nonnull Classifier dataBefore,
                                        @Nonnull Classifier dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        // delete method has to be there in order to prevent semantic errors.
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Classifier> instanceIdentifier,
                                        @Nonnull Classifier classifier,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String className = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, classifier, f(DELETE_TEMPLATE, className));
    }

    private String getCosCommand(Conditions conditions) {
        QosConditionAug aug = conditions.getAugmentation(QosConditionAug.class);
        if (aug != null && aug.getCos() != null && aug.getCos().getCos() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("match cos");
            if (aug.getCos().isInner()) {
                stringBuilder.append(" inner");
            }
            stringBuilder.append(" ").append(aug.getCos().getCos().getValue().toString());
            return stringBuilder.toString();
        }
        return null;
    }

    private String getDscpCommand(Conditions conditions) {
        if (conditions.getIpv4() != null && conditions.getIpv4().getConfig() != null) {
            final Config config = conditions.getIpv4().getConfig();
            if (config.getDscp() != null) {
                return "match ip dscp " + config.getDscp().getValue().toString();
            } else {
                QosIpv4ConditionAug v4Aug = config.getAugmentation(QosIpv4ConditionAug.class);
                if (v4Aug.getDscpEnum() != null) {
                    return "match ip dscp " + v4Aug.getDscpEnum().getName();
                }
            }
        }
        return null;
    }

}