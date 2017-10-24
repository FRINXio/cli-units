package io.frinx.cli.unit.ios.cdp.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.State;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.neighbor.StateBuilder;

public class NeighborStateReaderTest {


    private static final String IOS_OUTPUT = "Device ID: TELNET\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): FastEthernet0/0\n" +
            "Device ID: XE2.FRINX\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): GigabitEthernet1\n" +
            "Device ID: PE1.demo.frinx.io\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): MgmtEth0/0/CPU0/0\n" +
            "Device ID: R2.FRINX.LOCAL\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): FastEthernet0/0\n" +
            "Device ID: PE2.demo.frinx.io\n" +
            "Interface: FastEthernet0/0,  Port ID (outgoing port): MgmtEth0/0/CPU0/0\n";

    private static final State IOS_EXPECTED1 = new StateBuilder()
            .setId("PE2.demo.frinx.io")
            .setPortId("MgmtEth0/0/CPU0/0")
            .build();

    private static final State IOS_EXPECTED2 = new StateBuilder()
            .setId("TELNET")
            .setPortId("FastEthernet0/0")
            .build();

    private static final String XE_OUTPUT = "Device ID: TELNET\n" +
            "Interface: GigabitEthernet1,  Port ID (outgoing port): FastEthernet0/0\n" +
            "Device ID: XE1.FRINX\n" +
            "Interface: GigabitEthernet1,  Port ID (outgoing port): GigabitEthernet1\n" +
            "Device ID: PE1.demo.frinx.io\n" +
            "Interface: GigabitEthernet1,  Port ID (outgoing port): MgmtEth0/0/CPU0/0\n" +
            "Device ID: R121.FRINX.LOCAL\n" +
            "Interface: GigabitEthernet1,  Port ID (outgoing port): FastEthernet0/0\n" +
            "Device ID: R2.FRINX.LOCAL\n" +
            "Interface: GigabitEthernet1,  Port ID (outgoing port): FastEthernet0/0\n" +
            "Device ID: PE2.demo.frinx.io\n" +
            "Interface: GigabitEthernet1,  Port ID (outgoing port): MgmtEth0/0/CPU0/0\n";

    private static final State XE_EXPECTED = new StateBuilder()
            .setId("TELNET")
            .setPortId("FastEthernet0/0")
            .build();

    @Test
    public void parseNeighborStateFields() throws Exception {
        StateBuilder stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(stateBuilder, IOS_OUTPUT, "PE2.demo.frinx.io");
        assertEquals(IOS_EXPECTED1, stateBuilder.build());

        stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(stateBuilder, IOS_OUTPUT, "TELNET");
        assertEquals(IOS_EXPECTED2, stateBuilder.build());

        stateBuilder = new StateBuilder();
        NeighborStateReader.parseNeighborStateFields(stateBuilder, XE_OUTPUT, "TELNET");
        assertEquals(XE_EXPECTED, stateBuilder.build());
    }

}