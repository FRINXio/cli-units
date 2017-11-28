package io.frinx.cli.ospf.handler;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;

public class OspfAreaReaderTest {

    public static final String OSPF_1 = " Routing Process \"ospf 1\" with ID 10.255.255.1\n" +
            " Start time: 00:00:07.640, Time elapsed: 1w6d\n" +
            " Supports only single TOS(TOS0) routes\n" +
            " Supports opaque LSA\n" +
            " Supports Link-local Signaling (LLS)\n" +
            " Supports area transit capability\n" +
            " Supports NSSA (compatible with RFC 3101)\n" +
            " Event-log enabled, Maximum number of events: 1000, Mode: cyclic\n" +
            " It is an autonomous system boundary router\n" +
            " Redistributing External Routes from,\n" +
            " Router is not originating router-LSAs with maximum metric\n" +
            " Initial SPF schedule delay 5000 msecs\n" +
            " Minimum hold time between two consecutive SPFs 10000 msecs\n" +
            " Maximum wait time between two consecutive SPFs 10000 msecs\n" +
            " Incremental-SPF disabled\n" +
            " Minimum LSA interval 5 secs\n" +
            " Minimum LSA arrival 1000 msecs\n" +
            " LSA group pacing timer 240 secs\n" +
            " Interface flood pacing timer 33 msecs\n" +
            " Retransmission pacing timer 66 msecs\n" +
            " Number of external LSA 2. Checksum Sum 0x005C4C\n" +
            " Number of opaque AS LSA 0. Checksum Sum 0x000000\n" +
            " Number of DCbitless external and opaque AS LSA 0\n" +
            " Number of DoNotAge external and opaque AS LSA 0\n" +
            " Number of areas in this router is 1. 1 normal 0 stub 0 nssa\n" +
            " Number of areas transit capable is 0\n" +
            " External flood list length 0\n" +
            " IETF NSF helper support enabled\n" +
            " Cisco NSF helper support enabled\n" +
            " Reference bandwidth unit is 100 mbps\n" +
            "    Area BACKBONE(0)\n" +
            "        Number of interfaces in this area is 2 (1 loopback)\n" +
            "\tArea has no authentication\n" +
            "\tSPF algorithm last executed 1w5d ago\n" +
            "\tSPF algorithm executed 5 times\n" +
            "\tArea ranges are\n" +
            "\tNumber of LSA 3. Checksum Sum 0x017E32\n" +
            "\tNumber of opaque link LSA 0. Checksum Sum 0x000000\n" +
            "\tNumber of DCbitless LSA 0\n" +
            "\tNumber of indication LSA 0\n" +
            "\tNumber of DoNotAge LSA 0\n" +
            "\tFlood list length 0\n" +
            "    Area 48\n" +
            "        Number of interfaces in this area is 2 (1 loopback)\n" +
            "    Area BACKBONE(0.0.0.0)\n" +
            "    Area 9.9.9.9";

    public static final List<AreaKey> AREAS = Lists.newArrayList("0", "48", "0.0.0.0", "9.9.9.9")
            .stream()
            .map(OspfAreaReader::getAreaIdentifier)
            .map(AreaKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseArea() throws Exception {
        List<AreaKey> areaKeys = OspfAreaReader.parseAreasIds(OSPF_1);
        assertEquals(AREAS, areaKeys);
    }
}