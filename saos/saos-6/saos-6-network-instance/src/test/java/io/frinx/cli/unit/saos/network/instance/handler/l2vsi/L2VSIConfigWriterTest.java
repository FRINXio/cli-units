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

import io.fd.honeycomb.translate.write.WriteFailedException;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.SaosVsExtension.EncapCosPolicy;
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

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new L2VSIConfigWriter(cli);
    }

    @Test
    public void testWrite() throws Exception {
        createCommandAndTest(createConfig("Testing", (short) 3, EncapCosPolicy.Fixed, true),
            "virtual-switch ethernet create vs VLAN111444 vc vc4\n"
                    + "virtual-switch ethernet set vs VLAN111444 description \"Testing\"\n"
                    + "virtual-switch ethernet set vs VLAN111444 encap-cos-policy fixed\n"
                    + "virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 3\n"
                    + "l2-cft tagged-pvst-l2pt enable vs VLAN111444\n\n");
    }

    @Test
    public void testWriteNoDesc() throws Exception {
        createCommandAndTest(createConfig(null, (short) 3, EncapCosPolicy.Fixed, true),
                "virtual-switch ethernet create vs VLAN111444 vc vc4\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-cos-policy fixed\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 3\n"
                        + "l2-cft tagged-pvst-l2pt enable vs VLAN111444\n\n");
    }

    @Test
    public void testWriteDisableL2pt() throws Exception {
        createCommandAndTest(createConfig(null, (short) 3, EncapCosPolicy.Fixed, false),
                "virtual-switch ethernet create vs VLAN111444 vc vc4\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-cos-policy fixed\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 3\n"
                        + "l2-cft tagged-pvst-l2pt disable vs VLAN111444\n\n");
    }

    @Test
    public void testWriteNoL2pt() throws Exception {
        createCommandAndTest(createConfig(null, (short) 3, EncapCosPolicy.Fixed, null),
                "virtual-switch ethernet create vs VLAN111444 vc vc4\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-cos-policy fixed\n"
                        + "virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 3\n\n");
    }

    @Test
    public void testDelete() throws Exception {
        writer.deleteCurrentAttributesTesting(iid);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals("virtual-switch ethernet delete vs VLAN111444", commands.getValue().getContent());
    }

    @Test
    public void testUpdateTemplate() {
        // nothing
        Assert.assertEquals("",
                writer.updateTemplate(
                createConfig("desc1", (short) 3, EncapCosPolicy.Fixed, false),
                createConfig("desc1", (short) 3, EncapCosPolicy.Fixed, false), "VLAN111444"));

        // description
        Assert.assertEquals("virtual-switch ethernet set vs VLAN111444 description \"desc2\"\n",
                writer.updateTemplate(
                createConfig("desc1", (short) 3, EncapCosPolicy.Fixed, true),
                createConfig("desc2", (short) 3, EncapCosPolicy.Fixed, true), "VLAN111444"));

        // encap-fixed-dot1dpri
        Assert.assertEquals("virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 5\n",
                writer.updateTemplate(
                createConfig("desc1", (short) 3, EncapCosPolicy.Fixed, true),
                createConfig("desc1", (short) 5, EncapCosPolicy.Fixed, true), "VLAN111444"));

        // encap-cos-policy
        Assert.assertEquals("virtual-switch ethernet set vs VLAN111444 encap-cos-policy port-inherit\n",
                writer.updateTemplate(
                createConfig("desc1", (short) 3, EncapCosPolicy.Fixed, true),
                createConfig("desc1", (short) 3, EncapCosPolicy.PortInherit, true), "VLAN111444"));

        // all config parameters
        Assert.assertEquals("virtual-switch ethernet set vs VLAN111444 description \"desc2\"\n"
                + "l2-cft tagged-pvst-l2pt disable vs VLAN111444\n"
                + "virtual-switch ethernet set vs VLAN111444 encap-cos-policy phbg-inherit\n"
                + "virtual-switch ethernet set vs VLAN111444 encap-fixed-dot1dpri 4\n",
                writer.updateTemplate(
                createConfig("desc1", (short) 3, EncapCosPolicy.Fixed, true),
                createConfig("desc2", (short) 4, EncapCosPolicy.PhbgInherit, false), "VLAN111444"));
    }

    private void createCommandAndTest(Config data, String expected) throws WriteFailedException {
        writer.writeCurrentAttributesTesting(iid, data, VC_NAME, true);
        Mockito.verify(cli).executeAndRead(commands.capture());

        Assert.assertEquals(expected, commands.getValue().getContent());
    }

    private Config createConfig(String desc, Short encapDpri, EncapCosPolicy encapCosPolicy,
                                Boolean l2pt) {
        ConfigBuilder builder = new ConfigBuilder().setType(L2VSI.class).setName("VLAN111444").setEnabled(true);
        VsSaosAugBuilder saosAugBuilder = new VsSaosAugBuilder();

        if (desc != null) {
            builder.setDescription(desc);
        }
        if (encapDpri != null) {
            saosAugBuilder.setEncapFixedDot1dpri(encapDpri);
        }
        if (encapCosPolicy != null) {
            saosAugBuilder.setEncapCosPolicy(encapCosPolicy);
        }
        if (l2pt != null) {
            saosAugBuilder.setTaggedPvstL2pt(l2pt);
        }

        return builder.addAugmentation(VsSaosAug.class, saosAugBuilder.build()).build();
    }
}
