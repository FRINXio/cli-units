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
import io.frinx.cli.unit.cubro.unit.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReader {

    private static final String SH_CONFIGURATION = "show running-config";
    private static final Pattern IP_SEQUENCE_PATTERN = Pattern.compile("^\\s*(?<sequenceId>\\d+).*");
    private static final Pattern ACCESS_LIST_PATTERN = Pattern.compile("(?=(access-list))");

    private final Cli cli;
    private final AclSetReader aclSetReader;

    AclEntryReader(final Cli cli, final AclSetReader aclSetReader) {
        this.cli = cli;
        this.aclSetReader = aclSetReader;
    }

    List<AclEntryKey> getAllIds(@Nonnull final InstanceIdentifier<AclSet> instanceIdentifier,
                                       @Nonnull final ReadContext readContext) throws ReadFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        return parseAclEntryKey(aclSetReader.blockingRead(SH_CONFIGURATION, cli, instanceIdentifier, readContext),
                aclSetKey);
    }

    @VisibleForTesting
    public static List<AclEntryKey> parseAclEntryKey(String output, AclSetKey aclSetKey) {
        String accessListLine = String.format("access-list %s %s",
                AclUtil.getName(aclSetKey.getType()), aclSetKey.getName());

        List<String> candidates = ACCESS_LIST_PATTERN.splitAsStream(output).collect(Collectors.toList());
        for (String candidate : candidates) {
            if (candidate.contains(accessListLine)) {
                return ParsingUtils.parseFields(candidate, 0,
                    IP_SEQUENCE_PATTERN::matcher,
                    m -> m.group("sequenceId"),
                    value -> new AclEntryKey(Long.parseLong(value)));
            }
        }

        throw new IllegalArgumentException("ACL of name " + aclSetKey.getName() + "not found");
    }

    void readCurrentAttributes(@Nonnull final InstanceIdentifier<AclSet> instanceIdentifier,
                                      @Nonnull final AclEntryBuilder aclEntryBuilder,
                                      @Nonnull final ReadContext readContext) throws ReadFailedException {
        String output = aclSetReader.blockingRead(SH_CONFIGURATION, cli, instanceIdentifier, readContext);

        parseACL(instanceIdentifier, aclEntryBuilder, output);
    }

    @VisibleForTesting
    static void parseACL(final InstanceIdentifier<AclSet> instanceIdentifier, final AclEntryBuilder aclEntryBuilder,
                         final String output) {

        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        Optional<String> maybeLine = AclEntryLineParser.findAclEntryWithSequenceId(aclEntryBuilder.getKey(), output);
        maybeLine.ifPresent(s -> AclEntryLineParser.parseLine(aclEntryBuilder, s, aclSetKey.getType()));
    }
}