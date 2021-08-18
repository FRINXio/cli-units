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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReader {

    public static final String SH_ACL_NAME = "display current-configuration | section acl name %s";
    private final Cli cli;
    private final AclReader aclSetReader;

    public AclEntryReader(final Cli cli, final AclReader aclSetReader) {
        this.cli = cli;
        this.aclSetReader = aclSetReader;
    }

    public List<AclEntryKey> getAllIds(@Nonnull final InstanceIdentifier<AclSet> instanceIdentifier,
                                       @Nonnull final ReadContext readContext) throws ReadFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        String command = String.format(SH_ACL_NAME, aclSetKey.getName());
        return parseAclEntryKey(aclSetReader.blockingRead(command, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<AclEntryKey> parseAclEntryKey(String output) {
        final Pattern regex = Pattern.compile("rule (?<id>\\S+) (permit|deny) .*");
        Matcher matcher = (regex).matcher(output);
        List<AclEntryKey> result = new ArrayList<>();
        while (matcher.find()) {
            long parseLong = Long.parseLong(matcher.group(1));
            result.add(new AclEntryKey(parseLong));
        }
        return result;
    }

    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<AclSet> instanceIdentifier,
                                      @Nonnull final AclEntryBuilder aclEntryBuilder,
                                      @Nonnull final ReadContext readContext) throws ReadFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        String command = String.format(SH_ACL_NAME, aclSetKey.getName());
        String output = aclSetReader.blockingRead(command, cli, instanceIdentifier, readContext);

        parseACL(instanceIdentifier, aclEntryBuilder, output);
    }

    @VisibleForTesting
    static void parseACL(final InstanceIdentifier<AclSet> instanceIdentifier, final AclEntryBuilder aclEntryBuilder,
                         final String output) {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        Optional<String> maybeLine = AclEntryLineParser.findAclEntryWithSequenceId(aclEntryBuilder.getKey(),
                output, aclSetKey.getType());
        String accessListType = AclEntryLineParser.findAccessListType(aclSetKey.getName(), output);
        maybeLine.ifPresent(s -> AclEntryLineParser.parseLine(aclEntryBuilder, s, aclSetKey.getType(), accessListType));
    }
}
