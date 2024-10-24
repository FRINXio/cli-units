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

package io.frinx.cli.unit.iosxr.ifc.handler.verify;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWDEFAULT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWSELFPING;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RpfCheckTop.RpfCheck;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv4.verify.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv4.verify.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv6.verify.Ipv6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ipv6.verify.Ipv6Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.verify.unicast.source.reachable.via.top.VerifyUnicastSourceReachableVia;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class RpfCheckWriterTest {

    private static final InstanceIdentifier<VerifyUnicastSourceReachableVia> RPF_IID = IIDs
            .INTERFACES.child(Interface.class, new InterfaceKey("Bundle-Ether666"))
            .augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                            .rev171024.Interface1.class)
            .child(VerifyUnicastSourceReachableVia.class);
    private static final InstanceIdentifier<Ipv4> RPF_IPV4_IID = RPF_IID.child(Ipv4.class);
    private static final InstanceIdentifier<Ipv6> RPF_IPV6_IID = RPF_IID.child(Ipv6.class);

    private Cli cli;
    private WriteContext context = Mockito.mock(WriteContext.class);
    private ArgumentCaptor<Command> responseString = ArgumentCaptor.forClass(Command.class);

    @BeforeEach
    void setUp() {
        cli = Mockito.mock(Cli.class);

        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    void prepareCmdFull_ipv4() throws WriteFailedException {
        final Ipv4 ipv4Data = new Ipv4Builder()
                .setRpfCheck(RpfCheck.ANY)
                .setAllowConfig(Arrays.asList(RPFALLOWSELFPING.class, RPFALLOWDEFAULT.class))
                .build();

        RpfCheckIpv4Writer ipv4Writer = new RpfCheckIpv4Writer(cli);

        ipv4Writer.writeCurrentAttributes(RPF_IPV4_IID, ipv4Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv4Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv4Cmd.contains("ipv4 verify"));
        assertTrue(ipv4Cmd.contains("reachable-via any"));
        assertTrue(ipv4Cmd.contains("allow-default"));
        assertTrue(ipv4Cmd.contains("allow-self-ping"));
    }

    @Test
    void prepareCmdFull_ipv6() throws WriteFailedException {
        final Ipv6 ipv6Data = new Ipv6Builder()
                .setRpfCheck(RpfCheck.ANY)
                .setAllowConfig(Arrays.asList(RPFALLOWSELFPING.class, RPFALLOWDEFAULT.class))
                .build();

        RpfCheckIpv6Writer ipv6Writer = new RpfCheckIpv6Writer(cli);

        ipv6Writer.writeCurrentAttributes(RPF_IPV6_IID, ipv6Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv6Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv6Cmd.contains("ipv6 verify"));
        assertTrue(ipv6Cmd.contains("reachable-via any"));
        assertTrue(ipv6Cmd.contains("allow-default"));
        assertTrue(ipv6Cmd.contains("allow-self-ping"));
    }

    @Test
    void prepareCmdAllowDefault_ipv4() throws WriteFailedException {
        final Ipv4 ipv4Data = new Ipv4Builder()
                .setRpfCheck(RpfCheck.ANY)
                .setAllowConfig(Arrays.asList(RPFALLOWDEFAULT.class))
                .build();

        RpfCheckIpv4Writer ipv4Writer = new RpfCheckIpv4Writer(cli);

        ipv4Writer.writeCurrentAttributes(RPF_IPV4_IID, ipv4Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv4Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv4Cmd.contains("ipv4 verify"));
        assertTrue(ipv4Cmd.contains("reachable-via any"));
        assertTrue(ipv4Cmd.contains("allow-default"));
        assertFalse(ipv4Cmd.contains("allow-self-ping"));
    }

    @Test
    void prepareCmdAllowDefault_ipv6() throws WriteFailedException {
        final Ipv6 ipv6Data = new Ipv6Builder()
                .setRpfCheck(RpfCheck.ANY)
                .setAllowConfig(Arrays.asList(RPFALLOWDEFAULT.class))
                .build();

        RpfCheckIpv6Writer ipv6Writer = new RpfCheckIpv6Writer(cli);

        ipv6Writer.writeCurrentAttributes(RPF_IPV6_IID, ipv6Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv6Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv6Cmd.contains("ipv6 verify"));
        assertTrue(ipv6Cmd.contains("reachable-via any"));
        assertTrue(ipv6Cmd.contains("allow-default"));
        assertFalse(ipv6Cmd.contains("allow-self-ping"));
    }

    @Test
    void prepareCmdAllowSelfPing_ipv4() throws WriteFailedException {
        final Ipv4 ipv4Data = new Ipv4Builder()
                .setRpfCheck(RpfCheck.ANY)
                .setAllowConfig(Arrays.asList(RPFALLOWSELFPING.class))
                .build();

        RpfCheckIpv4Writer ipv4Writer = new RpfCheckIpv4Writer(cli);

        ipv4Writer.writeCurrentAttributes(RPF_IPV4_IID, ipv4Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv4Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv4Cmd.contains("ipv4 verify"));
        assertTrue(ipv4Cmd.contains("reachable-via any"));
        assertFalse(ipv4Cmd.contains("allow-default"));
        assertTrue(ipv4Cmd.contains("allow-self-ping"));
    }

    @Test
    void prepareCmdAllowSelfPing_ipv6() throws WriteFailedException {
        final Ipv6 ipv6Data = new Ipv6Builder()
                .setRpfCheck(RpfCheck.ANY)
                .setAllowConfig(Arrays.asList(RPFALLOWSELFPING.class))
                .build();

        RpfCheckIpv6Writer ipv6Writer = new RpfCheckIpv6Writer(cli);

        ipv6Writer.writeCurrentAttributes(RPF_IPV6_IID, ipv6Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv6Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv6Cmd.contains("ipv6 verify"));
        assertTrue(ipv6Cmd.contains("reachable-via any"));
        assertFalse(ipv6Cmd.contains("allow-default"));
        assertTrue(ipv6Cmd.contains("allow-self-ping"));
    }

    @Test
    void prepareCmdEmptyAllow_ipv4() throws WriteFailedException {
        final Ipv4 ipv4Data = new Ipv4Builder()
                .setRpfCheck(RpfCheck.ANY)
                .build();

        RpfCheckIpv4Writer ipv4Writer = new RpfCheckIpv4Writer(cli);

        ipv4Writer.writeCurrentAttributes(RPF_IPV4_IID, ipv4Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv4Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv4Cmd.contains("ipv4 verify"));
        assertTrue(ipv4Cmd.contains("reachable-via any"));
        assertFalse(ipv4Cmd.contains("allow-default"));
        assertFalse(ipv4Cmd.contains("allow-self-ping"));
    }

    @Test
    void prepareCmdEmptyAllow_ipv6() throws WriteFailedException {
        final Ipv6 ipv6Data = new Ipv6Builder()
                .setRpfCheck(RpfCheck.ANY)
                .build();

        RpfCheckIpv6Writer ipv6Writer = new RpfCheckIpv6Writer(cli);

        ipv6Writer.writeCurrentAttributes(RPF_IPV6_IID, ipv6Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv6Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv6Cmd.contains("ipv6 verify"));
        assertTrue(ipv6Cmd.contains("reachable-via any"));
        assertFalse(ipv6Cmd.contains("allow-default"));
        assertFalse(ipv6Cmd.contains("allow-self-ping"));
    }

    @Test
    void prepareCmdRx_ipv4() throws WriteFailedException {
        final Ipv4 ipv4Data = new Ipv4Builder()
                .setRpfCheck(RpfCheck.RX)
                .build();

        RpfCheckIpv4Writer ipv4Writer = new RpfCheckIpv4Writer(cli);

        ipv4Writer.writeCurrentAttributes(RPF_IPV4_IID, ipv4Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv4Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv4Cmd.contains("ipv4 verify"));
        assertTrue(ipv4Cmd.contains("reachable-via rx"));
    }

    @Test
    void prepareCmdRx_ipv6() throws WriteFailedException {
        final Ipv6 ipv6Data = new Ipv6Builder()
                .setRpfCheck(RpfCheck.RX)
                .build();

        RpfCheckIpv6Writer ipv6Writer = new RpfCheckIpv6Writer(cli);

        ipv6Writer.writeCurrentAttributes(RPF_IPV6_IID, ipv6Data, context);
        Mockito.verify(cli, Mockito.times(1))
                .executeAndRead(responseString.capture());
        final String ipv6Cmd = responseString.getValue()
                .getContent();
        assertTrue(ipv6Cmd.contains("ipv6 verify"));
        assertTrue(ipv6Cmd.contains("reachable-via rx"));
    }
}
