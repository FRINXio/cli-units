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

package io.frinx.cli.unit.saos8.platform.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;

public class Saos8ComponentReaderTest {

    public static final String OUTPUT = """
            +---------------- CHASSIS DEVICE ID ----------------+
            | Parameter                 |                       |
            +---------------------------+-----------------------+
            | Ethernet Base Address     | 2c:39:c1:47:2c:00     |
            | Eth Address Block Size    | 1024                  |
            | Module Serial Number      | M7655762              |
            | Model Part Number         | 154-8700-930          |
            | Model Revision            | 003                   |
            | Product ID                | CHASS-8700-4          |
            | Manufactured Date         | 09192014              |
            | CLEI Code                 | IPMWV00DRA            |
            | Bar Code                  | 466385                |
            | Backplane Assy Serial Num | C5307742              |
            | Backplane Assy Part Number| 154-0037-810          |
            | Backplane Assy Revision   | 005                   |
            | Backplane Serial Number   | B7428748              |
            | Backplane Part Number     | 154-0037-410          |
            | Backplane Revision        | 002                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            +---------------------------+-----------------------+


            +---------- MODULE DEVICE ID SLOT LM1      ---------+
            | Parameter                 | Value                 |
            +---------------------------+-----------------------+
            | CPU Board                 |                       |
            |  Ethernet Base Address    | 00:23:8a:e0:0a:c8     |
            |  Eth Address Block Size   | 8                     |
            |  Module Serial Number     | M7589108              |
            |  Model Part Number        | 154-0402-900          |
            |  Model Revision           | 004                   |
            |  Product ID               | PSLM-200-2            |
            |  Manufactured Date        | 10162014              |
            |  CLEI Code                | IPUCBG2EAA            |
            |  Bar Code                 | 189448                |
            |  Board Serial Number      | B7392652              |
            |  Board Part Number        | 134-0252-420          |
            |  Board Revision           | 008                   |
            |  Software Compatibility   | 1                     |
            |  Functional Test Count    | 1                     |
            |  Fault Card               | 0                     |
            +---------------------------+-----------------------+
            | Main Board                |                       |
            |  Module Serial Number     | M7589108              |
            |  Model Part Number        | 154-0402-900          |
            |  Model Revision           | 004                   |
            |  Manufactured Date        | 09082014              |
            |  Board Serial Number      | B7445460              |
            |  Board Part Number        | 154-0402-420          |
            |  Board Revision           | 005                   |
            +---------------------------+-----------------------+


            +---------- MODULE DEVICE ID SLOT LM2      ---------+
            | Parameter                 | Value                 |
            +---------------------------+-----------------------+
            | CPU Board                 |                       |
            |  Ethernet Base Address    | 00:23:8a:e7:60:c8     |
            |  Eth Address Block Size   | 8                     |
            |  Module Serial Number     | M7611703              |
            |  Model Part Number        | 154-0400-900          |
            |  Model Revision           | 004                   |
            |  Product ID               | PSLM-200-20           |
            |  Manufactured Date        | 10082014              |
            |  CLEI Code                | IPUCBG1EAA            |
            |  Bar Code                 | 189447                |
            |  Board Serial Number      | B7392514              |
            |  Board Part Number        | 134-0252-420          |
            |  Board Revision           | 008                   |
            |  Software Compatibility   | 1                     |
            |  Functional Test Count    | 1                     |
            |  Fault Card               | 0                     |
            +---------------------------+-----------------------+
            | Main Board                |                       |
            |  Module Serial Number     | M7611703              |
            |  Model Part Number        | 154-0400-900          |
            |  Model Revision           | 004                   |
            |  Manufactured Date        | 09112014              |
            |  Board Serial Number      | B7447154              |
            |  Board Part Number        | 154-0400-420          |
            |  Board Revision           | 005                   |
            +---------------------------+-----------------------+


            +---------- MODULE DEVICE ID SLOT LM3      ---------+
            | Parameter                 | Value                 |
            +---------------------------+-----------------------+
            | CPU Board                 |                       |
            |  Ethernet Base Address    | 00:23:8a:e7:66:e0     |
            |  Eth Address Block Size   | 8                     |
            |  Module Serial Number     | M7611898              |
            |  Model Part Number        | 154-0400-900          |
            |  Model Revision           | 004                   |
            |  Product ID               | PSLM-200-20           |
            |  Manufactured Date        | 10082014              |
            |  CLEI Code                | IPUCBG1EAA            |
            |  Bar Code                 | 189447                |
            |  Board Serial Number      | B7392733              |
            |  Board Part Number        | 134-0252-420          |
            |  Board Revision           | 008                   |
            |  Software Compatibility   | 1                     |
            |  Functional Test Count    | 1                     |
            |  Fault Card               | 0                     |
            +---------------------------+-----------------------+
            | Main Board                |                       |
            |  Module Serial Number     | M7611896              |
            |  Model Part Number        | 154-0400-900          |
            |  Model Revision           | 004                   |
            |  Manufactured Date        | 09222014              |
            |  Board Serial Number      | B7447434              |
            |  Board Part Number        | 154-0400-420          |
            |  Board Revision           | 005                   |
            +---------------------------+-----------------------+


            +---------- MODULE DEVICE ID SLOT LM4      ---------+
            | Parameter                 | Value                 |
            +---------------------------+-----------------------+
            | CPU Board                 |                       |
            |  Ethernet Base Address    | 74:87:bb:a8:ea:f0     |
            |  Eth Address Block Size   | 8                     |
            |  Module Serial Number     | M9434400              |
            |  Model Part Number        | 154-0401-900          |
            |  Model Revision           | ;;F                   |
            |  Product ID               | PSLM-200-11           |
            |  Manufactured Date        | 08172018              |
            |  CLEI Code                | IPUCBLWEAA            |
            |  Bar Code                 | 193805                |
            |  Board Serial Number      | B9815379              |
            |  Board Part Number        | 134-0252-441          |
            |  Board Revision           | 001                   |
            |  Software Compatibility   | 1                     |
            |  Functional Test Count    | 1                     |
            |  Fault Card               | 0                     |
            +---------------------------+-----------------------+
            | Main Board                |                       |
            |  Module Serial Number     | M9434400              |
            |  Model Part Number        | 154-0401-900          |
            |  Model Revision           | ;;F                   |
            |  Manufactured Date        | 08172018              |
            |  Board Serial Number      | B9815268              |
            |  Board Part Number        | 154-0401-410          |
            |  Board Revision           | 004                   |
            +---------------------------+-----------------------+


            +---------- MODULE DEVICE ID SLOT CTX1.ctm ---------+
            | Parameter                 | Value                 |
            +---------------------------+-----------------------+
            | Ethernet Base Address     | 00:23:8a:ea:bf:10     |
            | Eth Address Block Size    | 8                     |
            | Module Serial Number      | M7626011              |
            | Model Part Number         | 154-0005-900          |
            | Model Revision            | ;;X                   |
            | Product ID                | CTX-8700              |
            | Manufactured Date         | 12292020              |
            | CLEI Code                 | IPUCBGREAA            |
            | Bar Code                  | 189524                |
            | Board Serial Number       | C5265618              |
            | Board Part Number         | 154-0005-870          |
            | Board Revision            | 013                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            | Fault Card                | 0                     |
            +---------------------------+-----------------------+


            +---------- MODULE DEVICE ID SLOT CTX2.ctm ---------+
            | Parameter                 | Value                 |
            +---------------------------+-----------------------+
            | Ethernet Base Address     | 2c:39:c1:77:5e:58     |
            | Eth Address Block Size    | 8                     |
            | Module Serial Number      | M7766742              |
            | Model Part Number         | 154-0005-900          |
            | Model Revision            | 006                   |
            | Product ID                | CTX-8700              |
            | Manufactured Date         | 09302014              |
            | CLEI Code                 | IPUCBGREAA            |
            | Bar Code                  | 189524                |
            | Board Serial Number       | C5324994              |
            | Board Part Number         | 154-0005-870          |
            | Board Revision            | 004                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            | Fault Card                | 0                     |
            +---------------------------+-----------------------+


            +----------- MODULE DEVICE ID SLOT CTX1.sm  --------+
            | Parameter                 |                       |
            +---------------------------+-----------------------+
            | Module Serial Number      | M7626011              |
            | Model Part Number         | 154-0005-900          |
            | Model Revision            | ';;X'                 |
            | Product ID                | CTX-8700              |
            | Manufactured Date         | 12292020              |
            | CLEI Code                 | IPUCBGREAA            |
            | Bar Code                  | 189524                |
            | Board Serial Number       | C5265618              |
            | Board Part Number         | 154-0005-870          |
            | Board Revision            | 013                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            | Fault Card                | 0                     |
            +---------------------------+-----------------------+


            +----------- MODULE DEVICE ID SLOT CTX2.sm  --------+
            | Parameter                 |                       |
            +---------------------------+-----------------------+
            | Module Serial Number      | M7766742              |
            | Model Part Number         | 154-0005-900          |
            | Model Revision            | 006                   |
            | Product ID                | CTX.SM-8700           |
            | Manufactured Date         | 09302014              |
            | CLEI Code                 | IPUCBGREAA            |
            | Bar Code                  | 189524                |
            | Board Serial Number       | C5324994              |
            | Board Part Number         | 154-0005-870          |
            | Board Revision            | 004                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            | Fault Card                | 0                     |
            +---------------------------+-----------------------+


            +----------- MODULE DEVICE ID SLOT SM       --------+
            | Parameter                 |                       |
            +---------------------------+-----------------------+
            | Module Serial Number      | M7696693              |
            | Model Part Number         | 154-0006-900          |
            | Model Revision            | 005                   |
            | Product ID                | SM-8700               |
            | Manufactured Date         | 09052014              |
            | CLEI Code                 | IPUCBGSEAA            |
            | Bar Code                  | 189526                |
            | Board Serial Number       | C5307255              |
            | Board Part Number         | 154-0006-810          |
            | Board Revision            | 006                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            | Fault Card                | 0                     |
            +---------------------------+-----------------------+


            +--------------- DEVICE ID SLOT CFU      -----------+
            | Parameter                 |                       |
            +---------------------------+-----------------------+
            | Module Serial Number      | M7619209              |
            | Model Part Number         | 154-0008-900          |
            | Model Revision            | 004                   |
            | Product ID                | FAN-8700-4            |
            | Manufactured Date         | 08202014              |
            | CLEI Code                 | IPUCBGUEAA            |
            | Bar Code                  | 189529                |
            | Board Serial Number       |                       |
            | Board Part Number         |                       |
            | Board Revision            | 001                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            +---------------------------+-----------------------+


            +---------------- DEVICE ID SLOT PWR-A    ----------+
            | Parameter                 |                       |
            +---------------------------+-----------------------+
            | Module Serial Number      | M7732920              |
            | Model Part Number         | 154-0001-900          |
            | Model Revision            | 002                   |
            | Product ID                | PSU-8700              |
            | Manufactured Date         | 09052014              |
            | CLEI Code                 | IPUPAL4PAA            |
            | Bar Code                  | 189537                |
            | Board Serial Number       | M7732920              |
            | Board Part Number         | PS2553-Y              |
            | Board Revision            | 00A                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            +---------------------------+-----------------------+


            +---------------- DEVICE ID SLOT PWR-B    ----------+
            | Parameter                 |                       |
            +---------------------------+-----------------------+
            | Module Serial Number      | M7749043              |
            | Model Part Number         | 154-0001-900          |
            | Model Revision            | 002                   |
            | Product ID                | PSU-8700              |
            | Manufactured Date         | 09082014              |
            | CLEI Code                 | IPUPAL4PAA            |
            | Bar Code                  | 189537                |
            | Board Serial Number       | M7749043              |
            | Board Part Number         | PS2553-Y              |
            | Board Revision            | 00A                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            +---------------------------+-----------------------+


            +----------------- IOM DEVICE ID -------------------+
            | Parameter                 | Value                 |
            +---------------------------+-----------------------+
            | Module Serial Number      | M7750640              |
            | Model Part Number         | 154-0004-900          |
            | Model Revision            | 005                   |
            | Product ID                | IOM-8700              |
            | Manufactured Date         | 09112014              |
            | CLEI Code                 | IPU3A7ALAA            |
            | Bar Code                  | 189527                |
            | Board Serial Number       | C5318431              |
            | Board Part Number         | 154-0004-830          |
            | Board Revision            | 006                   |
            | Software Compatibility    | 1                     |
            | Functional Test Count     | 1                     |
            +---------------------------+-----------------------+""";

