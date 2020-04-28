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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.SaosVsExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigWriterTest {

    @Mock
    private Cli cli;

    private L2VSIConfigWriter writer;

    private final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey("VLAN111444"))
            .child(Config.class);

    private static final String VC_NAME = "vc4";

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);

    private VsSaosAugBuilder config1Builder = new VsSaosAugBuilder();

    private Config data = new ConfigBuilder().setType(L2VSI.class).setName("VLAN111444").setDescription("Testing")
            .setEnabled(true)
            .addAugmentation(VsSaosAug.class, config1Builder
                    .setEncapCosPolicy(SaosVsExtension.EncapCosPolicy.Fixed)
                    .setEncapFixedDot1dpri((short) 3)
                    .build())
            .build();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new L2VSIConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        writer.writeCurrentAttributesTesting(iid, data, VC_NAME, true);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("virtual-switch ethernet create vs VLAN111444 vc vc4\n"
                        + "virtual-switch ethernet set vs VLAN111444 description Testing\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-cos-policy fixed\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 3\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void testWriteWithNoDesc() throws Exception {
        Config data1 = new ConfigBuilder().setType(L2VSI.class).setName("VLAN111444")
                .setEnabled(true)
                .addAugmentation(VsSaosAug.class, config1Builder
                        .setEncapCosPolicy(SaosVsExtension.EncapCosPolicy.Fixed)
                        .setEncapFixedDot1dpri((short) 3)
                        .build())
                .build();
        writer.writeCurrentAttributesTesting(iid, data1, VC_NAME, true);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("virtual-switch ethernet create vs VLAN111444 vc vc4\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-cos-policy fixed\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 3\n"
                        + "configuration save\n",
                commands.getValue().getContent());
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributesTesting(iid);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("virtual-switch ethernet delete vs VLAN111444\n"
                + "configuration save", commands.getValue().getContent());
    }

    @Test
    public void testUpdate() throws Exception {
        Config dataNew = new ConfigBuilder().setType(L2VSI.class).setName("VLAN111444").setDescription("Testing2")
                .setEnabled(true)
                .addAugmentation(VsSaosAug.class, config1Builder
                        .setEncapCosPolicy(SaosVsExtension.EncapCosPolicy.PhbgInherit)
                        .setEncapFixedDot1dpri((short) 4)
                        .build())
                .build();
        writer.writeCurrentAttributesTesting(iid, dataNew, "", false);

        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("virtual-switch ethernet set vs VLAN111444 description Testing2\n"
                + "virtual-switch ethernet set vs VLAN111444 encap-cos-policy phbg-inherit\n"
                + "virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 4\n"
                + "configuration save\n", commands.getValue().getContent());
    }
}
