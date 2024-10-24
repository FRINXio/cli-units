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

import static org.hamcrest.MatcherAssert.assertThat;

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
import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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

@Disabled
@ExtendWith(MockitoExtension.class)
class VlanReaderTest {
    private static String SHOW_VLAN_CREATE;

    @Mock
    private Cli cli;

//    @InjectMocks
    private VlanReader target;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        SHOW_VLAN_CREATE = (String) FieldUtils.readStaticField(VlanReader.class, "SHOW_VLAN_CREATE", true);
    }

    @AfterAll
    static void tearDownAfterClass() {
    }

    @BeforeEach
    void setUp() {
        target = Mockito.spy(new VlanReader(cli));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testGetAllIds_A_001() throws Exception {

        final InstanceIdentifier<Vlan> instanceIdentifier =
                InstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(new VlanId(100)));
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        List<VlanKey> vlanKeys = new ArrayList<>();

        try (MockedStatic<VlanReader> mock = Mockito.mockStatic(VlanReader.class)) {
            mock.when(() -> VlanReader.getAllIds(cli, target, instanceIdentifier, readContext)).thenReturn(vlanKeys);


            List<VlanKey> result = target.getAllIds(instanceIdentifier, readContext);


            assertThat(result, CoreMatchers.sameInstance(vlanKeys));

            mock.verify(() -> VlanReader.getAllIds(cli, target, instanceIdentifier, readContext));
        }
    }

    @Test
    void testGetAllIds_A_002() throws Exception {

        final InstanceIdentifier<Vlan> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, new NetworkInstanceKey("not-default"))
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(new VlanId(100)));
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        List<VlanKey> vlanKeys = new LinkedList<>();
        vlanKeys.add(new VlanKey(new VlanId(1)));
        vlanKeys.add(new VlanKey(new VlanId(2)));

        try (MockedStatic<VlanReader> mock = Mockito.mockStatic(VlanReader.class)) {
            mock.when(() -> VlanReader.getAllIds(cli, target, instanceIdentifier, readContext)).thenReturn(vlanKeys);


            List<VlanKey> result = target.getAllIds(instanceIdentifier, readContext);


            assertThat(result, CoreMatchers.equalTo(Collections.emptyList()));

            mock.verify(() -> VlanReader.getAllIds(cli, target, instanceIdentifier, readContext), Mockito.never());
        }
    }

    @Test
    void testGetAllIds_B_001() throws Exception {
        final VlanReader cliReader = Mockito.mock(VlanReader.class);
        final InstanceIdentifier<Vlan> instanceIdentifier = InstanceIdentifier.create(Vlan.class);
        final InstanceIdentifier<Vlan> cuttedIid = InstanceIdentifier.create(Vlan.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);
        final String cliOutput = StringUtils.join(new String[] {
            "vlan create 1,200,3 eline",
            "vlan create 101,201,301",
        }, "\n");

        final List<VlanKey> vlanKeys = new ArrayList<>();

        try (MockedStatic<VlanReader> mock = Mockito.mockStatic(VlanReader.class, Mockito.CALLS_REAL_METHODS)) {
            mock.when(() -> VlanReader.parseVlanIds(cliOutput)).thenReturn(vlanKeys);

            Mockito.doReturn(cliOutput).when(cliReader)
                    .blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);


            List<VlanKey> result = VlanReader.getAllIds(cli, cliReader, instanceIdentifier, readContext);


            assertThat(result, CoreMatchers.sameInstance(vlanKeys));

            Mockito.verify(cliReader).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);

            mock.verify(() -> VlanReader.parseVlanIds(cliOutput));
        }
    }

    @Test
    void testGetAllIds_B_002() throws Exception {
        final CliReader cliReader = Mockito.mock(VlanReader.class);
        final InstanceIdentifier<?> instanceIdentifier = InstanceIdentifier.create(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan
                    .top.vlans.vlan.Config.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final String cliOutput = StringUtils.join(new String[] {
            "vlan create 1,200,3 eline",
            "vlan create 101,201,301",
        }, "\n");
        final List<VlanKey> vlanKeys = new ArrayList<>();

        try (MockedStatic<RWUtils> rwUtilsMock = Mockito.mockStatic(RWUtils.class);
            MockedStatic<VlanReader> vlanReaderMock = Mockito
                    .mockStatic(VlanReader.class, Mockito.CALLS_REAL_METHODS)) {
            vlanReaderMock.when(() -> VlanReader.parseVlanIds(cliOutput)).thenReturn(vlanKeys);

            Mockito.doReturn(cliOutput).when(cliReader)
                    .blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);


            List<VlanKey> result = VlanReader.getAllIds(cli, cliReader, instanceIdentifier, readContext);


            assertThat(result, CoreMatchers.sameInstance(vlanKeys));

            Mockito.verify(cliReader).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);

            vlanReaderMock.verify(() -> VlanReader.parseVlanIds(cliOutput));
        }
    }

    @Test
    void testParseVlanIds_001() throws Exception {
        String output = StringUtils.join(new String[] {
            " vlan create 1129,1133 eline",
            " vlan create 10,20-21,33-35",
        }, "\n");

        //test
        List<VlanKey> result = VlanReader.parseVlanIds(output);

        //verify
        assertThat(result.stream()
            .map(m -> m.getVlanId().getValue()).collect(Collectors.toList()),
                CoreMatchers.is(IsIterableContainingInAnyOrder.containsInAnyOrder(
                    1129,
                    1133,
                    10,
                    20, 21,
                    33, 34, 35
                )));
    }

    @Test
    void testParseVlanIds_002() {
        String output = ""; // output is empty

        //test
        List<VlanKey> result = VlanReader.parseVlanIds(output);

        //verify
        assertThat(result.size(), CoreMatchers.is(0));
    }

    @Test
    void testReadCurrentAttributes_001() throws Exception {
        final VlanId vlanId = new VlanId(100);
        final InstanceIdentifier<Vlan> instanceIdentifier =
                KeyedInstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, new VlanKey(vlanId));
        final VlanBuilder vlanBuilder = Mockito.mock(VlanBuilder.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        Mockito.doReturn(vlanBuilder).when(vlanBuilder).setVlanId(vlanId);


        target.readCurrentAttributes(instanceIdentifier, vlanBuilder, readContext);


        Mockito.verify(vlanBuilder).setVlanId(vlanId);
    }

    @Test
    void testMerge_001() {

        VlansBuilder builder = Mockito.mock(VlansBuilder.class);
        List<Vlan> vlans = new ArrayList<>();


        Mockito.doReturn(builder).when(builder).setVlan(vlans);


        target.merge(builder, vlans);


        Mockito.verify(builder).setVlan(vlans);
    }
}
