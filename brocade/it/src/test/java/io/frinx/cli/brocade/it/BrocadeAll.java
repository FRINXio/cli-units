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
package io.frinx.cli.brocade.it;

import io.frinx.cli.registry.impl.TranslateRegistryImpl;
import io.frinx.cli.unit.brocade.conf.ConfigurationUnit;
import io.frinx.cli.unit.brocade.ifc.BrocadeInterfaceUnit;
import io.frinx.cli.unit.brocade.init.BrocadeCliInitializerUnit;
import io.frinx.cli.unit.brocade.isis.BrocadeIsisUnit;
import io.frinx.cli.unit.brocade.network.instance.BrocadeNetworkInstanceUnit;
import io.frinx.cli.unit.brocade.stp.BrocadeStpUnit;
import io.frinx.cli.unit.generic.GenericTranslateUnit;
import java.net.InetSocketAddress;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNodeConnectionParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPasswordBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.keepalive.keepalive.strategy.KeepaliveBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;

public class BrocadeAll extends io.frinx.cli.unit.ios.it.IosAll {

    private static final Device BROCADE = new DeviceIdBuilder()
            .setDeviceType("ironware")
            .setDeviceVersion("*")
            .build();

    private static final int PORT = 23;
    private static final String HOST = "213.143.100.21";
    private static final InetSocketAddress IOS_ADDR = new InetSocketAddress(HOST, PORT);

    public BrocadeAll() {
        // Set false to not fail immediately when a read exception occurs
        this.failFast = false;
    }

    private static final CliNode CLI_CFG = new CliNodeBuilder()
            .setPort(new PortNumber(PORT))
            .setHost(new Host(new IpAddress(new Ipv4Address(HOST))))
            .setDeviceType(BROCADE.getDeviceType())
            .setDeviceVersion(BROCADE.getDeviceVersion())
            .setTransportType(CliNodeConnectionParameters.TransportType.Telnet)
            .setCredentials(new LoginPasswordBuilder()
                    .setUsername("frinx")
                    .setPassword("Igasp4WE!")
                    .build())
            .setKeepaliveStrategy(new KeepaliveBuilder()
                    .setKeepaliveDelay(30)
                    .setKeepaliveTimeout(30)
                    .setKeepaliveInitialDelay(30)
                    .build())
            .build();


    @Override
    protected Device getDeviceId() {
        return BROCADE;
    }

    @Override
    protected CliNode getCliNode() {
        return CLI_CFG;
    }

    protected InetSocketAddress getAddress() {
        return IOS_ADDR;
    }

    @Override
    protected TranslateRegistryImpl getTranslateRegistry(DataBroker mockBroker) {
        TranslateRegistryImpl reg = new TranslateRegistryImpl(mockBroker);

        new GenericTranslateUnit(reg).init();
        new BrocadeCliInitializerUnit(reg).init();
        new ConfigurationUnit(reg).init();
        new BrocadeStpUnit(reg).init();
        new BrocadeIsisUnit(reg).init();
        new BrocadeInterfaceUnit(reg).init();
        new BrocadeNetworkInstanceUnit(reg).init();

        return reg;
    }

    @Ignore
    @Test
    public void testConnectivity() throws Exception {
        super.testConnectivity();
    }

    @Ignore
    @Test
    public void getInterfacesBA() throws Exception {
        super.getInterfacesBA();
    }

    @Ignore
    @Test
    public void getAllDOM() throws Exception {
        super.getAllDOM();
    }

    @Ignore
    @Test
    public void getAllDOMBenchmark() throws Exception {
        super.getAllDOMBenchmark();
    }
}
