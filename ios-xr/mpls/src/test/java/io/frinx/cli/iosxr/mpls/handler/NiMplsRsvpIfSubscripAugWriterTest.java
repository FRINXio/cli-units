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

package io.frinx.cli.iosxr.mpls.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.MplsRsvpSubscriptionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te.InterfaceAttributes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class NiMplsRsvpIfSubscripAugWriterTest {

    private static final String WRITE_INPUT = "rsvp\n" +
            "interface Loopback0\n" +
            "bandwidth 5\n" +
            "root\n";

    private static final String UPDATE_INPUT = "rsvp\n" +
            "interface Loopback0\n" +
            "bandwidth 1.005\n" +
            "root\n";

    private static final String UPDATE_DEFAULT_BW_INPUT = "rsvp\n" +
            "interface Loopback0\n" +
            "bandwidth\n" +
            "root\n";

    private static final String DELETE_INPUT = "rsvp\n" +
            "interface Loopback0\n" +
            "no bandwidth\n" +
            "root\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private NiMplsRsvpIfSubscripAugWriter writer;

    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private InstanceIdentifier iid = KeyedInstanceIdentifier.create(InterfaceAttributes.class)
            .child(Interface.class, new InterfaceKey(new InterfaceId("Loopback0")));

    // test data
    private NiMplsRsvpIfSubscripAug data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new NiMplsRsvpIfSubscripAugWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new NiMplsRsvpIfSubscripAugBuilder().setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(5000L))
                .build();
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        // change bandwidth to different number
        NiMplsRsvpIfSubscripAug newData = new NiMplsRsvpIfSubscripAugBuilder()
                .setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(1005L))
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void updateDefault() throws WriteFailedException {
        // change badwidth to default
        NiMplsRsvpIfSubscripAug newData = new NiMplsRsvpIfSubscripAugBuilder()
                .setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth("default"))
                .build();

        this.writer.updateCurrentAttributes(iid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_DEFAULT_BW_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributes(iid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}
