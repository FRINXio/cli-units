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
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.CtrlProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocol.Name;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocolBuilder;

public class L2VSIConfigCftConfigWriterTest {

    private static final String PROFILES =
            "l2-cft protocol add profile CTB ctrl-protocol 802.1x untagged-disposition discard\n"
            + "l2-cft protocol add profile CTB ctrl-protocol cisco-dtp untagged-disposition discard\n"
            + "l2-cft protocol add profile CTB ctrl-protocol all-bridges-block untagged-disposition discard\n";

    private static final String UPDATE_PROTOCOLS =
            "l2-cft protocol set profile CTB ctrl-protocol cisco-stp-uplink-fast untagged-disposition forward\n"
            + "l2-cft protocol set profile CTB ctrl-protocol all-bridges-block untagged-disposition forward\n"
            + "l2-cft protocol add profile CTB ctrl-protocol garp-block untagged-disposition discard\n"
            + "l2-cft protocol add profile CTB ctrl-protocol gvrp untagged-disposition discard\n"
            + "l2-cft protocol remove profile CTB ctrl-protocol 802.1x\n"
            + "l2-cft protocol remove profile CTB ctrl-protocol cisco-dtp\n";

    private static final String REMOVE_PROTOCOLS =
            "l2-cft protocol remove profile CTB ctrl-protocol 802.1x\n"
            + "l2-cft protocol remove profile CTB ctrl-protocol cisco-dtp\n"
            + "l2-cft protocol remove profile CTB ctrl-protocol cisco-stp-uplink-fast\n"
            + "l2-cft protocol remove profile CTB ctrl-protocol all-bridges-block\n";

    private L2VSIConfigCftConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = new L2VSIConfigCftConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest() {
        // create profile
        Assert.assertEquals("l2-cft create profile CTB\n", writer.writeTemplate(
            createConfig("CTB", null)));

        // create profile with protocols
        Assert.assertEquals("l2-cft create profile CTB\n" + PROFILES, writer.writeTemplate(
            createConfig("CTB", Arrays.asList(
                    new CtrlProtocolBuilder().setName(Name._8021x)
                            .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                    new CtrlProtocolBuilder().setName(Name.CiscoDtp)
                            .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                    new CtrlProtocolBuilder().setName(Name.AllBridgesBlock)
                            .setDisposition(CtrlProtocol.Disposition.Discard).build()))));
    }

    @Test
    public void updateTemplateTest() {
        // update protocols: add new protocols
        Assert.assertEquals(PROFILES, writer.updateTemplate(
                createConfig("CTB", null),
                createConfig("CTB", Arrays.asList(
                        new CtrlProtocolBuilder().setName(Name._8021x)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.CiscoDtp)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.AllBridgesBlock)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build()))));

        // update protocols: delete all protocols
        Assert.assertEquals(REMOVE_PROTOCOLS, writer.updateTemplate(
                createConfig("CTB", Arrays.asList(
                        new CtrlProtocolBuilder().setName(Name._8021x)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.CiscoDtp)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.CiscoStpUplinkFast)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.AllBridgesBlock)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build())),
                createConfig("CTB", null)));

        // update protocols: delete two protocols, update two protocols, create two protocols
        Assert.assertEquals(UPDATE_PROTOCOLS, writer.updateTemplate(
                createConfig("CTB", Arrays.asList(
                        new CtrlProtocolBuilder().setName(Name._8021x)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.CiscoDtp)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.CiscoStpUplinkFast)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.AllBridgesBlock)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build())),
                createConfig("CTB", Arrays.asList(
                        new CtrlProtocolBuilder().setName(Name.CiscoStpUplinkFast)
                                .setDisposition(CtrlProtocol.Disposition.Forward).build(),
                        new CtrlProtocolBuilder().setName(Name.AllBridgesBlock)
                                .setDisposition(CtrlProtocol.Disposition.Forward).build(),
                        new CtrlProtocolBuilder().setName(Name.GarpBlock)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build(),
                        new CtrlProtocolBuilder().setName(Name.Gvrp)
                                .setDisposition(CtrlProtocol.Disposition.Discard).build()))));
    }

    private Config createConfig(String cftName, List<CtrlProtocol> protocolList) {
        ConfigBuilder builder = new ConfigBuilder().setCftName(cftName);

        if (protocolList != null) {
            builder.setCtrlProtocols(new CtrlProtocolsBuilder().setCtrlProtocol(protocolList).build());
        }

        return builder.build();
    }
}
