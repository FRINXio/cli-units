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
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class EgressAclSetReader implements CliListReader<EgressAclSet, EgressAclSetKey, EgressAclSetBuilder> {

    private static final String SH_ACL_INTF = "show run interface %s";
    private static final Pattern ACL_LINE = Pattern.compile("ipv4 access-group (?<name>.+) egress");

    private final Cli cli;

    public EgressAclSetReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<EgressAclSetKey> getAllIds(@Nonnull InstanceIdentifier<EgressAclSet> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        InterfaceId interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        return parseAclKeys(blockingRead(String.format(SH_ACL_INTF, interfaceId.getValue()), cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    public static List<EgressAclSetKey> parseAclKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            ACL_LINE::matcher,
            matcher -> matcher.group("name"),
            v -> new EgressAclSetKey(v, ACLIPV4.class));
    }


    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<EgressAclSet> list) {
        ((EgressAclSetsBuilder) builder).setEgressAclSet(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<EgressAclSet> instanceIdentifier, @Nonnull EgressAclSetBuilder ingressAclSetBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        ingressAclSetBuilder.setSetName(instanceIdentifier.firstKeyOf(EgressAclSet.class).getSetName());
        ingressAclSetBuilder.setType(ACLIPV4.class);
    }
}
