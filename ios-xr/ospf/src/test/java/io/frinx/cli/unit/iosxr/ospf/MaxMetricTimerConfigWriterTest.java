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

package io.frinx.cli.unit.iosxr.ospf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.iosxr.ospf.handler.MaxMetricTimerConfigWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICALWAYS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.max.metric.timer.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.max.metric.timer.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class MaxMetricTimerConfigWriterTest {

    private static final String WRITE_INPUT = """
            router ospf default\s
            max-metric router-lsa summary-lsa include-stub external-lsa\s
            root
            """;

    private static final String WRITE_NO_INCLUDE_INPUT = """
            router ospf default\s
            max-metric router-lsa\s
            root
            """;

    private static final String UPDATE_INPUT = """
            router ospf default\s
            max-metric router-lsa summary-lsa include-stub\s
            root
            """;

    private static final String REMOVE_TIMEOUT_INPUT = """
            router ospf default\s
            max-metric router-lsa summary-lsa include-stub external-lsa\s
            root
            """;

    private static final String DELETE_INPUT = """
            router ospf default\s
            no max-metric router-lsa summary-lsa include-stub external-lsa\s
            root
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private MaxMetricTimerConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
            .child(Protocols.class)
            .child(Protocol.class, new ProtocolKey(OSPF.class, "default"));

    // test data
    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new MaxMetricTimerConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder()
                .setInclude(Lists.newArrayList(MAXMETRICSUMMARYLSA.class, MAXMETRICINCLUDESTUB.class,
                        MAXMETRICINCLUDETYPE2EXTERNAL.class))
                .setTimeout(BigInteger.valueOf(1000L))
                .setTrigger(MAXMETRICALWAYS.class)
                .build();
    }

    @Test
    void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(piid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void writeNoInclude() throws WriteFailedException {
        // timeout to 500, removed external-lsa
        Config newData = new ConfigBuilder()
                .setTimeout(BigInteger.valueOf(1000L))
                .setTrigger(MAXMETRICALWAYS.class)
                .build();

        this.writer.writeCurrentAttributes(piid, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(WRITE_NO_INCLUDE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void update() throws WriteFailedException {
        // timeout to 500, removed external-lsa
        Config newData = new ConfigBuilder()
                .setInclude(Lists.newArrayList(MAXMETRICSUMMARYLSA.class, MAXMETRICINCLUDESTUB.class))
                .setTimeout(BigInteger.valueOf(500L))
                .setTrigger(MAXMETRICALWAYS.class)
                .build();

        this.writer.updateCurrentAttributes(piid, data, newData, context);

        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void updateNoTimeout() throws WriteFailedException {
        // removing timeout
        Config newData = new ConfigBuilder()
                .setInclude(Lists.newArrayList(MAXMETRICSUMMARYLSA.class, MAXMETRICINCLUDESTUB.class,
                        MAXMETRICINCLUDETYPE2EXTERNAL.class))
                .setTrigger(MAXMETRICALWAYS.class)
                .build();

        this.writer.updateCurrentAttributes(piid, data, newData, context);

        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(response.capture());
        assertEquals(REMOVE_TIMEOUT_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(piid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}
