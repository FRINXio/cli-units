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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.fd.honeycomb.translate.ModificationCache;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWDEFAULT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWSELFPING;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RpfCheckTop.RpfCheck;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.verify.unicast.source.reachable.via.top.VerifyUnicastSourceReachableVia;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.verify.unicast.source.reachable.via.top.VerifyUnicastSourceReachableViaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class RpfCheckReaderTest {

    private static final InstanceIdentifier<VerifyUnicastSourceReachableVia> RPF_IID = IIDs
            .INTERFACES.child(Interface.class, new InterfaceKey("Bundle-Ether666"))
            .augmentation(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco
                            .rev171024.Interface1.class)
            .child(VerifyUnicastSourceReachableVia.class);

    private Cli cli;
    private RpfCheckReader reader;
    private ReadContext context = Mockito.mock(ReadContext.class);

    @BeforeEach
    void setUp() {
        cli = Mockito.mock(Cli.class);
        reader = new RpfCheckReader(cli);

        final ModificationCache modificationCacheMock = Mockito.mock(ModificationCache.class);
        Mockito.when(context.getModificationCache())
                .then(invocation -> modificationCacheMock);
    }

    @Test
    void parseBasic() throws ReadFailedException {
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture("""
                        interface Bundle-Ether666
                         ipv4 verify unicast source reachable-via any
                         ipv6 nd suppress-ra
                         ipv6 verify unicast source reachable-via rx
                         ipv6 address 2400:2000:0:201:1:6509:2001:1/126!"""
                ));

        final VerifyUnicastSourceReachableViaBuilder builder = new VerifyUnicastSourceReachableViaBuilder();
        reader.readCurrentAttributes(RPF_IID, builder, context);

        assertEquals(RpfCheck.ANY, builder.getIpv4()
                .getRpfCheck());
        assertTrue(builder.getIpv4()
                .getAllowConfig()
                .isEmpty());
        assertEquals(RpfCheck.RX, builder.getIpv6()
                .getRpfCheck());
        assertTrue(builder.getIpv6()
                .getAllowConfig()
                .isEmpty());
    }

    @Test
    void parseAllowSelfPing() throws ReadFailedException {
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture("""
                        interface Bundle-Ether666
                         ipv4 verify unicast source reachable-via any
                         ipv6 nd suppress-ra
                         ipv6 verify unicast source reachable-via any allow-self-ping
                         ipv6 address 2400:2000:0:201:1:6509:2001:1/126!"""
                ));

        final VerifyUnicastSourceReachableViaBuilder builder = new VerifyUnicastSourceReachableViaBuilder();
        reader.readCurrentAttributes(RPF_IID, builder, context);

        assertEquals(RpfCheck.ANY, builder.getIpv4()
                .getRpfCheck());
        assertTrue(builder.getIpv4()
                .getAllowConfig()
                .isEmpty());
        assertEquals(RpfCheck.ANY, builder.getIpv6()
                .getRpfCheck());
        assertEquals(1, builder.getIpv6()
                .getAllowConfig()
                .size());
        assertEquals(RPFALLOWSELFPING.class, builder.getIpv6()
                .getAllowConfig()
                .get(0));
    }

    @Test
    void parseAllowDefault() throws ReadFailedException {
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture("""
                        interface Bundle-Ether666
                         ipv4 verify unicast source reachable-via any allow-default
                         ipv6 nd suppress-ra
                         ipv6 verify unicast source reachable-via rx
                         ipv6 address 2400:2000:0:201:1:6509:2001:1/126!"""
                ));

        final VerifyUnicastSourceReachableViaBuilder builder = new VerifyUnicastSourceReachableViaBuilder();
        reader.readCurrentAttributes(RPF_IID, builder, context);

        assertEquals(RpfCheck.ANY, builder.getIpv4()
                .getRpfCheck());
        assertEquals(1, builder.getIpv4()
                .getAllowConfig()
                .size());
        assertEquals(RPFALLOWDEFAULT.class, builder.getIpv4()
                .getAllowConfig()
                .get(0));
        assertEquals(RpfCheck.RX, builder.getIpv6()
                .getRpfCheck());
        assertTrue(builder.getIpv6()
                .getAllowConfig()
                .isEmpty());
    }

    @Test
    void parseAllowConfigSingleValues() throws ReadFailedException {
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture("""
                        interface Bundle-Ether666
                         ipv4 verify unicast source reachable-via any allow-default
                         ipv6 nd suppress-ra
                         ipv6 verify unicast source reachable-via any allow-self-ping
                         ipv6 address 2400:2000:0:201:1:6509:2001:1/126!"""
                ));

        final VerifyUnicastSourceReachableViaBuilder builder = new VerifyUnicastSourceReachableViaBuilder();
        reader.readCurrentAttributes(RPF_IID, builder, context);

        assertEquals(RpfCheck.ANY, builder.getIpv4()
                .getRpfCheck());
        assertEquals(1, builder.getIpv4()
                .getAllowConfig()
                .size());
        assertEquals(RPFALLOWDEFAULT.class, builder.getIpv4()
                .getAllowConfig()
                .get(0));
        assertEquals(RpfCheck.ANY, builder.getIpv6()
                .getRpfCheck());
        assertEquals(1, builder.getIpv6()
                .getAllowConfig()
                .size());
        assertEquals(RPFALLOWSELFPING.class, builder.getIpv6()
                .getAllowConfig()
                .get(0));
    }

    @Test
    void parseAllowConfigBothValuesAndSingleValue() throws ReadFailedException {
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture("""
                        interface Bundle-Ether666
                         ipv4 verify unicast source reachable-via any allow-default allow-self-ping
                         ipv6 nd suppress-ra
                         ipv6 verify unicast source reachable-via any allow-self-ping
                         ipv6 address 2400:2000:0:201:1:6509:2001:1/126!"""
                ));

        final VerifyUnicastSourceReachableViaBuilder builder = new VerifyUnicastSourceReachableViaBuilder();
        reader.readCurrentAttributes(RPF_IID, builder, context);

        assertEquals(RpfCheck.ANY, builder.getIpv4()
                .getRpfCheck());
        assertEquals(2, builder.getIpv4()
                .getAllowConfig()
                .size());
        assertTrue(builder.getIpv4()
                .getAllowConfig()
                .contains(RPFALLOWDEFAULT.class));
        assertTrue(builder.getIpv4()
                .getAllowConfig()
                .contains(RPFALLOWSELFPING.class));
        assertEquals(RpfCheck.ANY, builder.getIpv6()
                .getRpfCheck());
        assertEquals(1, builder.getIpv6()
                .getAllowConfig()
                .size());
        assertEquals(RPFALLOWSELFPING.class, builder.getIpv6()
                .getAllowConfig()
                .get(0));
    }

    @Test
    void parseAllowConfigBothValues() throws ReadFailedException {
        Mockito.when(cli.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture("""
                        interface Bundle-Ether666
                         ipv4 verify unicast source reachable-via any allow-default allow-self-ping
                         ipv6 nd suppress-ra
                         ipv6 verify unicast source reachable-via any allow-default allow-self-ping
                         ipv6 address 2400:2000:0:201:1:6509:2001:1/126!"""
                ));

        final VerifyUnicastSourceReachableViaBuilder builder = new VerifyUnicastSourceReachableViaBuilder();
        reader.readCurrentAttributes(RPF_IID, builder, context);

        assertEquals(RpfCheck.ANY, builder.getIpv4()
                .getRpfCheck());
        assertEquals(2, builder.getIpv4()
                .getAllowConfig()
                .size());
        assertTrue(builder.getIpv4()
                .getAllowConfig()
                .contains(RPFALLOWDEFAULT.class));
        assertTrue(builder.getIpv4()
                .getAllowConfig()
                .contains(RPFALLOWSELFPING.class));
        assertEquals(RpfCheck.ANY, builder.getIpv6()
                .getRpfCheck());
        assertEquals(2, builder.getIpv6()
                .getAllowConfig()
                .size());
        assertTrue(builder.getIpv6()
                .getAllowConfig()
                .contains(RPFALLOWDEFAULT.class));
        assertTrue(builder.getIpv6()
                .getAllowConfig()
                .contains(RPFALLOWSELFPING.class));
    }
}
