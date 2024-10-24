/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.system.handler.license;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.license.rev221202.licenses.top.licenses.LicenseKey;

class LicenseReaderTest {

    static final String OUTPUT = """
            CBR8 DOCSIS 3.0 Downstream Channel License Feature (DS_License):
              Description: CBR8 DOCSIS 3.0 Downstream Channel License Feature
              Count: 436
              Version: 1.0
              Status: AUTHORIZED
              Export status: NOT RESTRICTED

            CBR8 DOCSIS 3.0 Upstream Channel License Feature (US_License):
              Description: CBR8 DOCSIS 3.0 Upstream Channel License Feature
              Count: 81
              Version: 1.0
              Status: AUTHORIZED
              Export status: NOT RESTRICTED

            cBR DOCSIS 3.0 Line-card N+1 HA License Feature (LCHA_License):
              Description: cBR DOCSIS 3.0 Line-card HA License Feature
              Count: 1
              Version: 1.0
              Status: AUTHORIZED
              Export status: NOT RESTRICTED

            CBR8 VOD/SDV Downstream Video QAM License Feature (NC_License):
              Description: CBR8 VOD/SDV Downstream Video QAM License Feature
              Count: 16
              Version: 1.0
              Status: AUTHORIZED
              Export status: NOT RESTRICTED

            cBR8 D3.1 Downstream License (DS_D31_License):
              Description: cBR DOCSIS 3.1 6MHz Downstream License
              Count: 180
              Version: 1.0
              Status: AUTHORIZED
              Export status: NOT RESTRICTED

            cBR8 D3.1 Upstream License (US_D31_License):
              Description: cBR DOCSIS 3.1 1MHz Upstream Exclusive License
              Count: 62
              Version: 1.0
              Status: AUTHORIZED
              Export status: NOT RESTRICTED

            CBR8 - Supervisor 10G Port License (WAN_License):
              Description: CBR8 - Supervisor 10G Port License
              Count: 5
              Version: 1.0
              Status: AUTHORIZED
              Export status: NOT RESTRICTED""";

    @Test
    void testGetIds() {
        List<LicenseKey> keys = LicenseReader.parseAllKeyIds(OUTPUT);
        assertEquals(Lists.newArrayList(
                        "DS_License",
                        "US_License",
                        "LCHA_License",
                        "NC_License",
                        "DS_D31_License",
                        "US_D31_License",
                        "WAN_License"),
                keys.stream().map(LicenseKey::getLicenseId).collect(Collectors.toList()));
    }
}