    public static final String OUTPUT_XCVR_PORT = """
            +------------------------------------------Transceiver-Status----------------------------------+
            |                                |                                       |Ether Medium &  |Diag|
            |             Port               |      Vendor Name & Part Number        |Connector Type  |Data|
            +--------------------------------+---------------------------------------+----------------+----+
            | 1/1                            | CIENA NTTA03AA Rev001                 |100GE-MM-SR10:MU|Yes |
            | 1/2                            | CIENA NTTA03AA Rev001                 |100GE-MM-SR10:MU|Yes |
            | 2/1                            | Empty                                 |                |    |
            | 2/2                            | CIENA-JDS XCVR-S10V31 Rev000B         |10G BASE-LR:LC  |Yes |
            | 2/3                            | CISCO-AVAGO SFCT-739SMZ RevG3.1       |10G BASE-LR:LC  |Yes |
            | 2/4                            | Empty                                 |                |    |
            | 2/5                            | CISCO-AVAGO SFCT-739SMZ RevG3.1       |10G BASE-LR:LC  |Yes |
            | 2/6                            | CISCO-AVAGO SFCT-739SMZ RevG3.1       |10G BASE-LR:LC  |Yes |
            | 2/7                            | CISCO-OPLINK TPP5XGFLRCCISE2G Rev01   |10G BASE-LR:LC  |Yes |
            | 2/8                            | Empty                                 |                |    |
            | 2/9                            | CISCO-FINISAR FTLX1474D3BCL-C2 RevA   |10G BASE-LR:LC  |Yes |
            | 2/10                           | CISCO-AVAGO SFCT-5798PZ-CS3 Rev0000   |1000BASE-LX:LC  |    |
            | 2/11                           | CISCO-FINISAR FTLX1474D3BCL-C2 RevA   |10G BASE-LR:LC  |Yes |
            | 2/12                           | Empty                                 |                |    |
            | 2/13                           | Empty                                 |                |    |
            | 2/14                           | Empty                                 |                |    |
            | 2/15                           | Empty                                 |                |    |
            | 2/16                           | Empty                                 |                |    |
            | 2/17                           | Empty                                 |                |    |
            | 2/18                           | Empty                                 |                |    |
            | 2/19                           | Empty                                 |                |    |
            | 2/20                           | Empty                                 |                |    |
            | 3/1                            | CIENA-JDS XCVR-S10V31 Rev000B         |10G BASE-LR:LC  |Yes |
            | 3/2                            | Empty                                 |                |    |
            | 3/3                            | Empty                                 |                |    |
            | 3/4                            | Empty                                 |                |    |
            | 3/5                            | Empty                                 |                |    |
            | 3/6                            | Empty                                 |                |    |
            | 3/7                            | Empty                                 |                |    |
            | 3/8                            | Empty                                 |                |    |
            | 3/9                            | CISCO-FINISAR FTRJ1319P1BTL-C7 RevA   |1000BASE-LX:LC  |    |
            | 3/10                           | Empty                                 |                |    |
            | 3/11                           | Empty                                 |                |    |
            | 3/12                           | Empty                                 |                |    |
            | 3/13                           | JDSU JMEP-01LX10A00 Rev1              |1000BASE-LX:LC  |Yes |
            | 3/14                           | Empty                                 |                |    |
            | 3/15                           | Empty                                 |                |    |
            | 3/16                           | Empty                                 |                |    |
            | 3/17                           | CISCO-AVAGO ABCU-5710RZ-CS2           |1000BASE-T:RJ45 |    |
            | 3/18                           | Empty                                 |                |    |
            | 3/19                           | CIENA-FIN XCVR-B00CRJ RevA            |1000BASE-T:RJ45 |    |
            | 3/20                           | CIENA-FIN XCVR-B00CRJ RevA            |1000BASE-T:RJ45 |    |
            | 4/1                            | CISCO-AVAGO SFCT-739SMZ RevG3.1       |10G BASE-LR:LC  |Yes |
            | 4/2                            | Empty                                 |                |    |
            | 4/3                            | Empty                                 |                |    |
            | 4/4                            | Empty                                 |                |    |
            | 4/5                            | Empty                                 |                |    |
            | 4/6                            | Empty                                 |                |    |
            | 4/7                            | Empty                                 |                |    |
            | 4/8                            | Empty                                 |                |    |
            | 4/9                            | Empty                                 |                |    |
            | 4/10                           | Empty                                 |                |    |
            | 4/11                           | CIENA-INN XCVR-Q10V31 RevB            |100GE-QSFP28:LC |Yes |
            +--------------------------------+---------------------------------------+----------------+----+""";

