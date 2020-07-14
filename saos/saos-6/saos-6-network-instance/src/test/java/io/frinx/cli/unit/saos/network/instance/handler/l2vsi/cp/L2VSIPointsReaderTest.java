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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.cp;

import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPointKey;

public class L2VSIPointsReaderTest {

    private static final String OUTPUT =
            "virtual-switch ethernet create vs Optic_test2 reserved-vlan 4000 description Optic_test2\n"
            + "virtual-switch ethernet create vs Optic_test5 reserved-vlan 4001 description Optic_test5\n"
            + "virtual-switch ethernet create vs EVPL-test vc vc2721 description EVPL-test-2\n"
            + "virtual-switch ethernet add vs Optic_test2 port 1 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs Optic_test2 port 2 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs Optic_test5 port 3 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs Optic_test5 port 4 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 10 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 11 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 12 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 13 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 14 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 15 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 16 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 17 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 18 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 19 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 20 encap-cos-policy port-inherit\n"
            + "virtual-switch ethernet add vs EVPL-test port 7 vlan 21 encap-cos-policy port-inherit\n"
            + "port set port 1 untagged-data-vs Optic_test2\n"
            + "port set port 2 untagged-data-vs Optic_test2\n"
            + "port set port 3 untagged-data-vs Optic_test5\n"
            + "port set port 4 untagged-data-vs Optic_test5\n"
            + "traffic-profiling standard-profile create port 1 profile 1 name test3 cir 5056 eir 0 cbs 128 ebs 0 "
            + "vs Optic_test2\n"
            + "traffic-profiling standard-profile create port 2 profile 1 name test cir 2048 eir 0 cbs 128 ebs 0 "
            + "vs Optic_test2\n"
            + "cfm set vs-automatic-meps off\n";

    @Test
    public void getAllIdsTest() {
        Assert.assertEquals(Collections.singletonList(new ConnectionPointKey("vc2721")),
                L2VSIPointsReader.getAllIds(OUTPUT, "EVPL-test"));
    }
}
