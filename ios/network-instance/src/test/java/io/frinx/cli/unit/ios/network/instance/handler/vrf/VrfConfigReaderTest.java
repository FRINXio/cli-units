package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import static io.frinx.cli.unit.utils.ParsingUtils.parseField;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;

public class VrfConfigReaderTest {

    public static final String OUTPUT = "ip vrf abcd\n" +
            " rd 65002:1\n" +
            "!\n" +
            "!\n" +
            "interface GigabitEthernet2\n" +
            " ip vrf forwarding abcd\n" +
            " no ip address\n" +
            " ip ospf cost 5478\n" +
            " shutdown\n" +
            " negotiation auto\n" +
            " no keepalive\n" +
            "!\n" +
            "router bgp 65002\n" +
            " !\n" +
            " address-family ipv4 vrf abcd\n" +
            "  bgp router-id 17.18.19.20\n" +
            "  network 10.99.20.0 mask 255.255.255.0\n" +
            "  network 10.99.21.0 mask 255.255.255.0\n" +
            "  neighbor 1.2.3.4 remote-as 444\n" +
            "  neighbor 1.2.3.4 activate\n" +
            "  neighbor 87.87.87.87 remote-as 45\n" +
            "  neighbor 87.87.87.87 activate\n" +
            " exit-address-family\n" +
            "!\n" +
            "end\n" +
            "\n";

    @Test
    public void testRd() throws Exception {
        ConfigBuilder builder = new ConfigBuilder();
        parseField(OUTPUT,
                VrfConfigReader.RD_LINE::matcher,
                matcher -> matcher.group("rd"),
                rd -> builder.setRouteDistinguisher(new RouteDistinguisher(rd)));

        assertEquals(builder.build().getRouteDistinguisher().getString(), "65002:1");
    }
}