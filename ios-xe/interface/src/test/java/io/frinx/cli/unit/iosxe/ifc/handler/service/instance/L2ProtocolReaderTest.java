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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceL2protocol.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceL2protocol.ProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.L2protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.L2protocolBuilder;


public class L2ProtocolReaderTest {
    private static final L2protocol SERVICE_INSTANCE_L2PROTOCOL_BUILDER = new L2protocolBuilder()
            .setProtocolType(ProtocolType.Peer)
            .setProtocol(Arrays.asList(Protocol.Lldp, Protocol.Stp))
            .build();

    private static final String SERVICE_INSTANCE_L2PROTOCOL_OUTPUT = " service instance 200 ethernet EVC\n"
            + "  l2protocol peer lldp stp\n";

    @Test
    public void testParseL2protocol() {
        L2protocolBuilder configBuilder = new L2protocolBuilder();
        L2protocolReader.parseL2Protocol(SERVICE_INSTANCE_L2PROTOCOL_OUTPUT, configBuilder);
        Assert.assertEquals(SERVICE_INSTANCE_L2PROTOCOL_BUILDER, configBuilder.build());
    }

}
