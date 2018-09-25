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
import io.frinx.translate.unit.commons.handler.spi.TypedListReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryReader implements TypedListReader<AclEntry, AclEntryKey, AclEntryBuilder>,
        CliConfigListReader<AclEntry, AclEntryKey, AclEntryBuilder> {
    private static final String SH_ACCESS_LISTS_IPV4 = "show running-config ipv4 access-list %s";
    private static final String SH_ACCESS_LISTS_IPV6 = "show running-config ipv6 access-list %s";
    private static final Map<Class<? extends ACLTYPE>, String> TYPES_TO_COMMANDS = ImmutableMap.of(
            ACLIPV4.class, SH_ACCESS_LISTS_IPV4,
            ACLIPV6.class, SH_ACCESS_LISTS_IPV6
    );

    // find lines starting with number and not continuing with word remark
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("^\\s*(?<sequenceId>\\d+) (?!remark).*", Pattern
            .MULTILINE);

    private final Cli cli;

    public AclEntryReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean containsAllKeys(final Stream<? extends Identifier<? extends DataObject>> keys,
                                   final InstanceIdentifier<AclEntry> instanceIdentifier) {
        final Class<? extends ACLTYPE> aclType = instanceIdentifier.firstKeyOf(AclSet.class).getType();

        return aclType.equals(ACLIPV4.class) || aclType.equals(ACLIPV6.class);
    }

    @Override
    public List<AclEntryKey> getAllIdsForType(@Nonnull final InstanceIdentifier<AclEntry> instanceIdentifier,
                                              @Nonnull final ReadContext readContext) throws ReadFailedException {
        String command = getAclCommand(instanceIdentifier);
        return parseAclEntryKey(blockingRead(command, cli, instanceIdentifier, readContext));
    }

    static String getAclCommand(InstanceIdentifier<?> id) {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String typedCommand = TYPES_TO_COMMANDS.get(aclSetKey.getType());
        return String.format(typedCommand, aclSetKey.getName());
    }

    @VisibleForTesting
    static List<AclEntryKey> parseAclEntryKey(String output) {
        Matcher matcher = SEQUENCE_PATTERN.matcher(output);
        List<AclEntryKey> result = new ArrayList<>();
        while (matcher.find()) {
            long parseLong = Long.parseLong(matcher.group(1));
            result.add(new AclEntryKey(parseLong));
        }
        return result;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<AclEntry> list) {
        ((AclEntriesBuilder) builder).setAclEntry(list);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull final InstanceIdentifier<AclEntry> instanceIdentifier,
                                             @Nonnull final AclEntryBuilder aclEntryBuilder,
                                             @Nonnull final ReadContext readContext)
            throws ReadFailedException {

        String command = getAclCommand(instanceIdentifier);
        String output = blockingRead(command, cli, instanceIdentifier, readContext);

        parseACL(instanceIdentifier, aclEntryBuilder, output);
    }

    private void parseACL(final @Nonnull InstanceIdentifier<AclEntry> instanceIdentifier,
                          final @Nonnull AclEntryBuilder aclEntryBuilder,
                          final String output) {

        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);

        Optional<String> maybeLine = AclEntryLineParser.findAclEntryWithSequenceId(instanceIdentifier, output);

        maybeLine.ifPresent(s -> AclEntryLineParser.parseLine(aclEntryBuilder, s, aclSetKey.getType()));
    }
}
