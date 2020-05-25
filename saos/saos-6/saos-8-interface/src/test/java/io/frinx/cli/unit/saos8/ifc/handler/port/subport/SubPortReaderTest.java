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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

public class SubPortReaderTest {

    private static final String OUTPUT =
            "sub-port create sub-port spMGMT_LS02W parent-port LS02W classifier-precedence 200\n"
            + "sub-port create sub-port spCFM_LS02W parent-port LS02W classifier-precedence 191\n"
            + "sub-port create sub-port LAG=LS02W_100_1 parent-port LS02W classifier-precedence 100 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.100.map\n"
            + "sub-port create sub-port LAG=LS02W_200_1 parent-port LS02W classifier-precedence 101 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.300.map\n"
            + "sub-port create sub-port LAG=LS02W_VLAN992345_1 parent-port LS02W classifier-precedence 103 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.2345.map\n"
            + "sub-port create sub-port LAG=LS02W_1200_1 parent-port LS02W classifier-precedence 102 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.1200.map\n"
            + "sub-port create sub-port LAG=LS02W_VLAN654321_1 parent-port LS02W classifier-precedence 104 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.123.map\n"
            + "sub-port create sub-port LAG=LS02W_Ci_JK_1 parent-port LS02W classifier-precedence 105 "
            + "ingress-l2-transform pop egress-l2-transform push-88a8.111.map\n";

    @Test
    public void getAllIdsTest() {
        SubinterfaceBuilder builder = new SubinterfaceBuilder();

        List<SubinterfaceKey> expected = Arrays.asList(
                new SubinterfaceKey(Long.valueOf("200")),
                new SubinterfaceKey(Long.valueOf("191")),
                new SubinterfaceKey(Long.valueOf("100")),
                new SubinterfaceKey(Long.valueOf("101")),
                new SubinterfaceKey(Long.valueOf("103")),
                new SubinterfaceKey(Long.valueOf("102")),
                new SubinterfaceKey(Long.valueOf("104")),
                new SubinterfaceKey(Long.valueOf("105")));

        Assert.assertEquals(expected, SubPortReader.getAllIds(OUTPUT, "LS02W"));
    }
}
