/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.ios.local.routing.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHopsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class StaticWriterTest {

    private static final String EXPECTED_WRITE_OUTPUT = """
            configure terminal
            ip route %s
            end
            """;

    private static final String EXPECTED_DELETE_OUTPUT = """
            configure terminal
            no ip route %s
            end
            """;

    @Mock
    private Cli cli;

    private InstanceIdentifier iid = IIDs.NE_NE_PR_PR_ST_STATIC;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private StaticWriter staticWriter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        staticWriter = new StaticWriter(cli);
    }

    private NextHop getNextHop(String ip) {
        return new NextHopBuilder().setIndex(ip).setConfig(new org.opendaylight.yang.gen.v1.http.frinx
                .openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next
                .hop.ConfigBuilder().setIndex(ip).build()).build();
    }

    private Static getStaticData(String ipPrefix, String... nextHops) {
        IpPrefix ipPrefixLocal = new IpPrefix(new Ipv4Prefix(ipPrefix));
        List<NextHop> nextHopsLocal = null;
        if (nextHops != null) {
            nextHopsLocal = new ArrayList<>();
            for (String nextHop : nextHops) {
                nextHopsLocal.add(getNextHop(nextHop));
            }
        }
        return new StaticBuilder()
                .setConfig(new ConfigBuilder().setPrefix(ipPrefixLocal).build())
                .setKey(new StaticKey(ipPrefixLocal))
                .setNextHops(new NextHopsBuilder().setNextHop(nextHopsLocal).build())
                .setPrefix(ipPrefixLocal)
                .build();
    }

    @Test
    void writeCurrentAttributesForTypeTest() throws WriteFailedException {
        Static writeData1 = getStaticData("1.1.1.1/16", "9.9.9.9");
        staticWriter.writeCurrentAttributes(iid, writeData1, Mockito.mock(WriteContext.class));
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(String.format(EXPECTED_WRITE_OUTPUT, "1.1.1.1 255.255.0.0 9.9.9.9"),
                response.getValue().getContent());
    }

    @Test
    void writeCurrentAttributesIAExceptionTest() throws WriteFailedException {
        assertThrows(IllegalArgumentException.class, () -> {
            staticWriter.writeCurrentAttributes(iid, getStaticData("", (String) null),
                    Mockito.mock(WriteContext.class));
        });
    }

    @Test
    void updateCurrentAttributesForTypeTest() throws WriteFailedException {
        Static writeData1 = getStaticData("1.1.1.1/16", "9.9.9.9", "Ethernet1");
        Static writeData2 = getStaticData("1.1.1.1/16", "9.9.9.9");
        staticWriter.updateCurrentAttributes(iid, writeData1, writeData2, Mockito.mock(WriteContext.class));
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(String.format(EXPECTED_DELETE_OUTPUT, "1.1.1.1 255.255.0.0 Ethernet1"),
                response.getValue().getContent());

        staticWriter.updateCurrentAttributes(iid, writeData2, writeData1, Mockito.mock(WriteContext.class));
        Mockito.verify(cli, Mockito.times(2)).executeAndRead(response.capture());
        assertEquals(String.format(EXPECTED_WRITE_OUTPUT, "1.1.1.1 255.255.0.0 Ethernet1"),
                response.getValue().getContent());
    }

    @Test
    void deleteCurrentAttributesForTypeTest() throws WriteFailedException {
        Static writeData1 = getStaticData("1.1.1.1/16", "9.9.9.9");
        staticWriter.deleteCurrentAttributes(iid, writeData1, Mockito.mock(WriteContext.class));
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(String.format(EXPECTED_DELETE_OUTPUT, "1.1.1.1 255.255.0.0 9.9.9.9"),
                response.getValue().getContent());
    }
}