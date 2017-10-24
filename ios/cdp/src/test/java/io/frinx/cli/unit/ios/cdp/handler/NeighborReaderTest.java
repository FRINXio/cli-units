package io.frinx.cli.unit.ios.cdp.handler;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;

public class NeighborReaderTest {

    private static final String IOS_OUTPUT = "Device ID: TELNET\n" +
            "    Device ID: XE2.FRINX\n" +
            "    Device ID: PE1.demo.frinx.io\n" +
            "    Device ID: R2.FRINX.LOCAL\n" +
            "    Device ID: PE2.demo.frinx.io";

    private static final List<NeighborKey> IOS_EXPECTED = Lists.newArrayList("TELNET", "XE2.FRINX", "PE1.demo.frinx.io", "R2.FRINX.LOCAL", "PE2.demo.frinx.io")
            .stream()
            .map(NeighborKey::new)
            .collect(Collectors.toList());

    @Test
    public void parseNeighborIds() throws Exception {
        assertEquals(IOS_EXPECTED, NeighborReader.parseNeighborIds(IOS_OUTPUT));
    }

}