/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.saos.acl.handler;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.acl.IIDs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class IngressAclSetConfigWriterTest {

    private static final String INTERFACE_NAME = "1";
    private static final String ACL_SET_NAME = "FOO";
    private static final String ACL_SET_NAME_OTHER = "BAR";
    private static final Class<? extends ACLTYPE> ACL_TYPE = ACLIPV4.class;

    private static final AclSets ACL_SETS = new AclSetsBuilder()
            .setAclSet(Arrays.asList(
                    createAclSet(ACL_SET_NAME, ACL_TYPE),
                    createAclSet(ACL_SET_NAME_OTHER, ACL_TYPE)))
            .build();

    private static final String WRITE_INPUT = "port set port " + INTERFACE_NAME + " ingress-acl " + ACL_SET_NAME
            + "\n\n";

    private static final String DELETE_INPUT = "port unset port " + INTERFACE_NAME + " ingress-acl" + "\n\n";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private IngressAclSetConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier<Config> iid = IIDs.AC_INTERFACES
            .child(Interface.class, new InterfaceKey(new InterfaceId(INTERFACE_NAME)))
            .child(IngressAclSets.class)
            .child(IngressAclSet.class, new IngressAclSetKey(ACL_SET_NAME, ACL_TYPE))
            .child(Config.class);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new IngressAclSetConfigWriter(cli);
    }

    @Test
    public void write() throws WriteFailedException {
        final Config config = getConfig(ACL_SET_NAME, ACL_TYPE);
        presetWriteContext(createInterface(INTERFACE_NAME), createIngressAclSets(Arrays.asList(config)));
        writer.writeCurrentAttributes(iid, config, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void writeMultipleAclsForOneType() throws WriteFailedException {
        final Config config = getConfig(ACL_SET_NAME, ACL_TYPE);
        final Config configOther = getConfig(ACL_SET_NAME_OTHER, ACL_TYPE);
        presetWriteContext(createInterface(INTERFACE_NAME), createIngressAclSets(Arrays.asList(config, configOther)));
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(CoreMatchers.containsString("Could not add more than one"));
        writer.writeCurrentAttributes(iid, config, context);
    }

    @Test
    public void delete() throws WriteFailedException {
        final Config data = getConfig(ACL_SET_NAME, ACL_TYPE);
        writer.deleteCurrentAttributes(iid, data, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    private Config getConfig(final String setName, final Class<? extends ACLTYPE> aclType) {
        return new ConfigBuilder()
                .setSetName(setName)
                .setType(aclType)
                .build();
    }

    private IngressAclSets createIngressAclSets(final List<Config> configs) {
        final List<IngressAclSet> ingressAclSetList = new ArrayList<>();
        for (final Config config : configs) {
            ingressAclSetList.add(new IngressAclSetBuilder()
                    .setConfig(config)
                    .setSetName(config.getSetName())
                    .setType(config.getType())
                    .build());
        }

        return new IngressAclSetsBuilder()
                .setIngressAclSet(ingressAclSetList)
                .build();
    }

    private static AclSet createAclSet(final String aclSetName, final Class<? extends ACLTYPE> aclType) {
        return new AclSetBuilder()
                .setName(aclSetName)
                .setType(aclType)
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top
                        .acl.sets.acl.set.ConfigBuilder()
                        .setName(aclSetName)
                        .setType(aclType)
                        .build())
                .build();
    }

    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces
            .Interface createInterface(final String name) {
        return new InterfaceBuilder()
                .setName(name)
                .setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                .rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                                .setType(EthernetCsmacd.class)
                                .build())
                .build();
    }

    private void presetWriteContext(final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                            .rev161222.interfaces.top.interfaces.Interface anInterface,
                                    final IngressAclSets initIngressAclSets) {
        Mockito.when(context.readAfter(Mockito.any())).then(invocation -> {
            final Object iidParam = invocation.getArguments()[0];

            if (iidParam.getClass().equals(KeyedInstanceIdentifier.class)) {
                return Optional.of(anInterface);
            } else if (iidParam.getClass().equals(InstanceIdentifier.class)) {
                if (((InstanceIdentifier) iidParam).getTargetType().equals(IngressAclSets.class)) {
                    return Optional.of(initIngressAclSets);
                }
                return Optional.of(ACL_SETS);
            }

            return invocation.callRealMethod();
        });

        Mockito.when(context.readBefore(Mockito.any())).then(invocation -> Optional.absent());
    }

}