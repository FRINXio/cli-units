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

import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;

final class Ipv4Util {

    private static final Pattern ACL_LINE = Pattern.compile("match access-group ipv4 (?<acl>.+)");

    private static final Pattern PREC_LINE = Pattern.compile("match precedence ipv4 (?<prec>.+)");

    private Ipv4Util() {

    }

    static void parseIpv4(String output, ConditionsBuilder builder) {

        QosIpv4ConditionAugBuilder augBuilder = new QosIpv4ConditionAugBuilder();

        parseAcl(output, augBuilder);
        parsePrecedence(output, augBuilder);

        // don't set container if there is nothing in it
        if (augBuilder.getAclRef() == null && augBuilder.getPrecedences() == null) {
            return;
        }

        Ipv4Builder v4Builder = new Ipv4Builder();
        ConfigBuilder configBuilder = new ConfigBuilder();

        configBuilder.addAugmentation(QosIpv4ConditionAug.class, augBuilder.build());
        v4Builder.setConfig(configBuilder.build());
        builder.setIpv4(v4Builder.build());
    }

    private static void parseAcl(String output, QosIpv4ConditionAugBuilder augBuilder) {
        ParsingUtils.parseField(output, ACL_LINE::matcher,
            matcher -> matcher.group("acl"),
                augBuilder::setAclRef);
    }

    private static void parsePrecedence(String output, QosIpv4ConditionAugBuilder augBuilder) {
        ParsingUtils.parseField(output, 0, PREC_LINE::matcher,
            matcher -> matcher.group("prec"),
            a -> augBuilder.setPrecedences(ConditionsReader.parsePrecedence(a)));
    }
}
