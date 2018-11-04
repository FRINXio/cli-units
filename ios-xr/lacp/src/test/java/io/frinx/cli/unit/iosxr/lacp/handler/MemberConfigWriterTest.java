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

package io.frinx.cli.unit.iosxr.lacp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.member.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.member.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MemberConfigWriterTest {

    private static final Config WRITE_DATA_1 = new ConfigBuilder()
            .setInterface("GigabitEthernet0/0/0/0")
            .setLacpMode(LacpActivityType.ACTIVE)
            .setInterval(LacpPeriodType.SLOW)
            .build();

    private static final Config WRITE_DATA_2 = new ConfigBuilder()
            .setInterface("GigabitEthernet0/0/0/1")
            .setInterval(LacpPeriodType.FAST)
            .build();

    private static final String WRITTEN_DATA_1 = "interface GigabitEthernet0/0/0/0\n"
            + "bundle id 100 mode active\n"
            + "no lacp period short\n"
            + "root\n";

    private static final String WRITTEN_DATA_2 = "interface GigabitEthernet0/0/0/1\n"
            + "bundle id 1 mode on\n"
            + "lacp period short\n"
            + "root\n";

    private static final String DELETED_DATA_2 = "interface GigabitEthernet0/0/0/1\n"
            + "no bundle id\n"
            + "no lacp period short\n"
            + "root\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InstanceIdentifier iid = IIDs.IN_IN_CONFIG;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private MemberConfigWriter lagMemberConfigWriter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        this.lagMemberConfigWriter = new MemberConfigWriter(cli);
    }

    @Test
    public void writeCurrentAttributes1() throws WriteFailedException {
        lagMemberConfigWriter.writeMemberConfig(iid, WRITE_DATA_1, "100");
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITTEN_DATA_1, response.getValue().getContent());
    }

    @Test
    public void writeCurrentAttributes2() throws WriteFailedException {
        lagMemberConfigWriter.writeMemberConfig(iid, WRITE_DATA_2, "1");
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITTEN_DATA_2, response.getValue().getContent());
    }

    @Test
    public void deleteCurrentAttributesTest() throws WriteFailedException {
        lagMemberConfigWriter.deleteCurrentAttributes(iid, WRITE_DATA_2, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETED_DATA_2, response.getValue().getContent());
    }
}
