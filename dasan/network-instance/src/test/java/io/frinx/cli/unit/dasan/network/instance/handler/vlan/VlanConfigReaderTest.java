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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class VlanConfigReaderTest {
    private static final String SHOW_VLAN_CREATE = VlanConfigReader.SHOW_VLAN_CREATE;

    private static final String VLAN_CREATE_OUTPUT = StringUtils.join(new String[] {
        "vlan create 100 eline",
        "vlan create 200",
    }, "\n");

    @Mock
    private Cli cli;

    private VlanConfigReader target;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new VlanConfigReader(cli));
    }

    @After
    public void tearDown() throws Exception {
    }

    @PrepareOnlyThisForTest({VlanConfigReader.class})
    @Test
    public void testReadCurrentAttributes_001() throws Exception {
        final Integer vlanIdValue = Integer.valueOf(100);
        final VlanId vlanId = new VlanId(vlanIdValue);
        final VlanKey vlanKey = new VlanKey(vlanId);

        final InstanceIdentifier<Config> instanceIdentifier =
                InstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, vlanKey)
                .child(Config.class);
        final ConfigBuilder configBuilder = Mockito.mock(ConfigBuilder.class);
        final ReadContext   readContext   = Mockito.mock(ReadContext.class);
        final String cliOutput = VLAN_CREATE_OUTPUT;

        PowerMockito.mockStatic(VlanConfigReader.class);
        PowerMockito.doNothing().when(VlanConfigReader.class, "parseVlanConfig", cliOutput, configBuilder, vlanId);
        Mockito.doReturn(cliOutput).when(target).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);
        Mockito.doReturn(Boolean.TRUE).when(target).isVlan(instanceIdentifier, readContext, vlanId);


        target.readCurrentAttributes(instanceIdentifier, configBuilder , readContext);


        PowerMockito.verifyStatic();
        VlanConfigReader.parseVlanConfig(cliOutput, configBuilder, vlanId);
        Mockito.verify(target).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);
        Mockito.verify(target).isVlan(instanceIdentifier, readContext, vlanId);
    }

    @PrepareOnlyThisForTest({VlanConfigReader.class})
    @Test
    public void testReadCurrentAttributes_002() throws Exception {
        final Integer vlanIdValue = Integer.valueOf(100);
        final VlanId vlanId = new VlanId(vlanIdValue);
        final VlanKey vlanKey = new VlanKey(vlanId);

        final InstanceIdentifier<Config> instanceIdentifier =
                InstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, new NetworkInstanceKey("not-default"))
                .child(Vlans.class)
                .child(Vlan.class, vlanKey)
                .child(Config.class);
        final ConfigBuilder configBuilder = Mockito.mock(ConfigBuilder.class);
        final ReadContext   readContext   = Mockito.mock(ReadContext.class);
        final String cliOutput = VLAN_CREATE_OUTPUT;

        PowerMockito.mockStatic(VlanConfigReader.class);
        PowerMockito.doNothing().when(VlanConfigReader.class, "parseVlanConfig", cliOutput, configBuilder, vlanId);
        Mockito.doReturn(cliOutput).when(target).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);
        Mockito.doReturn(Boolean.TRUE).when(target).isVlan(instanceIdentifier, readContext, vlanId);


        target.readCurrentAttributes(instanceIdentifier, configBuilder , readContext);


        PowerMockito.verifyStatic(Mockito.never());
        VlanConfigReader.parseVlanConfig(cliOutput, configBuilder, vlanId);
        Mockito.verify(target, Mockito.never()).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);
        Mockito.verify(target, Mockito.never()).isVlan(instanceIdentifier, readContext, vlanId);
    }

    @PrepareOnlyThisForTest({VlanConfigReader.class})
    @Test
    public void testReadCurrentAttributes_003() throws Exception {
        final Integer vlanIdValue = Integer.valueOf(100);
        final VlanId vlanId = new VlanId(vlanIdValue);
        final VlanKey vlanKey = new VlanKey(vlanId);

        final InstanceIdentifier<Config> instanceIdentifier =
                InstanceIdentifier.create(NetworkInstances.class)
                .child(NetworkInstance.class, NetworInstance.DEFAULT_NETWORK)
                .child(Vlans.class)
                .child(Vlan.class, vlanKey)
                .child(Config.class);
        final ConfigBuilder configBuilder = Mockito.mock(ConfigBuilder.class);
        final ReadContext   readContext   = Mockito.mock(ReadContext.class);
        final String cliOutput = VLAN_CREATE_OUTPUT;

        PowerMockito.mockStatic(VlanConfigReader.class);
        PowerMockito.doNothing().when(VlanConfigReader.class, "parseVlanConfig", cliOutput, configBuilder, vlanId);
        Mockito.doReturn(cliOutput).when(target).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);
        Mockito.doReturn(Boolean.FALSE).when(target).isVlan(instanceIdentifier, readContext, vlanId);


        target.readCurrentAttributes(instanceIdentifier, configBuilder , readContext);


        PowerMockito.verifyStatic(Mockito.never());
        VlanConfigReader.parseVlanConfig(cliOutput, configBuilder, vlanId);
        Mockito.verify(target, Mockito.never()).blockingRead(SHOW_VLAN_CREATE, cli, instanceIdentifier, readContext);
        Mockito.verify(target).isVlan(instanceIdentifier, readContext, vlanId);
    }

    @PrepareOnlyThisForTest({VlanReader.class})
    @Test
    public void testIsVlan_001() throws Exception {

        final InstanceIdentifier id = Mockito.mock(InstanceIdentifier.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final Integer vlanIdValue = Integer.valueOf(1000);
        final VlanId vlanId = new VlanId(vlanIdValue);

        final List<VlanKey> vlanKeys = new ArrayList<VlanKey>() {
            {
                add(new VlanKey(vlanId));
            }
        };

        PowerMockito.mockStatic(VlanReader.class);
        PowerMockito.doReturn(vlanKeys).when(VlanReader.class, "getAllIds", cli, target, id, readContext);


        boolean result = target.isVlan(id, readContext, vlanId);


        Assert.assertThat(result, CoreMatchers.is(true));

        PowerMockito.verifyStatic();
        VlanReader.getAllIds(cli, target, id, readContext);
    }

    @PrepareOnlyThisForTest({VlanReader.class})
    @Test
    public void testIsVlan_002() throws Exception {

        final InstanceIdentifier id = Mockito.mock(InstanceIdentifier.class);
        final ReadContext readContext = Mockito.mock(ReadContext.class);

        final Integer vlanIdValue = Integer.valueOf(1000);
        final VlanId vlanId = new VlanId(vlanIdValue);

        final Integer anotherVlanIdValue = Integer.valueOf(100);
        final VlanId anotherVlanId = new VlanId(anotherVlanIdValue);

        final List<VlanKey> vlanKeys = new ArrayList<VlanKey>() {
            {
                add(new VlanKey(vlanId));
            }
        };

        PowerMockito.mockStatic(VlanReader.class);
        PowerMockito.doReturn(vlanKeys).when(VlanReader.class, "getAllIds", cli, target, id, readContext);


        boolean result = target.isVlan(id, readContext, anotherVlanId);

        Assert.assertThat(result, CoreMatchers.is(false));

        PowerMockito.verifyStatic();
        VlanReader.getAllIds(cli, target, id, readContext);
    }

    @Test
    public void testMerge_001() throws Exception {
        final VlanBuilder builder = Mockito.mock(VlanBuilder.class);
        final Config value = Mockito.mock(Config.class);

        Mockito.doReturn(builder).when(builder).setConfig(value);


        target.merge(builder, value);


        Mockito.verify(builder).setConfig(value);
    }

    @PrepareOnlyThisForTest({VlanConfigReader.class})
    @Test
    public void testParseVlanConfig_001() throws Exception {

        InputHolder4ParseVlanConfig holder = prepareVlanConfig(VLAN_CREATE_OUTPUT, 100);

        VlanConfigReader.parseVlanConfig(holder.cliOutput, holder.builder, holder.vlanId);

        Mockito.verify(holder.builder).setVlanId(holder.vlanId);
        Mockito.verify(holder.augmentConfigBuilder).setEline(Boolean.TRUE);
        Mockito.verify(holder.augmentConfigBuilder).build();
        Mockito.verify(holder.builder).addAugmentation(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1.class,
                holder.augmentConfig);
    }

    @PrepareOnlyThisForTest({VlanConfigReader.class})
    @Test
    public void testParseVlanConfig_002() throws Exception {

        InputHolder4ParseVlanConfig holder = prepareVlanConfig(VLAN_CREATE_OUTPUT, 200);

        VlanConfigReader.parseVlanConfig(holder.cliOutput, holder.builder, holder.vlanId);

        Mockito.verify(holder.builder).setVlanId(holder.vlanId);
        Mockito.verify(holder.augmentConfigBuilder, Mockito.never()).build();
        Mockito.verify(holder.augmentConfigBuilder, Mockito.never()).setEline(Mockito.anyBoolean());
        Mockito.verify(holder.builder, Mockito.never())
                .addAugmentation(
                        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1.class,
                        holder.augmentConfig);
    }

    @Test
    public void testParseVlanConfig_003() throws Exception {
        InputHolder4ParseVlanConfig holder = prepareVlanConfig(VLAN_CREATE_OUTPUT, 999);

        VlanConfigReader.parseVlanConfig(holder.cliOutput, holder.builder, holder.vlanId);

        Mockito.verify(holder.builder, Mockito.never()).setVlanId(holder.vlanId);
        Mockito.verify(holder.augmentConfigBuilder, Mockito.never()).setEline(Mockito.anyBoolean());
        Mockito.verify(holder.builder, Mockito.never()).addAugmentation(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1.class,
                holder.augmentConfig);
    }

    private InputHolder4ParseVlanConfig prepareVlanConfig(String cliOutput, int vlanIdIntValue) throws Exception {
        return new InputHolder4ParseVlanConfig(cliOutput, vlanIdIntValue).initMockBehaviors();
    }

    private static final class InputHolder4ParseVlanConfig {
        final String cliOutput;
        final ConfigBuilder builder = Mockito.mock(ConfigBuilder.class);
        final Integer vlanIdValue;  //another id
        final VlanId vlanId;
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1 augmentConfig =
            Mockito.mock(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801
                .Config1.class);
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1Builder
                augmentConfigBuilder =
                    Mockito.mock(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801
                        .Config1Builder.class);

        private InputHolder4ParseVlanConfig(String cliOutput, int vlanIdIntValue) {
            this.cliOutput = cliOutput;
            this.vlanIdValue = Integer.valueOf(vlanIdIntValue);
            this.vlanId = new VlanId(vlanIdValue);
        }

        private InputHolder4ParseVlanConfig initMockBehaviors() throws Exception {

            PowerMockito.whenNew(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1Builder.class)
                .withNoArguments().thenReturn(augmentConfigBuilder);
            Mockito.doReturn(augmentConfig).when(augmentConfigBuilder).build();
            Mockito.doReturn(augmentConfigBuilder).when(augmentConfigBuilder).setEline(Mockito.anyBoolean());
            return this;
        }
    }
}
