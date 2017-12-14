/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class AclAllReader implements CliConfigListReader<AclSet, AclSetKey, AclSetBuilder> {

    private static final String SH_ACCESS_LISTS = "show run ipv4 access-list | include access-list";
    private static final Pattern ACL_LINE = Pattern.compile("ipv4 access-list (?<name>.+)");

    private final Cli cli;

    public AclAllReader(Cli cli) {
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
            matcher -> matcher.group("name"),
            v -> new AclSetKey(v, ACLIPV4.class));
    }
    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<AclSet> readData) {
        ((AclSetsBuilder) builder).setAclSet(readData);
    }

    @Nonnull
    @Override
    public AclSetBuilder getBuilder(@Nonnull InstanceIdentifier<AclSet> id) {
        return new AclSetBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> id, @Nonnull AclSetBuilder builder, @Nonnull ReadContext ctx) throws ReadFailedException {
        builder.setName(id.firstKeyOf(AclSet.class).getName());
        builder.setType(ACLIPV4.class);
    }
}
