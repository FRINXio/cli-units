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

package io.frinx.cli.unit.cer.cable.handler.cablemac;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.CableMacKey;

class CableMacReaderTest {

    private static final String SH_CABLE_MAC = """
            Feb 12 23:16:26

            Interface                                                                                           \s
            (DS-US)                                          DOC                                                \s
            S/C/CH-S/CG/CH          Mac   Bonded State       SIS  Qos(DS-US)     CPE  MAC address     IP Address\s
            ----------------------- ----- ------ ----------- --- --------------- ---  --------------- -----------
            11/scq/0-2/scq/0        127    -     Ranged      1.0        -          0  1c3a.ded0.5761  -         \s
            11/scq/0-2/scq/1        127    -     Ranged      1.0        -          0  1c3a.ded4.fec9  -         \s
            11/scq/0-2/scq/3        127    -     Ranged      1.0        -          0  5467.51f1.d20a  -         \s
            11/scq/16-2/scq/0       127    -     Ranged      1.0        -          0  e448.c757.04bc  -         \s
            11/scq/31-2/scq/2       127    -     Ranged      1.0        -          0  fc8e.7efd.62d5  -         \s
            11/scq/66-2/scq/21      128    -     Ranged      1.0        -          0  0023.be8c.5c76  -         \s
            11/scq/81-2/scq/22      128    -     Ranged      1.0        -          0  105f.497a.bcc8  -         \s
            11/scq/66-2/scq/22      128    -     Ranged      1.0        -          0  38c8.5c02.9334  -         \s
            11/scq/66-2/scq/23      128    -     Offline     1.0        -          0  5039.5597.7ab8  -         \s
            11/scq/58-2/scq/22      128    -     Ranged      1.0        -          0  5039.5597.7b3c  -         \s
            11/scq/81-2/scq/21      128    -     Ranged      1.0        -          0  c0c6.8754.176d  -         \s
            11/scq/81-2/scq/22      128    -     Offline     3.1        -          0  d46a.6afc.ff33  -         \s
            11/ofd/2-2/scq/20       128    -     Ranged      3.1        -          0  d80f.999e.e84d  -         \s
            11/scq/66-2/scq/21      128    32x5  Operational 3.1    500K/500K      0  d80f.999e.e889  10.255.90.5
            11/scq/66-2/scq/20      128    -     Ranged      1.0        -          0  e448.c756.f98e  -         \s
            11/scq/50-2/scq/23      128    -     Ranged      1.0        -          0  e448.c756.fa0c  -         \s
            11/scq/58-2/scq/21      128    -     Ranged      1.0        -          0  e448.c769.4632  -         \s
            11/scq/81-2/scq/20      128    8x4   Operational 3.0    500K/500K      0  e448.c7bf.f49e  10.255.90.1
            11/scq/81-2/scq/21      128    -     Ranged      3.1        -          0  e457.4007.bcc3  -         \s
            11/ofd/2-2/scq/23       128    -     Ranged      3.1        -          0  e457.4007.be03  -         \s
            11/ofd/2-2/scq/20       128    -     Ranged      3.1        -          0  e457.4007.c862  -         \s
            11/scq/731-2/scq/290    141    -     Ranged      1.0        -          0  0090.50ea.e928  -         \s
            11/scq/716-2/scq/293    141    -     Ranged      1.0        -          0  38c8.5c02.8f44  -         \s
            11/scq/716-2/scq/290    141    -     Ranged      1.0        -          0  5467.51cf.51cd  -         \s
            11/scq/708-2/scq/290    141    -     Ranged      3.1        -          0  d80f.999e.e89d  -         \s
            11/scq/716-2/scq/290    141    -     Ranged      3.1        -          0  e457.4007.c209  -         \s
            11/scq/750-2/scq/310    142    -     Ranged      1.0        -          0  0090.50eb.4148  -         \s

                          Total    Oper  Disable    Init  Offline
            ---------------------------------------------------------
            Total            27       2        0      23        2 \
            """;

    private static final List<CableMacKey> IDS_EXPECTED = Lists.newArrayList("127", "128", "141", "142")
            .stream()
            .map(CableMacKey::new)
            .collect(Collectors.toList());

    @Test
    void testParseCableMacIds() {
        assertEquals(IDS_EXPECTED, CableMacReader.parseAllMacKeys(SH_CABLE_MAC));
    }
}
