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
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosRemarkQosGroupAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosRemarkQosGroupAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.actions.RemarkBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.common.remark.actions.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.common.remark.actions.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RemarkConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_POLICY_MAPS = "show running-config policy-map %s | include set";

    private static final Pattern MPLS_LINE = Pattern.compile("set mpls experimental topmost (?<mpls>.+)");
    private static final Pattern QOS_LINE = Pattern.compile("set qos-group (?<qos>.+)");
    private static final Pattern PREC_LINE = Pattern.compile("set precedence (?<prec>.+)");

    private Cli cli;

    public RemarkConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.actions.Config targetGroup =
                readContext.read(RWUtils.cutId(instanceIdentifier, Actions.class)
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.actions.Config.class)).orNull();
        if (targetGroup == null) {
            // no policy-map / class-map relationship is specified, no remarks to be filled in
            return;
        }
        String policyName = targetGroup.getTargetGroup();
        String output = blockingRead(f(SH_POLICY_MAPS, policyName), cli, instanceIdentifier, readContext);
        parseRemarks(output, configBuilder);
    }

    @VisibleForTesting
    public static void parseRemarks(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, MPLS_LINE::matcher,
            matcher -> matcher.group("mpls"),
            v -> configBuilder.setSetMplsTc(Short.valueOf(v)));
        parseQos(output, configBuilder);
    }

    private static void parseQos(String output, ConfigBuilder configBuilder) {
        QosRemarkQosGroupAugBuilder augBuilder = new QosRemarkQosGroupAugBuilder();
        ParsingUtils.parseField(output, QOS_LINE::matcher,
            matcher -> matcher.group("qos"),
            v -> augBuilder.setSetQosGroup(Integer.valueOf(v)));

        ParsingUtils.parseField(output, PREC_LINE::matcher,
            matcher -> matcher.group("prec"),
            v -> augBuilder.setSetPrecedences(ConditionsReader.parsePrecedence(v)));

        // don't set the builder if we didn't find a match
        if (augBuilder.getSetQosGroup() == null && augBuilder.getSetPrecedences() == null) {
            return;
        }
        configBuilder.addAugmentation(QosRemarkQosGroupAug.class, augBuilder.build());
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((RemarkBuilder) builder).setConfig(config);
    }
}
