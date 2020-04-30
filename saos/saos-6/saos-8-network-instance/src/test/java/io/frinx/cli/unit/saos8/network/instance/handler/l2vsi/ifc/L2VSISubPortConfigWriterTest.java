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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ifc;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAugBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSISubPortConfigWriterTest {

    @Mock
    private Cli cli;

    private final InstanceIdentifier iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey("IPTV_800"))
            .child(Interfaces.class)
            .child(Interface.class, new InterfaceKey("LAG=LM01W_IPTV_800_1"))
            .child(Config.class);

    private final ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);
    private L2VSISubPortConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new L2VSISubPortConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributesWResultTest() throws WriteFailedException {
        Config data = createConfig("LAG=LM01W_IPTV_800_1", Ieee8023adLag.class);

        writer.writeCurrentAttributesWResult(iid, data, null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("virtual-switch interface attach sub-port LAG=LM01W_IPTV_800_1 vs IPTV_800\n"
                + "configuration save", commands.getValue().getContent());
    }

    @Test
    public void writeCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        Config data = createConfig("cpuCFM990100_0190", L2vlan.class);

        Assert.assertFalse(writer.writeCurrentAttributesWResult(iid, data, null));
    }

    @Test(expected = NullPointerException.class)
    public void writeCurrentAttributesWResultTest_missingType() throws WriteFailedException {
        Config data = createConfig("cpuCFM990100_0190", null);

        writer.writeCurrentAttributesWResult(iid, data, null);
    }

    @Test
    public void deleteCurrentAttributesWResultTest() throws WriteFailedException {
        Config data = createConfig("LAG=LM01W_IPTV_800_1", Ieee8023adLag.class);

        writer.deleteCurrentAttributesWResult(iid, data, null);

        Mockito.verify(cli).executeAndRead(commands.capture());
        Assert.assertEquals("virtual-switch interface detach sub-port LAG=LM01W_IPTV_800_1\n"
                + "configuration save", commands.getValue().getContent());
    }

    @Test
    public void deleteCurrentAttributesWResultTest_incorrectType() throws WriteFailedException {
        Config data = createConfig("cpuCFM990100_0190", L2vlan.class);

        Assert.assertFalse(writer.deleteCurrentAttributesWResult(iid, data, null));
    }

    private Config createConfig(String subPortName, Class<? extends InterfaceType> type) {
        ConfigBuilder builder = new ConfigBuilder();

        builder.setId(subPortName);
        builder.setInterface(subPortName);

        if (type != null) {
            builder.addAugmentation(Saos8NiIfcAug.class, new Saos8NiIfcAugBuilder().setType(type).build());
        }

        return builder.build();
    }
}
