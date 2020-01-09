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
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReader {

    private static final String SH_FIREWALL = "show configuration firewall family %s filter %s | display set";
    private static final String TERM_NAME_GROUP = "termName";
    private static final Pattern TERM_PATTERN = Pattern.compile(".*term (?<" + TERM_NAME_GROUP + ">(\".+\"|\\S+)) .*");
    private static ImmutableMap<Long, String> sequenceTermMap;

    private final Cli cli;
    private final CliReader<AclSet, AclSetBuilder> cliReader;

    public AclEntryReader(Cli cli, CliReader<AclSet, AclSetBuilder> cliReader) {
        this.cli = cli;
        this.cliReader = cliReader;
    }

    public List<AclEntryKey> getAllIds(@Nonnull final InstanceIdentifier<AclSet> instanceIdentifier,
                                       @Nonnull final ReadContext readContext) throws ReadFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        String command = cliReader.f(SH_FIREWALL, AclUtil.getStringType(aclSetKey.getType()), aclSetKey.getName());
        return parseAclEntryKey(cliReader.blockingRead(command, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<AclEntryKey> parseAclEntryKey(String output) {
        Set<String> termNames = new LinkedHashSet<>();
        ParsingUtils.parseFields(output, 0, TERM_PATTERN::matcher, m -> m.group(TERM_NAME_GROUP), termNames::add);

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

    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<AclSet> instanceIdentifier,
            @Nonnull final AclEntryBuilder aclEntryBuilder, @Nonnull final ReadContext readContext)
            throws ReadFailedException {
        final AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        final String command = cliReader.f(SH_FIREWALL, AclUtil.getStringType(
                aclSetKey.getType()), aclSetKey.getName());
        final String output = cliReader.blockingRead(command, cli, instanceIdentifier, readContext);

        final List<String> maybeLine = AclEntryLineParser.findLinesWithTermName(
                sequenceTermMap.get(aclEntryBuilder.getKey().getSequenceId()), output);
        AclEntryLineParser.parseLines(aclEntryBuilder, maybeLine, aclSetKey.getType(), aclEntryBuilder.getKey(),
                sequenceTermMap.get(aclEntryBuilder.getKey().getSequenceId()));
    }
}