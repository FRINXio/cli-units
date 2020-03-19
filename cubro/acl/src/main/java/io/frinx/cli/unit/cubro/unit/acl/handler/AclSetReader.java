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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.cubro.unit.acl.handler.util.NameTypeEntry;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.AclEntries;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.AclEntriesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclSetReader implements CliConfigListReader<AclSet, AclSetKey, AclSetBuilder> {

    private static final String SH_CONFIGURATION = "show running-config";
    private static final Pattern ACL_LINE = Pattern.compile("^access-list (?<type>.+) (?<name>.+)");

    private final Cli cli;
    private final AclEntryReader aclEntryReader;

    public AclSetReader(Cli cli) {
        this.cli = cli;
        this.aclEntryReader = new AclEntryReader(cli, this);
    }

    @Nonnull
    @Override
    public List<AclSetKey> getAllIds(@Nonnull InstanceIdentifier<AclSet> instanceIdentifier,
                                     @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseAccessLists(blockingRead(SH_CONFIGURATION, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    private List<AclSetKey> parseAccessLists(String output) {
        return ParsingUtils.parseFields(output, 0,
            ACL_LINE::matcher,
            NameTypeEntry::fromMatcher,
            nameTypeEntry -> new AclSetKey(nameTypeEntry.getKey(), nameTypeEntry.getValue()));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<AclSet> readData) {
        ((AclSetsBuilder) builder).setAclSet(readData);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> instanceIdentifier,
                                      @Nonnull AclSetBuilder aclSetBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        final String aclName = aclSetKey.getName();
        final Class<? extends ACLTYPE> aclType = aclSetKey.getType();

        final AclEntries aclEntries;
        try {
            aclEntries = getAclEntries(instanceIdentifier, readContext);
        } catch (IllegalArgumentException e) {
            LOG.warn("ACL set '{}' cannot be parsed. Skipping of the whole ACL.",
                    instanceIdentifier.firstKeyOf(AclSet.class).getName(), e);
            return;
        }

        aclSetBuilder.setName(aclName)
                .setType(aclType)
                .setAclEntries(aclEntries)
                .setConfig(getAclConfig(aclSetKey));
    }

    private AclEntries getAclEntries(@Nonnull InstanceIdentifier<AclSet> id, @Nonnull ReadContext ctx)
            throws ReadFailedException {
        final List<AclEntry> allAclEntries = new ArrayList<>();
        final List<AclEntryKey> allEntryIds = aclEntryReader.getAllIds(id, ctx);
        final AclEntriesBuilder aclEntriesBuilder = new AclEntriesBuilder();
        for (final AclEntryKey aclEntryId : allEntryIds) {
            final AclEntryBuilder aclEntryBuilder = new AclEntryBuilder();
            aclEntryBuilder.setKey(aclEntryId);
            aclEntryReader.readCurrentAttributes(id, aclEntryBuilder, ctx);
            allAclEntries.add(aclEntryBuilder.build());
        }

        aclEntriesBuilder.setAclEntry(allAclEntries);
        return aclEntriesBuilder.build();
    }

    private static Config getAclConfig(final AclSetKey aclSetKey) {
        return new ConfigBuilder()
                .setName(aclSetKey.getName())
                .setType(aclSetKey.getType())
                .build();
    }
}