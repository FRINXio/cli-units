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

package io.frinx.cli.unit.junos.unit.acl.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.unit.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.AclEntries;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntriesWriter implements CliWriter<AclEntries> {

    private final Cli cli;

    private static final String COMMAND = "insert firewall family %s filter %s term %s before term %s";
    private final AclEntryWriter entryWriter;

    public AclEntriesWriter(Cli cli) {
        this.cli = cli;
        this.entryWriter = new AclEntryWriter(cli);
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<AclEntries> instanceIdentifier,
                                       @Nonnull AclEntries aclEntries,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkNotNull(aclEntries.getAclEntry(), "Empty access list entries");

        Map<String, Set<Long>> aclKeysAfter = new HashMap<>();
        for (AclEntry aclEntry : aclEntries.getAclEntry()) {
            Set<Long> sequences = aclKeysAfter.computeIfAbsent(AclEntryWriter
                    .getTermName(aclEntry), k -> new HashSet<>());
            sequences.add(aclEntry.getSequenceId());
        }
        String wrongAcl = aclKeysAfter.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getKey() + " -> ("
                        + e.getValue().stream().map(Object::toString).collect(Collectors.joining(",")) + ")")
                .collect(Collectors.joining(", "));
        if (wrongAcl.length() > 0) {
            throw new IllegalStateException(f("Cannot set access list with the same term name"
                    + " with different sequence IDs: %s", wrongAcl));
        }

        for (AclEntry aclEntry : aclEntries.getAclEntry()) {
            InstanceIdentifier<AclEntry> iid = instanceIdentifier.builder()
                    .child(AclEntry.class, aclEntry.getKey()).build();
            entryWriter.writeCurrentAttributes(iid, aclEntry, writeContext);
        }

        if (aclEntries.getAclEntry().size() > 1) {
            AclSetKey key = instanceIdentifier.firstKeyOf(AclSet.class);
            List<AclEntry> aclEntryList = aclEntries.getAclEntry();
            aclEntryList.sort(Comparator.comparingLong(AclEntry::getSequenceId));
            for (int i = 0; i < aclEntryList.size() - 1; i++) {
                AclEntry aclEntry = aclEntryList.get(i);
                AclEntry aclEntryNext = aclEntryList.get(i + 1);
                String termName = AclEntryWriter.getTermName(aclEntry);
                String termNameNext = AclEntryWriter.getTermName(aclEntryNext);
                String order = f(COMMAND, AclUtil.getStringType(key.getType()), key.getName(), termName, termNameNext);
                blockingWriteAndRead(order, cli, instanceIdentifier, aclEntries);
            }
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<AclEntries> instanceIdentifier,
                                        @Nonnull AclEntries aclEntries,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkNotNull(aclEntries.getAclEntry(), "Empty access list entries");

        for (AclEntry aclEntry : aclEntries.getAclEntry()) {
            InstanceIdentifier<AclEntry> iid = instanceIdentifier.builder()
                    .child(AclEntry.class, aclEntry.getKey()).build();
            entryWriter.deleteCurrentAttributes(iid, aclEntry, writeContext);
        }
    }
}