    @Test
    void parseAllModulesTest() {
        final var componentKeys = Saos8ComponentReader.parseAllModules(OUTPUT);
        final var expectedKeys = List.of(
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "LM1"),
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "LM2"),
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "LM3"),
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "LM4"),
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "CTX1.ctm"),
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "CTX2.ctm"),
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "CTX1.sm"),
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "CTX2.sm"),
                new ComponentKey(Saos8ComponentReader.MODULE_PREFIX + "SM")
        );
        assertEquals(expectedKeys, componentKeys);
    }

    @Test
    void parseAllDeviceIdsTest() {
        final var componentKeys = Saos8ComponentReader.parseAllDeviceIds(OUTPUT);
        final var expectedKeys = List.of(
                new ComponentKey(Saos8ComponentReader.DEVICE_PREFIX + "CFU"),
                new ComponentKey(Saos8ComponentReader.DEVICE_PREFIX + "PWR-A"),
                new ComponentKey(Saos8ComponentReader.DEVICE_PREFIX + "PWR-B")
        );
        assertEquals(expectedKeys, componentKeys);
    }

    @Test
    void parseIomTest() {
        final var componentKeys = Saos8ComponentReader.parseIom(OUTPUT);
        final var expectedKeys = List.of(
                new ComponentKey(Saos8ComponentReader.IOM_PREFIX + "IOM")
        );
        assertEquals(expectedKeys, componentKeys);
    }

    @Test
    void parseAllPortIds() {
        final var componentKeys = Saos8ComponentReader.parseAllPortIds(OUTPUT_XCVR_PORT);
        final var expectedKeys = List.of(
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "1/1"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "1/2"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/1"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/2"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/3"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/4"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/5"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/6"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/7"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/8"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/9"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/10"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/11"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/12"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/13"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/14"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/15"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/16"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/17"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/18"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/19"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "2/20"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/1"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/2"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/3"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/4"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/5"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/6"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/7"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/8"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/9"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/10"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/11"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/12"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/13"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/14"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/15"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/16"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/17"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/18"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/19"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "3/20"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/1"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/2"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/3"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/4"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/5"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/6"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/7"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/8"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/9"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/10"),
                new ComponentKey(Saos8ComponentReader.PORT_PREFIX_CONST + "4/11")
        );
        assertEquals(expectedKeys, componentKeys);
    }
}