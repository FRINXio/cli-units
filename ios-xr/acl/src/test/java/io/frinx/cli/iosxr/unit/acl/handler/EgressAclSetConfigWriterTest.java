/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.unit.acl.handler;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class EgressAclSetConfigWriterTest {

    private Cli cliMock = Mockito.mock(Cli.class);
    private WriteContext context = Mockito.mock(WriteContext.class);
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    static void presetWriteContext(
        final WriteContext context,
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface anInterface) {
        Mockito.when(context.readAfter(Mockito.any()))
            .then(invocation -> {
                final Object iidParam = invocation.getArguments()[0];
                if (iidParam.getClass().equals(KeyedInstanceIdentifier.class)) {
                    return Optional.of(anInterface);
                } else if (iidParam.getClass().equals(InstanceIdentifier.class)) {
                    return Optional.of(TestData.ACL_SETS);
                }

                return invocation.callRealMethod();
            });
    }

    @Before
    public void setUp() throws Exception {
        Mockito.when(cliMock.executeAndRead(Mockito.any()))
            .then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    public void writeAclSet_LAGInterface_goldenPath() throws WriteFailedException {
        presetWriteContext(context, TestData.INTERFACE_CORRECT_TYPE);

        final EgressAclSetConfigWriter writer = new EgressAclSetConfigWriter(cliMock);

        writer.writeCurrentAttributes(TestData.ACL_CONFIG_IID, TestData.ACL_CONFIG, context);

        Mockito.verify(cliMock, Mockito.times(1)).executeAndRead(Mockito.any());
    }

    @Test
    public void writeAclSet_noLAGInterface() throws WriteFailedException {
        presetWriteContext(context, TestData.INTERFACE_WRONG_TYPE);

        final EgressAclSetConfigWriter writer = new EgressAclSetConfigWriter(cliMock);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(CoreMatchers.allOf(
            CoreMatchers.containsString("Parent interface should be"),
            CoreMatchers.containsString(EthernetCsmacd.class.getSimpleName()),
            CoreMatchers.containsString(Ieee8023adLag.class.getSimpleName())
        ));

        writer.writeCurrentAttributes(TestData.ACL_CONFIG_IID, TestData.ACL_CONFIG, context);
    }

    private static class TestData {

        private static final String INTERFACE_NAME = "GigabitInterface 0/0/0/0";
        private static final String ACL_SET_NAME = "test_acl_group";
        private static final String ACL_SET_NAME_OTHER = "bubu_group";
        private static final Class<? extends ACLTYPE> ACL_TYPE = ACLIPV6.class;
        static final AclSets ACL_SETS = new AclSetsBuilder()
            .setAclSet(Arrays.asList(
                createAclSet(ACL_SET_NAME, ACL_TYPE),
                createAclSet(ACL_SET_NAME_OTHER, ACL_TYPE)
            ))
            .build();

        static final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface
            INTERFACE_CORRECT_TYPE =
            new InterfaceBuilder()
                .setName(INTERFACE_NAME)
                .setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                        .setType(Ieee8023adLag.class)
                        .build())
                .build();
        static final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface
            INTERFACE_WRONG_TYPE =
            new InterfaceBuilder()
                .setName(INTERFACE_NAME)
                .setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                        .setType(L2vlan.class)
                        .build())
                .build();

        static final InstanceIdentifier<Config> ACL_CONFIG_IID = IIDs.AC_INTERFACES
            .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_NAME)))
            .child(EgressAclSets.class)
            .child(EgressAclSet.class, new EgressAclSetKey(ACL_SET_NAME, ACL_TYPE))
            .child(Config.class);
        static final Config ACL_CONFIG = new ConfigBuilder()
            .setSetName(ACL_SET_NAME)
            .setType(ACL_TYPE)
            .build();

        private static AclSet createAclSet(final String aclSetName,
                                           final Class<? extends ACLTYPE> aclType) {
            return new AclSetBuilder()
                .setName(aclSetName)
                .setType(aclType)
                .setConfig(
                    new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.ConfigBuilder()
                        .setName(aclSetName)
                        .setType(aclType)
                        .build())
                .build();
        }
    }
}
