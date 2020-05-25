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

package io.frinx.cli.unit.saos.ifc.handler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    public static final String SH_INTERFACE = "port tdm set mode ansi\n"
            + "port set port 1 speed hundred auto-neg off mode rj45\n"
            + "port set port 1 max-frame-size 9000 ingress-to-egress-qmap NNI-NNI resolved-cos-remark-l2 true\n"
            + "port set port 2 mode rj45\n"
            + "port set port 2 description cau\n"
            + "port set port 3 speed hundred auto-neg off mode rj45\n"
            + "port set port 3 max-frame-size 9216\n"
            + "port disable port 4\n"
            + "port set port 4 max-frame-size 9216 ingress-to-egress-qmap NNI-NNI\n"
            + "port set port 5 max-frame-size 9130 description test\n"
            + "port set port 6 max-frame-size 9216\n"
            + "port set port 8 description TEST\n"
            + "port set port 10 speed ten-gig auto-neg off\n"
            + "port set port 10 max-frame-size 9200\n"
            + "vlan add vlan 25,50 port 4\n"
            + "vlan add vlan 127,1234 port 4\n"
            + "vlan add vlan 5 port 5\n"
            + "vlan add vlan 5 port 6\n"
            + "vlan add vlan 199 port 9\n"
            + "vlan remove vlan 127 port 9\n"
            + "vlan add vlan 2,199 port 10\n"
            + "vlan remove vlan 127 port 10\n"
            + "aggregation add agg LP02 port 2\n"
            + "aggregation add agg LS02E port 3\n"
            + "port set port 4 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "port set port 7 acceptable-frame-type untagged-only\n"
            + "virtual-switch ethernet add vs VLAN111222 port 1\n"
            + "virtual-switch ethernet add vs VLAN111333 port 3\n"
            + "port set port 1 untagged-data-vs VLAN111222 untagged-ctrl-vs VLAN111222\n"
            + "virtual-circuit ethernet set port 4 vlan-ethertype-policy vlan-tpid\n"
            + "virtual-circuit ethernet set port 8 vlan-ethertype-policy vlan-tpid\n"
            + "virtual-circuit ethernet set port 10 vlan-ethertype 88A8\n"
            + "traffic-services queuing egress-port-queue-group set port 1 shaper-rate 50048\n"
            + "aggregation set port 4 activity passive\n"
            + "l2-cft set port 1 profile VLAN111222\n"
            + "l2-cft enable port 1\n"
            + "traffic-profiling set port 1 mode advanced\n"
            + "traffic-profiling set port 3 mode advanced\n"
            + "traffic-profiling set port 4 mode advanced\n"
            + "traffic-profiling set port 6 mode advanced\n"
            + "traffic-profiling standard-profile create port 1 profile 1 name CIA_CoS0 cir 50048 eir 0 cbs 8 ebs 0\n"
            + "traffic-profiling standard-profile set port 1 profile CIA_CoS0 remark-rcos-policy remark-both"
            + " green-remark-rcos 0 yellow-remark-rcos 0\n"
            + "traffic-profiling standard-profile create port 1 profile 2 name V4096 cir 10048 eir 0 cbs 256"
            + " ebs 0 vs VLAN111222\n"
            + "traffic-profiling enable port 1\n"
            + "lldp  set port 1-10 notification on\n"
            + "mstp disable port 3\n"
            + "mstp disable port 10\n";

    public static final String SH_AGG_IFACE =
            "aggregation create agg LP01\n"
            + "aggregation create agg LM01E\n"
            + "aggregation create agg LM01W\n"
            + "aggregation create agg LS01W\n"
            + "aggregation create agg LS02W\n"
            + "aggregation create agg LP02\n"
            + "aggregation create agg JMEP\n"
            + "aggregation create agg LSPIRENT01\n";

    public static final String OUTPUT = SH_INTERFACE.concat(SH_AGG_IFACE);

    private static final List<InterfaceKey> IDS_EXPECTED = Lists.newArrayList("1", "2", "3", "4", "5", "6",
            "8",  "10", "9", "7", "LP01", "LM01E", "LM01W", "LS01W", "LS02W", "LP02", "JMEP", "LSPIRENT01")
            .stream()
            .map(InterfaceKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseInterfaceAggIds() {
        Assert.assertEquals(IDS_EXPECTED,
                new InterfaceReader(Mockito.mock(Cli.class)).getAllIds(OUTPUT));
    }
}