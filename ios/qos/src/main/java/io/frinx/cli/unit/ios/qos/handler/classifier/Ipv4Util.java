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

import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Dscp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;

public final class Ipv4Util {

    private static final Pattern DSCP_LINE = Pattern.compile("Match ip  dscp (?<dscp>.+)");

    private Ipv4Util() {

    }

    public static void parseIpv4(String output, ConditionsBuilder builder) {
        Ipv4Builder v4Builder = new Ipv4Builder();
        ConfigBuilder configBuilder = new ConfigBuilder();
        QosIpv4ConditionAugBuilder augBuilder = new QosIpv4ConditionAugBuilder();

        parseDscp(output, augBuilder, configBuilder);

        // don't set containers if there is nothing in it
        if (configBuilder.getDscp() == null && augBuilder.getDscpEnum() == null) {
            return;
        } else if (augBuilder.getDscpEnum() != null) {
            configBuilder.addAugmentation(QosIpv4ConditionAug.class, augBuilder.build());
        }

        v4Builder.setConfig(configBuilder.build());
        builder.setIpv4(v4Builder.build());
    }

    private static void parseDscp(String output, QosIpv4ConditionAugBuilder augBuilder, ConfigBuilder configBuilder) {
        output = output.trim();

        String dscpStringValue = ParsingUtils.parseField(output, 0, DSCP_LINE::matcher,
            matcher -> matcher.group("dscp")).orElse(null);

        if (dscpStringValue != null) {
            if (dscpStringValue.contains(" ")) {
                throw new IllegalArgumentException("DSCP line contains more than 1 value");
            }

            Dscp dscp = DscpBuilder.getDefaultInstance(dscpStringValue);
            if (dscp.getDscp() != null) {
                configBuilder.setDscp(dscp.getDscp());
            } else if (dscp.getDscpEnumeration() != null) {
                augBuilder.setDscpEnum(dscp.getDscpEnumeration());
            }
        }
    }

}
