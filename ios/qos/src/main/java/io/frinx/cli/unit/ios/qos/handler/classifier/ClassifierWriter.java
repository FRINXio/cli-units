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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.CosValue;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.qos.condition.cos.config.cos.CosList;
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

    private static final String MATCH_DSCP = "{% if ($aug.dscp) %}"
            + "{% if ($aug.dscp.dscp_list) %}"
            + "{% loop in $aug.dscp.dscp_list as $dscp_leaf %}"
            + "match ip dscp"
            + "{% if ($dscp_leaf.dscp_value_list) %}"
            + "{% loop in $dscp_leaf.dscp_value_list as $dscp_value_leaf %}"
            + "{% if ($dscp_value_leaf.dscp_enumeration) %}"
            + " {$dscp_value_leaf.dscp_enumeration.name}"
            + "{% endif %}"
            + "{% if ($dscp_value_leaf.dscp_number) %}"
            + " {$dscp_value_leaf.dscp_number.value}"
            + "{% endif %}"
            + "{% endloop %}"
            + "{% endif %}"
            + "\n"
            + "{% endloop %}"
            + "{% endif %}"
            + "{% endif %}";

    private static final String WRITE_ALL_ATTR = "{% if ($conditions) %}"
            + "configure terminal\n"
            + "class-map match-{$classType} {$className}\n"
            + MATCH_QOS
            + MATCH_DSCP
            + "{% if ($cos) %}{$cos}{% endif %}"
            + "end{% endif %}";

    private static final String DELETE_MAP = "configure terminal\n"
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
                fT(WRITE_ALL_ATTR,
                "className", className,
                "classType", type.getStringValue(),
                "conditions", conditions,
                "aug", conditions.getAugmentation(QosConditionAug.class),
                // cos has separate method, because I was unable to get "inner's" bool value in Chunk
                "cos", getCosCommands(conditions)));
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
        blockingWriteAndRead(cli, instanceIdentifier, classifier, f(DELETE_MAP, className));
    }

    private String getCosCommands(Conditions conditions) {
        QosConditionAug aug = conditions.getAugmentation(QosConditionAug.class);
        if (aug != null && aug.getCos() != null && aug.getCos().getCosList() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (CosList cosList : aug.getCos().getCosList()) {
                stringBuilder.append("match cos");
                if (cosList.isInner()) {
                    stringBuilder.append(" inner");
                }
                for (CosValue cosValue : cosList.getCosValueList()) {
                    stringBuilder.append(" ").append(cosValue.getValue());
                }
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        }
        return null;
    }

}