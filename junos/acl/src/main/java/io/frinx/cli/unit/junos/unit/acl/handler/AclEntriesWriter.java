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
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
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

        for (AclEntry aclEntry : aclEntries.getAclEntry()) {
            InstanceIdentifier<AclEntry> iid = instanceIdentifier.builder()
                    .child(AclEntry.class, aclEntry.getKey()).build();
            entryWriter.writeCurrentAttributes(iid, aclEntry, writeContext);
        }

        if (aclEntries.getAclEntry().size() > 1) {
            AclSetKey key = instanceIdentifier.firstKeyOf(AclSet.class);
            List<AclEntry> aclEntry1 = aclEntries.getAclEntry();
            aclEntry1.sort(Comparator.comparingLong(AclEntry::getSequenceId));
            for (int i = 0; i < aclEntry1.size() - 1; i++) {
                AclEntry aclEntry = aclEntry1.get(i);
                AclEntry aclEntryNext = aclEntry1.get(i + 1);
                String termName = aclEntry.getConfig().getAugmentation(Config2.class).getTermName();
                String termNameNext = aclEntryNext.getConfig().getAugmentation(Config2.class).getTermName();
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
