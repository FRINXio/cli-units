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

package io.frinx.cli.unit.iosxr.unit.acl.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.ModificationCache;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.unit.acl.handler.util.AclUtil;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class EgressAclSetConfigReaderTest {

    private Cli cliMock = Mockito.mock(Cli.class);
    private ReadContext context = Mockito.mock(ReadContext.class);

    @BeforeEach
    void setUp() throws Exception {
        Mockito.when(cliMock.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(TestData.READ_OUTPUT));

        final ModificationCache modificationCacheMock = Mockito.mock(ModificationCache.class);
        Mockito.when(context.getModificationCache())
                .then(invocation -> modificationCacheMock);
        Mockito.when(context.getModificationCache().containsKey(Mockito.any()))
                .then(invocation -> true);
    }

    @Test
    void parseTest() {
        final String setName = "IPV4_ACL_EXAMPLE2";
        final String type = "ipv4";
        String readOutput = "Tue Apr  3 01:10:12.042 UTC\n"
                + "interface Bundle-Ether666\n"
                + " "
                + type
                + " access-group "
                + setName
                + " egress\n"
                + "!";

        final ConfigBuilder builder = new ConfigBuilder();
        EgressAclSetReader.parseAcl(readOutput, builder, setName);

        assertEquals(builder.getSetName(), setName);
        assertEquals(builder.getType(), AclUtil.getType(type));
    }


    @Test
    void readAclConfig_LAGInterface() throws ReadFailedException {
        Mockito.when(context.read(Mockito.any()))
                .then(invocation -> Optional.of(TestData.INTERFACE_CORRECT_TYPE));

        final ConfigBuilder aclSetBuilder = new ConfigBuilder();
        EgressAclSetConfigReader reader = new EgressAclSetConfigReader(cliMock);
        reader.readCurrentAttributes(TestData.ACL_CONFIG_IID, aclSetBuilder, context);

        assertEquals(TestData.ACL_SET_NAME, aclSetBuilder.getSetName());
        assertEquals(TestData.ACL_TYPE, aclSetBuilder.getType());
    }

    private static final class TestData {

        private static final String INTERFACE_NAME = "GigabitEthernet0/0/0/0";
        private static final String ACL_SET_NAME = "test_acl_group";
        private static final String ACL_SET_NAME_OTHER = "bubu_group";
        private static final Class<? extends ACLTYPE> ACL_TYPE = ACLIPV6.class;
        private static final String READ_OUTPUT = String.format("""
                        interface GigabitEthernet0/0/0/0
                         ipv6 access-group %s egress
                         ipv6 access-group %s egress
                        !""",
                ACL_SET_NAME, ACL_SET_NAME_OTHER);

        static final InstanceIdentifier<Config> ACL_CONFIG_IID = IIDs.AC_INTERFACES
                .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_NAME)))
                .child(EgressAclSets.class)
                .child(EgressAclSet.class, new EgressAclSetKey(ACL_SET_NAME, ACL_TYPE))
                .child(Config.class);

        static final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
                .interfaces.Interface
                INTERFACE_CORRECT_TYPE =
                new InterfaceBuilder()
                        .setName(INTERFACE_NAME)
                        .setConfig(
                                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                        .rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                                        .setType(Ieee8023adLag.class)
                                        .build())
                        .build();
        static final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
                .interfaces.Interface
                INTERFACE_WRONG_TYPE =
                new InterfaceBuilder()
                        .setName(INTERFACE_NAME)
                        .setConfig(
                                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                        .rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                                        .setType(L2vlan.class)
                                        .build())
                        .build();
    }
}
