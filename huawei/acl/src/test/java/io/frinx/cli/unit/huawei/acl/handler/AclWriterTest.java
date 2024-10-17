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

package io.frinx.cli.unit.huawei.acl.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4STANDARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.AclSetAclEntryIpv4WildcardedAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.src.dst.ipv4.address.wildcarded.SourceAddressWildcardedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.Actions;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields.top.Ipv4Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

class AclWriterTest {

    private static final String ACL_STANDARD_WRITE = """
            system-view
            acl name TEST
            rule 20 permit ip source 123.45.6.0 0.0.0.255
            return
            """;

    private static final String ACL_STANDARD_DELETE = """
            system-view
            acl name TEST
            undo rule 10
            return
            """;

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private AclEntryWriter writer;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(AclSets.class)
            .child(AclSet.class, new AclSetKey("TEST", ACLIPV4STANDARD.class));

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new AclEntryWriter(cli);
    }

    @Test
    void testStandardWrite() throws WriteFailedException {
        final AclEntry aclEntry = getStandardAclEntry(20L, "permit", "123.45.6.0", "0.0.0.255");
        writer.writeCurrentAttributes(piid, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(ACL_STANDARD_WRITE, response.getValue().getContent());
    }

    @Test
    void testStandardDelete() throws WriteFailedException {
        final AclEntry aclEntry = new AclEntryBuilder().setSequenceId(10L).build();
        writer.deleteCurrentAttributes(piid, aclEntry, context);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(ACL_STANDARD_DELETE, response.getValue().getContent());
    }

    private AclEntry getStandardAclEntry(final Long sequence,
                                         final String fwdAction,
                                         final String ip,
                                         final String mask) {
        final AclEntryBuilder aclEntryBuilder = new AclEntryBuilder();
        aclEntryBuilder.setSequenceId(sequence);
        aclEntryBuilder.setActions(parseAction(fwdAction));

        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.header.fields.rev171215.ipv4.protocol.fields
                .top.ipv4.ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                .header.fields.rev171215.ipv4.protocol.fields.top.ipv4.ConfigBuilder();
        configBuilder.addAugmentation(AclSetAclEntryIpv4WildcardedAug.class, getSourceIpv4WildcardedAug(ip, mask));

        final Ipv4Builder ipv4Builder = new Ipv4Builder();
        ipv4Builder.setConfig(configBuilder.build());
        aclEntryBuilder.setIpv4(ipv4Builder.build());

        return aclEntryBuilder.build();
    }

    private Actions parseAction(String action) {
        final ActionsBuilder actionsBuilder = new ActionsBuilder();
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions
                .ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl
                .rev170526.action.top.actions.ConfigBuilder();

        switch (action) {
            case "permit":
                configBuilder.setForwardingAction(ACCEPT.class);
                break;
            case "deny":
                configBuilder.setForwardingAction(DROP.class);
                break;
            default:
                break;
        }

        actionsBuilder.setConfig(configBuilder.build());
        return actionsBuilder.build();
    }

    private AclSetAclEntryIpv4WildcardedAug getSourceIpv4WildcardedAug(final String ip, final String mask) {
        final AclSetAclEntryIpv4WildcardedAugBuilder augBuilder = new AclSetAclEntryIpv4WildcardedAugBuilder();
        augBuilder.setSourceAddressWildcarded(new SourceAddressWildcardedBuilder()
                .setAddress(new Ipv4Address(ip))
                .setWildcardMask(new Ipv4Address(mask))
                .build());
        return augBuilder.build();
    }
}
