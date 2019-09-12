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

package io.frinx.cli.unit.ios.mpls.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.MplsRsvpSubscriptionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.SignalingProtocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.RsvpTe;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te.InterfaceAttributes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NiMplsRsvpIfSubscripConfigWriterTest {

    private static final String WRITE_INPUT = "configure terminal\n"
            + "interface Tunnel1\n"
            + "ip rsvp bandwidth 5\n"
            + "end\n";

    private static final String WRITE_INPUT_PERCENTAGE = "configure terminal\n"
            + "interface Tunnel1\n"
            + "ip rsvp bandwidth percent 45\n"
            + "end\n";

    private static final String UPDATE_INPUT = "configure terminal\n"
            + "interface Tunnel1\n"
            + "ip rsvp bandwidth 1.005\n"
            + "end\n";

    private static final String UPDATE_DEFAULT_BW_INPUT = "configure terminal\n"
            + "interface Tunnel1\n"
            + "ip rsvp bandwidth\n"
            + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "interface Tunnel1\n"
            + "no ip rsvp bandwidth\n"
            + "end\n";

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private NiMplsRsvpIfSubscripConfigWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = LdpInterfaceWriterTest.BASE_IID
            .child(SignalingProtocols.class).child(RsvpTe.class)
            .child(InterfaceAttributes.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId("Tunnel1")));

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new NiMplsRsvpIfSubscripConfigWriter(this.cli);
    }

    @Test
    public void write() throws Exception {
        Config data = new ConfigBuilder().addAugmentation(NiMplsRsvpIfSubscripAug.class,
                new NiMplsRsvpIfSubscripAugBuilder().setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(5000L))
                        .build())
                .build();
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void writePercentage() throws Exception {
        Config data = new ConfigBuilder().setSubscription(new Percentage((short) 45)).build();
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT_PERCENTAGE, response.getValue().getContent());
    }

    @Test
    public void writeFail() throws Exception {
        Config data = new ConfigBuilder().addAugmentation(NiMplsRsvpIfSubscripAug.class,
                new NiMplsRsvpIfSubscripAugBuilder().setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(5000L))
                        .build())
                .setSubscription(new Percentage((short) 45))
                .build();
        exception.expect(IllegalArgumentException.class);
        this.writer.writeCurrentAttributes(iid, data, context);
    }

    @Test
    public void update() throws Exception {
        Config data = new ConfigBuilder().addAugmentation(NiMplsRsvpIfSubscripAug.class,
                new NiMplsRsvpIfSubscripAugBuilder().setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(5000L))
                        .build())
                .build();
        // change bandwidth to different number
        Config newData = new ConfigBuilder().addAugmentation(NiMplsRsvpIfSubscripAug.class,
                new NiMplsRsvpIfSubscripAugBuilder().setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(1005L))
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void updateDefault() throws Exception {
        Config data = new ConfigBuilder().addAugmentation(NiMplsRsvpIfSubscripAug.class,
                new NiMplsRsvpIfSubscripAugBuilder().setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(5000L))
                        .build())
                .build();
        // change badwidth to default
        Config newData = new ConfigBuilder().addAugmentation(NiMplsRsvpIfSubscripAug.class,
                new NiMplsRsvpIfSubscripAugBuilder()
                        .setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(
                                new MplsRsvpSubscriptionConfig.Bandwidth("default")))
                        .build())
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_DEFAULT_BW_INPUT, response.getValue()
                .getContent());
    }

    @Test
    public void delete() throws Exception {
        Config data = new ConfigBuilder().addAugmentation(NiMplsRsvpIfSubscripAug.class,
                new NiMplsRsvpIfSubscripAugBuilder().setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(5000L))
                        .build())
                .build();
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli)
                .executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }
}
