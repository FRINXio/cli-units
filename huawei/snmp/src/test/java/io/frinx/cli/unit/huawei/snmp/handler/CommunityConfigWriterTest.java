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

package io.frinx.cli.unit.huawei.snmp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.snmp.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.huawei.snmp.extension.rev211129.huawei.snmp.top.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.huawei.snmp.extension.rev211129.huawei.snmp.top.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class CommunityConfigWriterTest {

    @Mock
    private Cli cli;

    @Mock
    private WriteContext writeContext;

    private CommunityConfigWriter writer;

    private static final String WRITE_DATA = """
            system-view
            snmp-agent local-engineid 17634DB030425C50419D5
            Y
            snmp-agent sys-info location Mukachevo
            Y
            return
            """;

    private static final String WRITE_ANOTHER_DATA = """
            system-view
            snmp-agent local-engineid 435677DB030425C32423423
            Y
            snmp-agent community read %^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDasdsd acl 2000
            Y
            snmp-agent community write %^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%# acl 2000
            Y
            undo snmp-agent sys-info location
            Y
            return
            """;

    private static final String UPDATE_WITH_DATA = """
            system-view
            snmp-agent local-engineid 17634DB030425C50419D5
            Y
            undo snmp-agent community %^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDasdsd
            Y
            undo snmp-agent community %^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%#
            Y
            snmp-agent sys-info location Mukachevo
            Y
            return
            """;

    private static final String DELETE_DATA = """
            system-view
            undo snmp-agent local-engineid
            Y
            undo snmp-agent sys-info location
            Y
            return
            """;

    private static final String DELETE_ANOTHER_DATA = """
            system-view
            undo snmp-agent local-engineid
            Y
            undo snmp-agent community %^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDasdsd
            Y
            undo snmp-agent community %^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%#
            Y
            undo snmp-agent sys-info location
            Y
            return
            """;

    private final InstanceIdentifier<Config> iid = IIDs.SN_AUG_SNMPHUAWEIAUG_CONFIG;

    private final Config config = new ConfigBuilder()
            .setLocalEngineid("17634DB030425C50419D5")
            .setCommunityLocation("Mukachevo")
            .build();

    private final Config configWithAnotherData = new ConfigBuilder()
            .setLocalEngineid("435677DB030425C32423423")
            .setReadCommunityPassword(new EncryptedPassword(
                    new PlainString("%^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDasdsd")))
            .setWriteCommunityPassword(new EncryptedPassword(
                    new PlainString("%^%#z6fs5u7VNTEC}>C&W:(NH><t7amc_%<{B<+H#smPJh!W;x{y)&7%%&KAEDq7%#")))
            .build();


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        writer = new CommunityConfigWriter(cli);
    }

    @Test
    void testWriteData() throws Exception {
        writer.writeCurrentAttributes(iid, config, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_DATA));
    }

    @Test
    void testWriteAnotherData() throws Exception {
        writer.writeCurrentAttributes(iid, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_ANOTHER_DATA));
    }

    @Test
    void testUpdateWithData() throws Exception {
        writer.updateCurrentAttributes(iid, configWithAnotherData, config, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(UPDATE_WITH_DATA));
    }

    @Test
    void testUpdateWithAnotherData() throws Exception {
        writer.updateCurrentAttributes(iid, config, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(WRITE_ANOTHER_DATA));
    }

    @Test
    void testDeleteData() throws Exception {
        writer.deleteCurrentAttributes(iid, config, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_DATA));
    }

    @Test
    void testDeleteAnotherData() throws Exception {
        writer.deleteCurrentAttributes(iid, configWithAnotherData, writeContext);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(DELETE_ANOTHER_DATA));
    }
}