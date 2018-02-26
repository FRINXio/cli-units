/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.iosxr.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.AclEntriesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class AclEntryReader implements CliConfigListReader<AclEntry, AclEntryKey, AclEntryBuilder> {
    private static final String SH_ACCESS_LISTS_PREFIX = "do show running-config ipv";
    private static final String SH_ACCESS_LISTS_SUFFIX = " access-list ";
    private static final String SH_ACCESS_LISTS_IPV4 = SH_ACCESS_LISTS_PREFIX + "4" + SH_ACCESS_LISTS_SUFFIX;
    private static final String SH_ACCESS_LISTS_IPV6 = SH_ACCESS_LISTS_PREFIX + "6" + SH_ACCESS_LISTS_SUFFIX;
    private static final Map<Class<? extends ACLTYPE>, String> TYPES_TO_COMMANDS = ImmutableMap.of(
            ACLIPV4.class, SH_ACCESS_LISTS_IPV4,
            ACLIPV6.class, SH_ACCESS_LISTS_IPV6
    );

    // find lines starting with number and not continuing with word remark
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("^\\s*(?<sequenceId>\\d+) (?!remark).*", Pattern.MULTILINE);

    private final Cli cli;

    public AclEntryReader(Cli cli) {
        this.cli = cli;
    }

    static String getAclCommand(InstanceIdentifier<?> id) {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        return TYPES_TO_COMMANDS.get(aclSetKey.getType()) + aclSetKey.getName();
    }

    @Nonnull
    @Override
    public List<AclEntryKey> getAllIds(@Nonnull InstanceIdentifier<AclEntry> id, @Nonnull ReadContext context) throws ReadFailedException {
        String command = getAclCommand(id);
        return parseAclEntryKey(blockingRead(command, cli, id, context));
    }

    @VisibleForTesting
    static List<AclEntryKey> parseAclEntryKey(String output) {
        Matcher matcher = SEQUENCE_PATTERN.matcher(output);
        List<AclEntryKey> result = new ArrayList<>();
        while (matcher.find()) {
            long l = Long.parseLong(matcher.group(1));
            result.add(new AclEntryKey(l));
        }
        return result;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<AclEntry> list) {
        ((AclEntriesBuilder) builder).setAclEntry(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> instanceIdentifier,
                                      @Nonnull AclEntryBuilder aclEntryBuilder, @Nonnull ReadContext readContext) {
        KeyedInstanceIdentifier kii = (KeyedInstanceIdentifier) instanceIdentifier;
        AclEntryKey aclEntryKey = (AclEntryKey) kii.getKey();
        aclEntryBuilder.setSequenceId(aclEntryKey.getSequenceId());
    }
}
