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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.unit.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReader implements CliConfigListReader<AclEntry, AclEntryKey, AclEntryBuilder> {

    private static final String SH_FIREWALL = "show configuration firewall family %s filter %s | display set";
    private static final Pattern TERM_PATTERN = Pattern.compile(".*term (?<termName>\\S+).*");

    private static ImmutableMap<Long, String> sequenceTermMap;
    private final Cli cli;

    public AclEntryReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<AclEntryKey> getAllIds(@Nonnull final InstanceIdentifier<AclEntry> instanceIdentifier,
                                              @Nonnull final ReadContext readContext) throws ReadFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        String command = f(SH_FIREWALL, AclUtil.getStringType(aclSetKey.getType()), aclSetKey.getName());
        return parseAclEntryKey(blockingRead(command, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<AclEntryKey> parseAclEntryKey(String output) {
        Set<String> termNames = new LinkedHashSet<>();
        ParsingUtils.parseFields(output, 0, TERM_PATTERN::matcher, m -> m.group("termName"), termNames::add);

        long sequenceId = 0;
        List<AclEntryKey> result = new ArrayList<>();
        ImmutableMap.Builder<Long, String> builder = new ImmutableMap.Builder<>();
        for (String termName : termNames) {
            result.add(new AclEntryKey(++sequenceId));
            builder.put(sequenceId, termName);
        }
        sequenceTermMap = builder.build();
        return result;
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<AclEntry> instanceIdentifier,
                                             @Nonnull final AclEntryBuilder aclEntryBuilder,
                                             @Nonnull final ReadContext readContext)
            throws ReadFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        String command = f(SH_FIREWALL, AclUtil.getStringType(aclSetKey.getType()), aclSetKey.getName());
        String output = blockingRead(command, cli, instanceIdentifier, readContext);

        parseACL(instanceIdentifier, aclEntryBuilder, output);
    }

    private static void parseACL(final @Nonnull InstanceIdentifier<AclEntry> iid,
                                final @Nonnull AclEntryBuilder aclEntryBuilder,
                                final String output) {

        AclSetKey aclSetKey = iid.firstKeyOf(AclSet.class);
        AclEntryKey entryKey = iid.firstKeyOf(AclEntry.class);
        List<String> maybeLine = AclEntryLineParser
                .findLinesWithTermName(sequenceTermMap.get(entryKey.getSequenceId()), output);

        AclEntryLineParser.parseLines(aclEntryBuilder, maybeLine, aclSetKey.getType(), entryKey);
    }
}
