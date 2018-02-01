package io.frinx.cli.ospf.handler;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;

public class AreaInterfaceReaderTest {


    private static final String OUTPUT = "interface GigabitEthernet1\n" +
            "interface GigabitEthernet2\n" +
            " ip ospf 99 area 7.7.7.7\n" +
            "interface GigabitEthernet3\n" +
            " ip ospf 991 area 7\n";

    @Test
    public void testAllIds() throws Exception {
        List<InterfaceKey> interfaceKeys = AreaInterfaceReader.parseInterfaceIds("99", OUTPUT, new OspfAreaIdentifier(new DottedQuad("7.7.7.7")));
        assertEquals(interfaceKeys, Lists.newArrayList(new InterfaceKey("GigabitEthernet2")));

        interfaceKeys = AreaInterfaceReader.parseInterfaceIds("99", OUTPUT, new OspfAreaIdentifier(new DottedQuad("7.7.7.8")));
        assertEquals(interfaceKeys, Collections.emptyList());

        interfaceKeys = AreaInterfaceReader.parseInterfaceIds("991", OUTPUT, new OspfAreaIdentifier(7L));
        assertEquals(interfaceKeys, Lists.newArrayList(new InterfaceKey("GigabitEthernet3")));
    }
}