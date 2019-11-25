/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.lr.handler.statics.nexthop;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.ref.InterfaceRef;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.ref.InterfaceRefBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.AfiSafiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.AfiSafiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.SetTagAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.SetTagAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.STATIC;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.TagType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopListWriterTest {
    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;
    private NextHopListWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private static final String WRITE_COMMAND = "router static\n"
                           + "address-family ipv4 unicast\n"
                           + " 118.103.0.0/24 Bundle-Ether100.100 61.125.140.86 tag 203\n"
                           + "root\n";
    private static final String DELETE_COMMAND = "router static\n"
            + "address-family ipv4 unicast\n"
            + "no 118.103.0.0/24 Bundle-Ether100.100 61.125.140.86\n"
            + "root\n";

    private static final InstanceIdentifier<NextHop> IID = IidUtils.createIid(IIDs.NE_NE_PR_PR_ST_ST_NE_NEXTHOP,
        NetworInstance.DEFAULT_NETWORK,
        new ProtocolKey(STATIC.class, "STATIC"),
        new StaticKey(new IpPrefix(new IpPrefix(new Ipv4Prefix("118.103.0.0/24")))),
        new NextHopKey("1"));

    private Static staticObj;
    private NextHop nexthop;

    private void initializeData() {
        SetTagAug setTagAug = new SetTagAugBuilder()
                .setSetTag(new TagType(203L))
                .build();
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515
            .local._static.top._static.routes._static.next.hops.next.hop.Config nexthopConfig =
            new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515
                .local._static.top._static.routes._static.next.hops.next.hop.ConfigBuilder()
                .setIndex("1")
                .setNextHop(new IpAddress(new Ipv4Address("61.125.140.86")))
                .addAugmentation(SetTagAug.class, setTagAug)
                .build();
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.ref
            ._interface.ref.Config ifcRefCfg =
            new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                .net.yang.interfaces.rev161222._interface.ref._interface.ref.ConfigBuilder()
                .setInterface("Bundle-Ether100")
                .setSubinterface("100")
                .build();
        InterfaceRef ifcRef = new InterfaceRefBuilder()
                .setConfig(ifcRefCfg)
                .build();
        nexthop = new NextHopBuilder()
            .setConfig(nexthopConfig)
            .setIndex("1")
            .setKey(new NextHopKey("1"))
            .setInterfaceRef(ifcRef)
            .build();
        AfiSafiAug afisafiAug = new AfiSafiAugBuilder()
            .setAfiSafiType(IPV4UNICAST.class)
            .build();
        Config staticCofig = new ConfigBuilder()
            .addAugmentation(AfiSafiAug.class, afisafiAug)
            .build();
        staticObj = new StaticBuilder()
            .setConfig(staticCofig)
            .build();
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        target = new NextHopListWriter(cli);
        initializeData();
    }

    @Test
    public void testWriteCurrentAttributesWResult() throws WriteFailedException {
        Mockito.doReturn(Optional.of(staticObj)).when(writeContext).readAfter(Mockito.any());
        target.writeCurrentAttributes(IID, nexthop, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_COMMAND, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributesWResult() throws WriteFailedException {
        Mockito.doReturn(Optional.of(staticObj)).when(writeContext).readBefore(Mockito.any());
        target.deleteCurrentAttributes(IID, nexthop, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_COMMAND, response.getValue().getContent());
    }
}
