/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.unit.acl.handler;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.top.Acl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;

public class EgressAclSetConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public EgressAclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        final Optional<AclSets> aclSets = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Acl.class).child(AclSets.class));
        if (aclSets.isPresent()) {
            final List<AclSet> sets = aclSets.get().getAclSet();
            if (sets != null && sets.stream().map(AclSet::getName).anyMatch(it -> it.equals(config.getSetName()))) {
                blockingWriteAndRead(cli, instanceIdentifier, config,
                    "configure terminal",
                    f("interface %s", name),
                    f("ipv4 access-group %s egress", config.getSetName()),
                    "commit",
                    "end");
            }
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        blockingWriteAndRead(cli, instanceIdentifier, config,
            "configure terminal",
            f("interface %s", name),
            f("no ipv4 access-group %s egress", config.getSetName()),
            "commit",
            "end");
    }
}
