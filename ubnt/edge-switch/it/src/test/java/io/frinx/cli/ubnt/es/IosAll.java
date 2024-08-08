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
package io.frinx.cli.ubnt.es;

import io.frinx.cli.registry.impl.TranslateRegistryImpl;
import io.frinx.cli.unit.generic.GenericTranslateUnit;
import io.frinx.cli.unit.ubnt.es.init.UbntEsInitializerUnit;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.unit.generic.rev191119.ExecuteAndReadInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.unit.generic.rev191119.ExecuteAndReadInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.unit.generic.rev191119.ExecuteAndReadOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class IosAll extends io.frinx.cli.unit.ios.it.IosAll {

    private static final Device IOS_ALL = new DeviceIdBuilder()
            .setDeviceType("ubnt es")
            .setDeviceVersion("1.8.2")
            .build();

    private static final int PORT = 22;
    private static final String HOST = "10.1.133.121";
    private static final InetSocketAddress IOS_ADDR = new InetSocketAddress(HOST, PORT);

    public IosAll() {
        // Set false to not fail immediately when a read exception occurs
        this.failFast = false;
    }

    private static final CliNode CLI_CFG = new CliNodeBuilder()
            .setPort(new PortNumber(PORT))
            .setHost(new Host(new IpAddress(new Ipv4Address(HOST))))
            .setDeviceType(IOS_ALL.getDeviceType())
            .setDeviceVersion(IOS_ALL.getDeviceVersion())
            .setTransportType(CliNodeConnectionParameters.TransportType.Ssh)
            .setCredentials(new LoginPasswordBuilder()
                    .setUsername("ubnt")
                    .setPassword("ubnt")
                    .build())
            .setKeepaliveStrategy(new KeepaliveBuilder()
                    .setKeepaliveDelay(30)
                    .setKeepaliveTimeout(30)
                    .setKeepaliveInitialDelay(30)
                    .build())
            .build();


    @Override
    protected Device getDeviceId() {
        return IOS_ALL;
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
        new UbntEsInitializerUnit(reg).init();

        return reg;
    }

    @Disabled
    @Test
    public void testConnectivity() throws Exception {
        super.testConnectivity();
    }

    @Disabled
    @Test
    void testRpc() throws Exception {
        QName qname = QName.create(ExecuteAndReadInput.QNAME, "execute-and-read").intern();
        SchemaPath schemaPath = SchemaPath.create(true, qname);

        CompletionStage invoke = this.rpcReg.invoke(schemaPath, new ExecuteAndReadInputBuilder()
                .setCommand("show running-config")
                .build());

        ExecuteAndReadOutput output = (ExecuteAndReadOutput) invoke.toCompletableFuture().get();
    }

    @Disabled
    @Test
    public void getInterfacesBA() throws Exception {
        super.getInterfacesBA();
    }

    @Disabled
    @Test
    public void getAllDOM() throws Exception {
        super.getAllDOM();
    }

    @Disabled
    @Test
    public void getAllDOMBenchmark() throws Exception {
        super.getAllDOMBenchmark();
    }
}
