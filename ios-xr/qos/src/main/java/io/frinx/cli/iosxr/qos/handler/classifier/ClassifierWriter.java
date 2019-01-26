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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv6ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.Term;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Conditions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.top.classifiers.Classifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ClassifierWriter implements CliWriter<Classifier> {

    private Cli cli;

    public ClassifierWriter(Cli cli) {
        this.cli = cli;
    }

    public static final String LIST_PREC = "{% if ($prec.uint8) %}{$prec.uint8}{% endif %}"
            + "{% if ($prec.string) %}{$prec.string}{% endif %}"
            + "{% divider %}"
            + " "
            + "{% endloop %}\n";

    public static final String LIST_QOS = "{% if ($qos.uint32) %}{$qos.uint32}{% endif %}"
            + "{% if ($qos.qos_group_range) %}{$qos.qos_group_range.value|s/\\.\\./-/g}{% endif %}"
            + "{% divider %}"
            + " "
            + "{% endloop %}\n";

    private static final String MATCH_QOS_T = "{% if ($aug.qos_group) %}"
            + "match qos-group {% loop in $aug.qos_group as $qos %}"
            + LIST_QOS
            + "{% endif %}";

    private static final String MATCH_PREC_T = "{% if ($aug.precedences) %}"
            + "match precedence {% loop in $aug.precedences as $prec %}"
            + LIST_PREC
            + "{% endif %}";

    private static final String MATCH_MPLS_T = "{% if ($conditions.mpls) %}"
            + "match mpls experimental topmost{% loop in $conditions.mpls.config.traffic_class as $val %} {$val}"
            + "{% endloop %}\n{% endif %}";

    private static final String MATCH_IPV4_ACL_T = "{% if ($v4Aug) %}"
            + "{% if ($v4Aug.acl_ref) %}match access-group ipv4 {$v4Aug.acl_ref}\n{% endif %}{% endif %}";

    private static final String MATCH_IPV6_ACL_T = "{% if ($v6Aug) %}"
            + "{% if ($v6Aug.acl_ref) %}match access-group ipv6 {$v6Aug.acl_ref}\n{% endif %}{% endif %}";

    private static final String MATCH_IPV4_PREC_T = "{% if ($v4Aug.precedences) %}"
            + "match precedence ipv4 {% loop in $v4Aug.precedences as $prec %}"
            + LIST_PREC
            + "{% endif %}";

    private static final String MATCH_IPV6_PREC_T = "{% if ($v6Aug.precedences) %}"
            + "match precedence ipv6 {% loop in $v6Aug.precedences as $prec %}"
            + LIST_PREC
            + "{% endif %}";

    private static final String WRITE_ALL_ATTR = "{% if ($conditions) %}"
            + "class-map match-{$classType} {$className}\n"
            + MATCH_QOS_T
            + MATCH_PREC_T
            + MATCH_MPLS_T
            + MATCH_IPV4_ACL_T
            + MATCH_IPV6_ACL_T
            + MATCH_IPV4_PREC_T
            + MATCH_IPV6_PREC_T
            + "root{% endif %}";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Classifier> instanceIdentifier, @Nonnull
            Classifier classifier, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String className = instanceIdentifier.firstKeyOf(Classifier.class)
                .getName();
        if (classifier.getTerms() == null || classifier.getTerms()
                .getTerm() == null || className.endsWith(ClassifierReader.DEFAULT_CLASS_SUFFIX)) {
            return;
        }
        // first determine class-map type by counting terms
        List<Term> terms = classifier.getTerms()
                .getTerm();
        // even if we have one term, it can be 'match-any', so check the term name as well
        if (terms.size() == 1 && ClassMapType.MATCH_ALL.getStringValue()
                .equals(terms.get(0)
                        .getId())) {
            Conditions conditions = terms.get(0)
                    .getConditions();
            writeMatch(instanceIdentifier, classifier, className, conditions, ClassMapType.MATCH_ALL);
            return;
        }
        writeMatchAny(instanceIdentifier, classifier, className, terms);
    }

    private void writeMatch(InstanceIdentifier<Classifier> instanceIdentifier, Classifier classifier, String
            className, Conditions conditions, ClassMapType type) throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, classifier, fT(WRITE_ALL_ATTR,
                "className", className,
                "classType", type.getStringValue(),
                "conditions", conditions,
                "aug", conditions.getAugmentation(QosConditionAug.class),
                "v4Aug", findIpv4Aug(conditions),
                "v6Aug", findIpv6Aug(conditions)));
    }

    private void writeMatchAny(InstanceIdentifier<Classifier> instanceIdentifier, Classifier classifier, String
            className, List<Term> terms) throws WriteFailedException.CreateFailedException {
        // match-any terms need to be sorted, write term1 condition first, term2 second, etc.
        List<Term> sortedTerms = sortTerms(terms);
        int checkCount = 1;
        for (Term t : sortedTerms) {
            Preconditions.checkArgument(checkCount == Integer.valueOf(t.getId()),
                    "Unexpected term id, expected " + checkCount + " but was " + t.getId());
            if (t.getConditions() == null) {
                return;
            }
            Conditions conditions = t.getConditions();
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

    private QosIpv4ConditionAug findIpv4Aug(Conditions conditions) {
        if (conditions.getIpv4() != null) {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                    .top.ipv4.Config v4Config =
                    conditions.getIpv4()
                            .getConfig();
            return v4Config.getAugmentation(QosIpv4ConditionAug.class);
        }
        return null;
    }

    private QosIpv6ConditionAug findIpv6Aug(Conditions conditions) {
        if (conditions.getIpv6() != null) {
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv6.protocol.fields
                    .top.ipv6.Config v6Config =
                    conditions.getIpv6()
                            .getConfig();
            return v6Config.getAugmentation(QosIpv6ConditionAug.class);
        }
        return null;
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Classifier> id, @Nonnull Classifier dataBefore,
                                        @Nonnull Classifier dataAfter, @Nonnull WriteContext writeContext) throws
            WriteFailedException {
        //delete method has to be there in order to prevent semantic errors.
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Classifier> instanceIdentifier, @Nonnull
            Classifier classifier, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String className = instanceIdentifier.firstKeyOf(Classifier.class)
                .getName();
        blockingWriteAndRead(cli, instanceIdentifier, classifier,
                f("no class-map %s", className));
    }
}
