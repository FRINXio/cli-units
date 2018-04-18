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
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.unit.acl.handler.util.NameTypeEntry;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclSetReader implements CliConfigListReader<AclSet, AclSetKey, AclSetBuilder> {

    private static final String SH_ACCESS_LISTS = "show running-config | include access-list";
    private static final Pattern ACL_LINE = Pattern.compile("(?<type>.+) access-list (?<name>.+)");

    private final Cli cli;

    public AclSetReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AclSetKey> getAllIds(@Nonnull InstanceIdentifier<AclSet> id, @Nonnull ReadContext context) throws ReadFailedException {
        return parseAccessLists(blockingRead(SH_ACCESS_LISTS, cli, id, context));
    }

    @VisibleForTesting
    public List<AclSetKey> parseAccessLists(String output) {
        return ParsingUtils.parseFields(output, 0,
            ACL_LINE::matcher,
            NameTypeEntry::fromMatcher,
            nameTypeEntry -> new AclSetKey(nameTypeEntry.getKey(), nameTypeEntry.getValue()));
    }
    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<AclSet> readData) {
        ((AclSetsBuilder) builder).setAclSet(readData);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> id, @Nonnull AclSetBuilder builder, @Nonnull ReadContext ctx) throws ReadFailedException {
        final AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String aclName = aclSetKey.getName();
        final Class<? extends ACLTYPE> aclType = aclSetKey.getType();

        builder.setName(aclName);
        builder.setType(aclType);
    }
}

