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

package io.frinx.cli.unit.iosxr.mpls.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension.rev180822.NiMplsLdpGlobalAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension.rev180822.NiMplsLdpGlobalAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.ldp.global.Ldp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.ldp.global.LdpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.ldp.global.ldp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top.InterfaceAttributes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.Mpls;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.SignalingProtocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class LdpInterfaceWriterTest {

    static final InstanceIdentifier BASE_IID = KeyedInstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, new NetworkInstanceKey(NetworInstance.DEFAULT_NETWORK))
            .child(Mpls.class);

    private static final String WRITE_INTERFACE_INPUT = """
            mpls ldp
            interface Bundle-Ether 511
            root
            """;

    private static final String DELETE_INTERFACE_INPUT = """
            mpls ldp
            no interface Bundle-Ether 511
            root
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private LdpInterfaceConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = BASE_IID.child(SignalingProtocols.class).child(Ldp.class)
            .child(InterfaceAttributes.class)
            .child(Interfaces.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId("Bundle-Ether 511")))
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp
                    ._interface.attributes.top._interface.attributes.interfaces._interface.Config.class);

    // test data
    private Config data;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new LdpInterfaceConfigWriter(this.cli);
    }

    @Test
    void write() throws WriteFailedException {
        Ldp ldp = new LdpBuilder().setGlobal(new GlobalBuilder().setConfig(new org.opendaylight.yang.gen.v1.http
                .frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp.global.ConfigBuilder()
                .addAugmentation(NiMplsLdpGlobalAug.class, new NiMplsLdpGlobalAugBuilder().setEnabled(true)
                        .build()).build()).build()).build();

        Mockito.when(context.readAfter(Mockito.any(InstanceIdentifier.class))).thenReturn(Optional.of(ldp));

        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(WRITE_INTERFACE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        assertEquals(DELETE_INTERFACE_INPUT, response.getValue()
                .getContent());
    }
}
