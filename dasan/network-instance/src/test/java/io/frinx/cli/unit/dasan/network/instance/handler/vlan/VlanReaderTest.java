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

package io.frinx.cli.unit.dasan.network.instance.handler.vlan;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.Vlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.VlansBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class VlanReaderTest {
    private static String SHOW_VLAN_CREATE;

    @Mock
    private Cli cli;

//    @InjectMocks
    private VlanReader target;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        SHOW_VLAN_CREATE = (String) FieldUtils.readStaticField(VlanReader.class, "SHOW_VLAN_CREATE", true);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = spy(new VlanReader(cli));
    }

    @After
    public void tearDown() throws Exception {
    }

    @PrepareOnlyThisForTest(VlanReader.class)
    @Test
    public void testGetAllIds_A_001() throws Exception {

        final InstanceIdentifier<Vlan> instanceIdentifier =
                InstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(new VlanId(Integer.valueOf(100))));
        final ReadContext readContext = mock(ReadContext.class);
        List<VlanKey> vlanKeys = new ArrayList<>();

        PowerMockito.mockStatic(VlanReader.class);
        PowerMockito.when(VlanReader.getAllIds(cli, target, instanceIdentifier, readContext)).thenReturn(vlanKeys);


        List<VlanKey> result = target.getAllIds(instanceIdentifier, readContext);


        assertThat(result, sameInstance(vlanKeys));

        PowerMockito.verifyStatic();
        VlanReader.getAllIds(cli, target, instanceIdentifier, readContext);
    }

    @PrepareOnlyThisForTest(VlanReader.class)
    @Test
    public void testGetAllIds_A_002() throws Exception {

        final InstanceIdentifier<Vlan> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, new NetworkInstanceKey("not-default"))
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(new VlanId(Integer.valueOf(100))));
        final ReadContext readContext = mock(ReadContext.class);
        List<VlanKey> vlanKeys = new LinkedList<VlanKey>() {
            {
                add(new VlanKey(new VlanId(Integer.valueOf(1))));
                add(new VlanKey(new VlanId(Integer.valueOf(2))));
            }
        };

        PowerMockito.mockStatic(VlanReader.class);
        PowerMockito.when(VlanReader.getAllIds(cli, target, instanceIdentifier, readContext)).thenReturn(vlanKeys);


        List<VlanKey> result = target.getAllIds(instanceIdentifier, readContext);


        assertThat(result, equalTo(Collections.emptyList()));

        PowerMockito.verifyStatic(never());
        VlanReader.getAllIds(cli, target, instanceIdentifier, readContext);
    }

    @PrepareOnlyThisForTest({RWUtils.class, VlanReader.class})
    @Test
    public void testGetAllIds_B_001() throws Exception {
        final VlanReader cliReader = mock(VlanReader.class);
        final InstanceIdentifier<Vlan> instanceIdentifier = InstanceIdentifier.create(Vlan.class);
        final InstanceIdentifier<Vlan> cuttedIid = InstanceIdentifier.create(Vlan.class);
        final ReadContext readContext = mock(ReadContext.class);
        final String cliOutput = StringUtils.join(new String[] {
            "vlan create 1,200,3 eline",
            "vlan create 101,201,301",
        }, "\n");

        final List<VlanKey> vlanKeys = new ArrayList<>();

        PowerMockito.mockStatic(VlanReader.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.doReturn(vlanKeys).when(VlanReader.class, "parseVlanIds", cliOutput);

        Mockito.doReturn(cliOutput).when(cliReader)
            .blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);


        List<VlanKey> result = VlanReader.getAllIds(cli, cliReader, instanceIdentifier, readContext);


        assertThat(result, sameInstance(vlanKeys));

        Mockito.verify(cliReader).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);

        PowerMockito.verifyStatic();
        VlanReader.parseVlanIds(cliOutput);
    }

    @PrepareOnlyThisForTest({RWUtils.class, VlanReader.class})
    @Test
    public void testGetAllIds_B_002() throws Exception {
        final CliReader cliReader = mock(VlanReader.class);
        final InstanceIdentifier<?> instanceIdentifier = InstanceIdentifier.create(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan
                    .top.vlans.vlan.Config.class);
        final ReadContext readContext = mock(ReadContext.class);

        final String cliOutput = StringUtils.join(new String[] {
            "vlan create 1,200,3 eline",
            "vlan create 101,201,301",
        }, "\n");
        final List<VlanKey> vlanKeys = new ArrayList<>();

        PowerMockito.mockStatic(RWUtils.class);

        PowerMockito.mockStatic(VlanReader.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.doReturn(vlanKeys).when(VlanReader.class, "parseVlanIds", cliOutput);

        Mockito.doReturn(cliOutput).when(cliReader)
            .blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);


        List<VlanKey> result = VlanReader.getAllIds(cli, cliReader, instanceIdentifier, readContext);


        assertThat(result, sameInstance(vlanKeys));

        Mockito.verify(cliReader).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);

        PowerMockito.verifyStatic();
        VlanReader.parseVlanIds(cliOutput);
    }

    @Test
    public void testParseVlanIds_001() throws Exception {
        String output = StringUtils.join(new String[] {
            " vlan create 1129,1133 eline",
            " vlan create 10,20-21,33-35",
        }, "\n");

        //test
        List<VlanKey> result = VlanReader.parseVlanIds(output);

        //verify
        assertThat(result.stream()
            .map(m -> m.getVlanId().getValue()).collect(Collectors.toList()),
            is(containsInAnyOrder(
                Integer.valueOf(1129),
                Integer.valueOf(1133),
                Integer.valueOf(10),
                Integer.valueOf(20),Integer.valueOf(21),
                Integer.valueOf(33),Integer.valueOf(34),Integer.valueOf(35)
                )));
    }

    @Test
    public void testParseVlanIds_002() throws Exception {
        String output = ""; // output is empty

        //test
        List<VlanKey> result = VlanReader.parseVlanIds(output);

        //verify
        assertThat(result.size(), is(0));
    }

    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final VlanId vlanId = new VlanId(Integer.valueOf(100));
        final InstanceIdentifier<Vlan> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(vlanId));
        final VlanBuilder vlanBuilder = mock(VlanBuilder.class);
        final ReadContext readContext = mock(ReadContext.class);

        Mockito.doReturn(vlanBuilder).when(vlanBuilder).setVlanId(vlanId);


        target.readCurrentAttributes(instanceIdentifier, vlanBuilder, readContext);


        Mockito.verify(vlanBuilder).setVlanId(vlanId);
    }

    @Test
    public void testMerge_001() throws Exception {

        VlansBuilder builder = mock(VlansBuilder.class);
        List<Vlan> vlans = new ArrayList<>();


        Mockito.doReturn(builder).when(builder).setVlan(vlans);


        target.merge(builder, vlans);


        Mockito.verify(builder).setVlan(vlans);
    }
}
