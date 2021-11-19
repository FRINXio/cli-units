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

package io.frinx.cli.unit.huawei.snmp.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.huawei.snmp.extension.rev211129.huawei.snmp.top.ConfigBuilder;

public class CommunityConfigReaderTest {

    private static final String SNMP_OUTPUT = " snmp-agent local-engineid 21315435DB030425C50419D4\n"
            + " snmp-agent community read achzv%^y*}^kQ:mui12BC1k)-4t$1Fl}\\(_@4\\"
            + "5@'SOKx%iM;B]x`NdJz#D}ka5-MiR]R$dYM'O+8BAJfN{J%^%# acl 2000 \n"
            + " snmp-agent community write %^%#0jwFL\"`KeH('[_<CZ2R0oqa,![(p+M,l5@DCx0`3CDW3HRuitP0ili0"
            + "F[udR2V2:%l_b)Ffawd63&^(*& acl 2000 \n"
            + " snmp-agent sys-info location Mukachevo   \n"
            + " snmp-agent sys-info version v2c          \n"
            + " snmp-agent trap source LoopBack0 \n";

    @Test
    public void testSNMPIds() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        CommunityConfigReader.parseConfigAttributes(SNMP_OUTPUT, configBuilder);
        Assert.assertEquals(new ConfigBuilder().setLocalEngineid("21315435DB030425C50419D4")
                .setReadCommunityPassword(new EncryptedPassword(new PlainString(
                        "achzv%^y*}^kQ:mui12BC1k)-4t$1Fl}\\(_@4\\5@'SOKx%iM;B]x`NdJz#D}ka5-MiR]R$dYM'O+8BAJfN{J%^%#")))
                .setWriteCommunityPassword(new EncryptedPassword(new PlainString(
                        "%^%#0jwFL\"`KeH('[_<CZ2R0oqa,![(p+M,l5@DCx0`3CDW3HRuitP0ili0F[udR2V2:%l_b)Ffawd63&^(*&")))
                .setCommunityLocation("Mukachevo")
                .build(), configBuilder.build());
    }
}