/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.network.instance.l2vsi.cp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.Endpoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;

class L2VSIPointsReaderTest {

    private static final String VPLS_OUTPUT = """
             vpls abcd 4444\s
              vpls-peer 8.8.8.8 7.7.7.7 6.6.6.6\s
              vlan 200\s
               untagged e 1/8\s

            """;

    @Test
    void readTest() {
        List<ConnectionPoint> connectionPoints = L2VSIPointsReader.parseVplsLines(VPLS_OUTPUT);
        assertEquals(4, connectionPoints.size());
        List<Endpoint> remotes = connectionPoints.stream()
                .map(ConnectionPoint::getEndpoints)
                .map(Endpoints::getEndpoint)
                .flatMap(Collection::stream)
                .filter(ep -> ep.getRemote() != null)
                .collect(Collectors.toList());
        assertEquals(3, remotes.size());

        List<Endpoint> locals = connectionPoints.stream()
                .map(ConnectionPoint::getEndpoints)
                .map(Endpoints::getEndpoint)
                .flatMap(Collection::stream)
                .filter(ep -> ep.getLocal() != null)
                .collect(Collectors.toList());
        assertEquals(1, locals.size());
    }
}