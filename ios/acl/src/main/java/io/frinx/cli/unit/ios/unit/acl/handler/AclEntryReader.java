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

package io.frinx.cli.unit.ios.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.unit.acl.handler.util.AclUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReader {
    private static final String SH_ACCESS_LISTS_IPV4 = "show ip access-lists %s";
    private static final String SH_ACCESS_LISTS_IPV6 = "show ipv6 access-list %s";

    // find lines starting with number and not continuing with word remark
    private static final Pattern IP_SEQUENCE_PATTERN = Pattern.compile("^\\s*(?<sequenceId>\\d+) (?!remark).*", Pattern
            .MULTILINE);

    private static final Pattern IPV6_SEQUENCE_PATTERN = Pattern.compile("^.*sequence (?<sequenceId>\\d+)$", Pattern
            .MULTILINE);

    private final Cli cli;
    private final AclSetReader aclSetReader;

    public AclEntryReader(final Cli cli, final AclSetReader aclSetReader) {
        this.cli = cli;
        this.aclSetReader = aclSetReader;
    }

    public List<AclEntryKey> getAllIds(@Nonnull final InstanceIdentifier<AclSet> instanceIdentifier,
                                       @Nonnull final ReadContext readContext) throws ReadFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        String command = getAclCommand(instanceIdentifier);
        return parseAclEntryKey(aclSetReader.blockingRead(command, cli, instanceIdentifier, readContext),
                aclSetKey.getType());
    }

    static String getAclCommand(InstanceIdentifier<?> id) {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String command = AclUtil.isIpv4Acl(aclSetKey.getType()) ? SH_ACCESS_LISTS_IPV4 : SH_ACCESS_LISTS_IPV6;
        return String.format(command, aclSetKey.getName());
    }

    @VisibleForTesting
    static List<AclEntryKey> parseAclEntryKey(String output, Class<? extends ACLTYPE> type) {
        Matcher matcher = (AclUtil.isIpv4Acl(type) ? IP_SEQUENCE_PATTERN : IPV6_SEQUENCE_PATTERN).matcher(output);
        List<AclEntryKey> result = new ArrayList<>();
        while (matcher.find()) {
            long parseLong = Long.parseLong(matcher.group(1));
            result.add(new AclEntryKey(parseLong));
        }
        return result;
    }

    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<AclSet> instanceIdentifier,
            @Nonnull final AclEntryBuilder aclEntryBuilder, @Nonnull final ReadContext readContext)
            throws ReadFailedException {

        String command = getAclCommand(instanceIdentifier);
        String output = aclSetReader.blockingRead(command, cli, instanceIdentifier, readContext);

        parseACL(instanceIdentifier, aclEntryBuilder, output);
    }

    @VisibleForTesting
    static void parseACL(final InstanceIdentifier<AclSet> instanceIdentifier, final AclEntryBuilder aclEntryBuilder,
                         final String output) {

        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        Optional<String> maybeLine = AclEntryLineParser.findAclEntryWithSequenceId(aclEntryBuilder.getKey(),
                output, aclSetKey.getType());
        maybeLine.ifPresent(s -> AclEntryLineParser.parseLine(aclEntryBuilder, s, aclSetKey.getType()));
    }
}
