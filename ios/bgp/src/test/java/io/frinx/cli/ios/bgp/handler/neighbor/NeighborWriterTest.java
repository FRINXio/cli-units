package io.frinx.cli.ios.bgp.handler.neighbor;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonNeighborGroupTransportConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.TransportBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Address;

public class NeighborWriterTest implements CliFormatter {

    private static final Neighbor NEIGH_1 = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
            .setConfig(new ConfigBuilder()
                    .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
                    .setPeerAs(new AsNumber(45L))
                    .build())
            .setTransport(new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.ConfigBuilder()
                            .setLocalAddress(new BgpCommonNeighborGroupTransportConfig.LocalAddress("Loopback0"))
                            .build())
                    .build())
            .setApplyPolicy(new ApplyPolicyBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder()
                            .setExportPolicy(Lists.newArrayList("export1", "export2"))
                            .setImportPolicy(Lists.newArrayList("import1", "import2"))
                            .build())
                    .build())
            .build();

    private static final Neighbor NEIGH_2 = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
            .setConfig(new ConfigBuilder()
                    .setNeighborAddress(new IpAddress(new Ipv4Address("1.2.3.4")))
                    .setPeerAs(new AsNumber(45L))
                    .build())
            .setTransport(new TransportBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.ConfigBuilder()
                            .setLocalAddress(new BgpCommonNeighborGroupTransportConfig.LocalAddress("Loopback0"))
                            .build())
                    .build())
            .build();

    private static final Neighbor NEIGH_3 = new NeighborBuilder()
            .setNeighborAddress(new IpAddress(new Ipv6Address("::45")))
            .setConfig(new ConfigBuilder()
                    .setNeighborAddress(new IpAddress(new Ipv6Address("::45")))
                    .setPeerAs(new AsNumber(45L))
                    .build())
            .build();

    @Test
    public void testGlobal() throws Exception {

        String config = fT(NeighborWriter.NEIGHBOR_GLOBAL,
                "as", 484,
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_1.getNeighborAddress()),
                "neighbor", NEIGH_1
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "neighbor 1.2.3.4 remote-as 45\n" +
                "neighbor 1.2.3.4 update-source Loopback0\n" +
                "neighbor 1.2.3.4 route-map import1 in\n" +
                "neighbor 1.2.3.4 route-map import2 in\n" +
                "neighbor 1.2.3.4 route-map export1 out\n" +
                "neighbor 1.2.3.4 route-map export2 out\n" +
                "neighbor 1.2.3.4 activate\n" +
                "end", config);
        String delete = fT(NeighborWriter.NEIGHBOR_GLOBAL_DELETE,
                "as", 484,
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_1.getNeighborAddress()),
                "neighbor", NEIGH_1
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "no neighbor 1.2.3.4 activate\n" +
                "no neighbor 1.2.3.4 remote-as 45\n" +
                "end", delete);


        config = fT(NeighborWriter.NEIGHBOR_GLOBAL,
                "as", 484,
                "afi_safi", "vpnv4",
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_1.getNeighborAddress()),
                "neighbor", NEIGH_1
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "neighbor 1.2.3.4 remote-as 45\n" +
                "neighbor 1.2.3.4 update-source Loopback0\n" +
                "address-family vpnv4\n" +
                "neighbor 1.2.3.4 route-map import1 in\n" +
                "neighbor 1.2.3.4 route-map import2 in\n" +
                "neighbor 1.2.3.4 route-map export1 out\n" +
                "neighbor 1.2.3.4 route-map export2 out\n" +
                "neighbor 1.2.3.4 activate\n" +
                "exit\n" +
                "end", config);

        config = fT(NeighborWriter.NEIGHBOR_GLOBAL_DELETE,
                "as", 484,
                "afi_safi", "vpnv4",
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_1.getNeighborAddress()),
                "neighbor", NEIGH_1
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "address-family vpnv4\n" +
                "no neighbor 1.2.3.4 activate\n" +
                "exit\n" +
                "no neighbor 1.2.3.4 remote-as 45\n" +
                "end", config);

        config = fT(NeighborWriter.NEIGHBOR_GLOBAL,
                "as", 484,
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_2.getNeighborAddress()),
                "neighbor", NEIGH_2
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "neighbor 1.2.3.4 remote-as 45\n" +
                "neighbor 1.2.3.4 update-source Loopback0\n" +
                "neighbor 1.2.3.4 activate\n" +
                "end", config);


        config = fT(NeighborWriter.NEIGHBOR_GLOBAL,
                "as", 484,
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_3.getNeighborAddress()),
                "neighbor", NEIGH_3
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "neighbor ::45 remote-as 45\n" +
                "neighbor ::45 activate\n" +
                "end", config);


        config = fT(NeighborWriter.NEIGHBOR_GLOBAL,
                "as", 484,
                "afi_safi", "ipv4",
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_3.getNeighborAddress()),
                "neighbor", NEIGH_3
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "neighbor ::45 remote-as 45\n" +
                "address-family ipv4\n" +
                "neighbor ::45 activate\n" +
                "exit\n" +
                "end", config);
    }

    @Test
    public void testVRF() throws Exception {

        String config = fT(NeighborWriter.NEIGHBOR_VRF,
                "as", 484,
                "vrf", "CUST1",
                "afi_safi", "ipv4",
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_1.getNeighborAddress()),
                "neighbor", NEIGH_1
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "address-family ipv4 vrf CUST1\n" +
                "neighbor 1.2.3.4 remote-as 45\n" +
                "neighbor 1.2.3.4 route-map import1 in\n" +
                "neighbor 1.2.3.4 route-map import2 in\n" +
                "neighbor 1.2.3.4 route-map export1 out\n" +
                "neighbor 1.2.3.4 route-map export2 out\n" +
                "neighbor 1.2.3.4 update-source Loopback0\n" +
                "neighbor 1.2.3.4 activate\n" +
                "end", config);
        String delete = fT(NeighborWriter.NEIGHBOR_VRF_DELETE,
                "as", 484,
                "vrf", "CUST1",
                "afi_safi", "ipv4",
                "neighbor_ip", NeighborWriter.getNeighborIp(NEIGH_1.getNeighborAddress()),
                "neighbor", NEIGH_1
        );
        Assert.assertEquals("configure terminal\n" +
                "router bgp 484\n" +
                "address-family ipv4 vrf CUST1\n" +
                "no neighbor 1.2.3.4 activate\n" +
                "no neighbor 1.2.3.4 remote-as 45\n" +
                "end", delete);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingMandatolryFields() throws Exception {
        fT(NeighborWriter.NEIGHBOR_GLOBAL,
                "as", 484
        );
    }
}