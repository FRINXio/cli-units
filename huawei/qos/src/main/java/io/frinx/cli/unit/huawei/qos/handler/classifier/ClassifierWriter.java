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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClassifierWriter implements CliWriter<Classifier> {

    private static final String MATCH_DSCP = "{% if ($dscp) %}{$dscp}\n{% endif %}";


    private static final String WRITE_ALL_TEMPLATE = "system-view\n"
            + "traffic classifier {$class_name} operator or\n"
            + MATCH_DSCP
            + "return";

    private static final String DELETE_TEMPLATE = """
            system-view
            undo traffic classifier %s
            return""";


    private Cli cli;

    public ClassifierWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Classifier> instanceIdentifier,
                                       @NotNull Classifier classifier,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
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
            writeMatch(instanceIdentifier, classifier, className,
                    Collections.singletonList(conditions), ClassMapType.MATCH_ALL);
            return;
        }
        writeMatchAny(instanceIdentifier, classifier, className, terms);
    }

    private void writeMatch(InstanceIdentifier<Classifier> instanceIdentifier,
                            Classifier classifier,
                            String className,
                            List<Conditions> dscps,
                            ClassMapType type) throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, classifier,
                fT(WRITE_ALL_TEMPLATE,
                        "class_name", className,
                        "class_type", type.getStringValue(),
                        "dscp", getDscpCommand(dscps)));
    }

    private void writeMatchAny(InstanceIdentifier<Classifier> instanceIdentifier,
                               Classifier classifier,
                               String className,
                               List<Term> terms) throws WriteFailedException.CreateFailedException {
        // match-any terms need to be sorted, write term1 condition first, term2 second, etc.
        List<Term> sortedTerms = sortTerms(terms);
        List<Conditions> dscps = new ArrayList<>();
        int checkCount = 1;
        for (Term term : sortedTerms) {
            Preconditions.checkArgument(checkCount == Integer.parseInt(term.getId()),
                    "Unexpected term id, expected " + checkCount + " but was " + term.getId());
            if (term.getConditions() == null) {
                return;
            }
            Conditions conditions = term.getConditions();
            dscps.add(conditions);
            // in match-any only one line will be returned

            checkCount++;
        }
        writeMatch(instanceIdentifier, classifier, className, dscps, ClassMapType.MATCH_ANY);
    }

    private List<Term> sortTerms(List<Term> terms) {
        return terms.stream()
                .peek(t -> Preconditions.checkArgument(StringUtils.isNumeric(t.getId()),
                        "Term ID must be a number but was " + t.getId()))
                .sorted(Comparator.comparingInt(t -> Integer.parseInt(t.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Classifier> id,
                                        @NotNull Classifier dataBefore,
                                        @NotNull Classifier dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // delete method has to be there in order to prevent semantic errors.
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Classifier> instanceIdentifier,
                                        @NotNull Classifier classifier,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String className = instanceIdentifier.firstKeyOf(Classifier.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, classifier, f(DELETE_TEMPLATE, className));
    }

    private String getDscpCommand(List<Conditions> conditions) {
        String dscpCommand = "if-match dscp";
        for (Conditions condition: conditions) {
            if (condition.getIpv4() != null && condition.getIpv4().getConfig() != null) {
                final Config config = condition.getIpv4().getConfig();
                if (config.getDscp() != null) {
                    dscpCommand += " " + config.getDscp().getValue().toString();
                } else {
                    QosIpv4ConditionAug v4Aug = config.getAugmentation(QosIpv4ConditionAug.class);
                    if (v4Aug.getDscpEnum() != null) {
                        dscpCommand += " " + v4Aug.getDscpEnum().getName();
                    }
                }
            }
        }
        return dscpCommand;
    }
}