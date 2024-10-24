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

package io.frinx.cli.unit.saos.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReader implements CliConfigListReader<AclEntry, AclEntryKey, AclEntryBuilder> {

    private final Cli cli;

    public AclEntryReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AclEntryKey> getAllIds(@NotNull InstanceIdentifier<AclEntry> instanceIdentifier,
                                       @NotNull ReadContext readContext) throws ReadFailedException {
        String aclSetName = instanceIdentifier.firstKeyOf(AclSet.class).getName();
        return getAllIds(blockingRead(AclSetReader.SHOW_COMMAND, cli, instanceIdentifier, readContext), aclSetName);
    }

    @VisibleForTesting
    static List<AclEntryKey> getAllIds(String output, String aclSetName) {
        Pattern pattern = Pattern.compile("access-list add profile " + aclSetName + " .* precedence (?<seq>\\d+).*");

        return ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            matcher -> matcher.group("seq"),
            seq -> new AclEntryKey(Long.valueOf(seq)));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<AclEntry> instanceIdentifier,
                                      @NotNull AclEntryBuilder aclEntryBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        aclEntryBuilder.setSequenceId(instanceIdentifier.firstKeyOf(AclEntry.class).getSequenceId());
    }
}