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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetConfigWriterTest {

    private Cli cliMock = Mockito.mock(Cli.class);
    private WriteContext context = Mockito.mock(WriteContext.class);
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        Mockito.when(cliMock.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    public void writeAclSet_LAGInterface_goldenPath() throws WriteFailedException {
        EgressAclSetConfigWriterTest.presetWriteContext(context, TestData.INTERFACE_CORRECT_TYPE,
                TestData.EGRESS_ACL_SETS_INIT_EMPTY, TestData.INGRESS_ACL_SETS_INIT);

        final IngressAclSetConfigWriter writer = new IngressAclSetConfigWriter(cliMock);

        writer.writeCurrentAttributes(
                TestData.ACL_CONFIG_IID, TestData.ACL_CONFIG, context);

        Mockito.verify(cliMock, Mockito.times(1)).executeAndRead(Mockito.any());
    }

    @Test
    public void writeAclSet_multipleForOneType() throws WriteFailedException {
        EgressAclSetConfigWriterTest.presetWriteContext(context, TestData.INTERFACE_CORRECT_TYPE,
                TestData.EGRESS_ACL_SETS_INIT_EMPTY, TestData.INGRESS_ACL_SETS_MULTIPLE_FOR_TYPE);

        final IngressAclSetConfigWriter writer = new IngressAclSetConfigWriter(cliMock);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
                CoreMatchers.containsString("Could not add more than one")
        );

        writer.writeCurrentAttributes(
                TestData.ACL_CONFIG_IID, TestData.ACL_CONFIG, context);
    }

    private static class TestData {

        private static final String INTERFACE_NAME = "GigabitInterface 0/0/0/0";
        private static final String ACL_SET_NAME = "test_acl_group";
        private static final String ACL_SET_NAME_OTHER = "bub_group";
        private static final Class<? extends ACLTYPE> ACL_TYPE = ACLIPV6.class;

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

        static final InstanceIdentifier<Config> ACL_CONFIG_IID = IIDs.AC_INTERFACES
                .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_NAME)))
                .child(IngressAclSets.class)
                .child(IngressAclSet.class, new IngressAclSetKey(ACL_SET_NAME, ACL_TYPE))
                .child(Config.class);
        static final Config ACL_CONFIG = new ConfigBuilder()
                .setSetName(ACL_SET_NAME)
                .setType(ACL_TYPE)
                .build();

        static final IngressAclSets INGRESS_ACL_SETS_INIT = new IngressAclSetsBuilder()
                .setIngressAclSet(Arrays.asList(new IngressAclSetBuilder()
                        .setConfig(ACL_CONFIG)
                        .setSetName(ACL_CONFIG.getSetName())
                        .setType(ACL_CONFIG.getType())
                        .build()))
                .build();
        static final IngressAclSets INGRESS_ACL_SETS_MULTIPLE_FOR_TYPE = new IngressAclSetsBuilder()
                .setIngressAclSet(Arrays.asList(
                        new IngressAclSetBuilder()
                                .setConfig(ACL_CONFIG)
                                .setSetName(ACL_CONFIG.getSetName())
                                .setType(ACL_CONFIG.getType())
                                .build(),
                        new IngressAclSetBuilder()
                                .setConfig(ACL_CONFIG)
                                .setSetName(ACL_SET_NAME_OTHER)
                                .setType(ACL_CONFIG.getType())
                                .build()))
                .build();
        static final EgressAclSets EGRESS_ACL_SETS_INIT_EMPTY = new EgressAclSetsBuilder()
                .build();
    }
}
