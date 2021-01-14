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

package io.frinx.cli.unit.ios.snmp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.config.Mib;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.config.MibBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.config.MibKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.Views;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.View;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.ViewBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.ViewKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.view.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class ViewWriterTest {

    private static final String WRITE = "configure terminal\n"
            + "snmp-server view SnmpReadAccess iso included\n"
            + "snmp-server view SnmpReadAccess internet excluded\n"
            + "end\n";

    private static final String DELETE = "configure terminal\n"
            + "no snmp-server view SnmpReadAccess\n"
            + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private ViewWriter writer;
    private View data;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(Views.class)
            .child(View.class, new ViewKey("SnmpReadAccess"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new ViewWriter(cli);
        data = new ViewBuilder()
                .setName("SnmpReadAccess")
                .setKey(new ViewKey("SnmpReadAccess"))
                .setConfig(new ConfigBuilder()
                        .setName("SnmpReadAccess")
                        .setMib(Arrays.asList(getMib("iso", Mib.Inclusion.Included),
                                getMib("internet", Mib.Inclusion.Excluded)))
                        .build())
                .build();
    }

    @Test
    public void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE, response.getValue().getContent());
    }

    private Mib getMib(final String name, final Mib.Inclusion inclusion) {
        return new MibBuilder()
                .setName(name)
                .setKey(new MibKey(name))
                .setInclusion(inclusion)
                .build();
    }

}