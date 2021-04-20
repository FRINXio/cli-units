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

package io.frinx.cli.unit.ios.routing.policy.handlers.prefix;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;

public class PrefixConfigWriterTest implements CliFormatter {

    private static final String WRITE_INPUT_PERMIT = "configure terminal\n"
            + "ip prefix-list NAME seq 5 permit 0.0.0.0/24\n"
            + "end\n";

    private static final String WRITE_INPUT_DENY = "configure terminal\n"
            + "ip prefix-list NAME seq 5 deny 0.0.0.0/24\n"
            + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "no ip prefix-list NAME seq 5 permit 0.0.0.0/24\n"
            + "end\n";

    @Test
    public void writeTest() {
        Assert.assertEquals(WRITE_INPUT_PERMIT, configWithPermit(PrefixConfigWriter.WRITE_TEMPLATE));
        Assert.assertEquals(WRITE_INPUT_DENY, configWithDeny(PrefixConfigWriter.WRITE_TEMPLATE));
    }

    @Test
    public void deleteTest() {
        Assert.assertEquals(DELETE_INPUT, configWithPermit(PrefixConfigWriter.DELETE_TEMPLATE));
    }

    private String configWithDeny(String template) {
        Config config = new ConfigBuilder()
            .setIpPrefix(new IpPrefix(new Ipv4Prefix("0.0.0.0/24")))
            .setMasklengthRange("exact")
            .addAugmentation(PrefixConfigAug.class,
                new PrefixConfigAugBuilder()
                        .setSequenceId(5L)
                        .setOperation(DENY.class)
                        .build())
            .build();
        return writeTemplate(config, template);
    }

    private String configWithPermit(String template) {
        Config config = new ConfigBuilder()
            .setIpPrefix(new IpPrefix(new Ipv4Prefix("0.0.0.0/24")))
            .setMasklengthRange("exact")
            .addAugmentation(PrefixConfigAug.class,
                new PrefixConfigAugBuilder()
                        .setSequenceId(5L)
                        .setOperation(PERMIT.class)
                        .build())
            .build();
        return writeTemplate(config, template);
    }

    private String writeTemplate(Config config, String template) {
        return fT(template,
            "name", "NAME",
            "sequenceId", config.getAugmentation(PrefixConfigAug.class).getSequenceId(),
            "operation", PrefixConfigWriter.getOperationName(config),
            "network", config.getIpPrefix().getIpv4Prefix().getValue());
    }
}
