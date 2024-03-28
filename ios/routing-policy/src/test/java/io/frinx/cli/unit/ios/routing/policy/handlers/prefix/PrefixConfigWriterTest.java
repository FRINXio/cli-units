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

package io.frinx.cli.unit.ios.routing.policy.handlers.prefix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.policy.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefixBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class PrefixConfigWriterTest {

    private static final String WRITE_INPUT = """
            configure terminal
            ip prefix-list NAME seq 5 permit 0.0.0.0/24 ge 8 le 24
            end
            """;

    private static final String DELETE_INPUT = """
            configure terminal
            no ipv6 prefix-list NAME seq 10 deny AB::0/64 le 24
            end
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private PrefixConfigWriter writer;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = IIDs.RO_DE_PREFIXSETS.child(PrefixSet.class, new PrefixSetKey("NAME"));
    private Config data;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new PrefixConfigWriter(cli);
    }

    @Test
    void write() throws WriteFailedException {
        data = new ConfigBuilder()
                .setIpPrefix(IpPrefixBuilder.getDefaultInstance("0.0.0.0/24"))
                .setMasklengthRange("exact")
                .addAugmentation(PrefixConfigAug.class,
                        new PrefixConfigAugBuilder()
                                .setOperation(PERMIT.class)
                                .setSequenceId(5L)
                                .setMinimumPrefixLength((short) 8)
                                .setMaximumPrefixLength((short) 24)
                                .build())
                .build();

        writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        data = new ConfigBuilder()
                .setIpPrefix(IpPrefixBuilder.getDefaultInstance("AB::0/64"))
                .setMasklengthRange("exact")
                .addAugmentation(PrefixConfigAug.class,
                        new PrefixConfigAugBuilder()
                                .setOperation(DENY.class)
                                .setSequenceId(10L)
                                .setMaximumPrefixLength((short) 24)
                                .build())
                .build();

        writer.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

}