/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.huawei.conf.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadataBuilder;

class ConfigMetadataReaderTest {

    private static final String OUTPUT = """
            [V200R009C00SPC500]
            #
             sysname NOKIA_BSOD_TEST_CPE
             header login information "
            #                                        \s
             drop illegal-mac alarm
            #
             factory-configuration prohibit
            #
             clock timezone CEST add 01:00:00
             clock daylight-saving-time CEST repeating 2:0 last Sunday March 3:0 last Sunday October 01:00 2018 2025
            #
            ipv6
            #
            ipv6 icmp-error ratelimit 2147483647
            #
            vlan batch 101 110 112 120 130 140 400 911
            #
            authentication-profile name default_authen_profile
            authentication-profile name dot1x_authen_profile
            authentication-profile name mac_authen_profile
            authentication-profile name portal_authen_profile
            authentication-profile name dot1xmac_authen_profile
            authentication-profile name multi_authen_profile
            #
             icmp rate-limit enable
             icmp rate-limit threshold 32768
            #
            dhcp enable                              \s
            #
            return""";

    private static final String FINGERPRINT = DigestUtils.md5Hex(OUTPUT);

    @Test
    void testParse() {
        var builder = new ConfigurationMetadataBuilder();
        var expected = new ConfigurationMetadataBuilder();
        builder.setLastConfigurationFingerprint(FINGERPRINT);
        ConfigMetadataReader.parseFingerprint(OUTPUT, expected);
        assertEquals(expected.build(), builder.build());
    }
}
