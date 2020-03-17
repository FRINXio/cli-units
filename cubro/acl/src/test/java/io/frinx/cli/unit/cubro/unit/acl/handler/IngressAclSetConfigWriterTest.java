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

package io.frinx.cli.unit.cubro.unit.acl.handler;

import com.google.common.base.Optional;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class IngressAclSetConfigWriterTest {

    private Cli cliMock = Mockito.mock(Cli.class);
    private WriteContext context = Mockito.mock(WriteContext.class);
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        Mockito.when(cliMock.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    public void writeAclSet_multipleForOneType() throws WriteFailedException {
        presetWriteContext(context, TestData.INTERFACE_CORRECT_TYPE,
                TestData.INGRESS_ACL_SETS_MULTIPLE_FOR_TYPE);

        final IngressAclSetConfigWriter writer = new IngressAclSetConfigWriter(cliMock);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(
                CoreMatchers.containsString("Could not add more than one")
        );

        writer.writeCurrentAttributes(
                TestData.ACL_CONFIG_IID, TestData.ACL_CONFIG, context);
    }

    private static AclSet createAclSet(final String aclSetName, final Class<? extends ACLTYPE> aclType) {
        return new AclSetBuilder()
                .setName(aclSetName)
                .setType(aclType)
                .setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top
                                .acl.sets.acl.set.ConfigBuilder()
                                .setName(aclSetName)
                                .setType(aclType)
                                .build())
                .build();
    }

    static void presetWriteContext(
            final WriteContext context,
            final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
                    .interfaces.Interface anInterface,
            final IngressAclSets initIngressAclSets) {
        Mockito.when(context.readAfter(Mockito.any()))
                .then(invocation -> {
                    final Object iidParam = invocation.getArguments()[0];
                    if (iidParam.getClass().equals(KeyedInstanceIdentifier.class)) {
                        return Optional.of(anInterface);
                    } else if (iidParam.getClass().equals(InstanceIdentifier.class)) {
                        if (((InstanceIdentifier) iidParam).getTargetType().equals(IngressAclSets.class)) {
                            return Optional.of(initIngressAclSets);
                        }
                        return Optional.of(TestData.ACL_SETS);
                    }

                    return invocation.callRealMethod();
                });
        Mockito.when(context.readBefore(Mockito.any()))
                .then(invocation -> Optional.absent());

    }

    private static class TestData {

        private static final String INTERFACE_NAME = "1";
        private static final String ACL_SET_NAME = "acl2";
        private static final String ACL_SET_NAME_OTHER = "acl3";
        private static final Class<? extends ACLTYPE> ACL_TYPE = ACLIPV4.class;
        static final AclSets ACL_SETS = new AclSetsBuilder()
                .setAclSet(Arrays.asList(
                        createAclSet(ACL_SET_NAME, ACL_TYPE),
                        createAclSet(ACL_SET_NAME_OTHER, ACL_TYPE)
                ))
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

        static final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
                .interfaces.Interface
                INTERFACE_CORRECT_TYPE =
                new InterfaceBuilder()
                        .setName(INTERFACE_NAME)
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
    }
}