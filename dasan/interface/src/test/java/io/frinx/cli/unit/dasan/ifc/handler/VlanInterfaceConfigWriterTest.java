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

package io.frinx.cli.unit.dasan.ifc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.IanaInterfaceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanInterfaceConfigWriterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;
    private VlanInterfaceConfigWriter target;
    private InstanceIdentifier<Config> id;
    private Config data;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new VlanInterfaceConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
    }

    private void prepare(Class<? extends IanaInterfaceType> ifType, String ifName) {
        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(ifName))
                .child(Config.class);

        data = new ConfigBuilder().setEnabled(Boolean.TRUE).setName(ifName).setType(ifType)
                .setMtu(Integer.valueOf(1000)).build();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        prepare(L3ipvlan.class, "Vlan100");

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testWriteCurrentAttributes_002() throws Exception {
        prepare(L3ipvlan.class, "Bundle8");

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_001() throws Exception {
        prepare(L3ipvlan.class, "Vlan100");
        Config newData = new ConfigBuilder(data).build();

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_002() throws Exception {
        prepare(EthernetCsmacd.class, "Vlan100");

        Config newData = new ConfigBuilder(data).setEnabled(Boolean.TRUE).setType(null).build();

        thrown.expect(IllegalArgumentException.class);

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributes_003() throws Exception {
        prepare(L3ipvlan.class, "Bundle8");
        Config newData = new ConfigBuilder(data).build();

        target.updateCurrentAttributes(id, data, newData, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        prepare(L3ipvlan.class, "Vlan100");

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }

    @Test
    public void testDeleteCurrentAttributes_002() throws Exception {
        prepare(L3ipvlan.class, "Bundle8");

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testValidateIfcNameAgainstType_001() throws Exception {

        prepare(EthernetCsmacd.class, "Vlan100");
        thrown.expect(InvocationTargetException.class);

        Method method = VlanInterfaceConfigWriter.class.getDeclaredMethod("validateIfcNameAgainstType", Config.class);
        method.setAccessible(true);
        method.invoke(null, data);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testWriteOrUpdateInterface_001() throws Exception {

        prepare(EthernetCsmacd.class, "Vlan100");

        Method method = VlanInterfaceConfigWriter.class.getDeclaredMethod("writeOrUpdateInterface", id.getClass(),
                Config.class, String.class);
        method.setAccessible(true);
        // test
        data = new ConfigBuilder().setEnabled(Boolean.FALSE).setName("Vlan100").build();
        method.invoke(target, id, data, "100");

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(Mockito.any());
    }
}
