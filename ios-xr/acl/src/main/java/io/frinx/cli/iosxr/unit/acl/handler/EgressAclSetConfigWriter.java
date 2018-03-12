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

import static com.google.common.base.Preconditions.checkArgument;
import static io.frinx.cli.iosxr.unit.acl.handler.IngressAclSetConfigWriter.EMPTY_ACLS;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.top.Acl;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EgressAclSetConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public EgressAclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();

        // Check interface exists
        boolean ifcExists = writeContext.readAfter(IIDs.INTERFACES.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey(name)))
                .isPresent();
        checkArgument(ifcExists, "Interface: %s does not exist, cannot configure ACLs", name);

        final AclSets aclSets = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Acl.class).child(AclSets.class)).or(EMPTY_ACLS);

        // Check acl exists
        final List<AclSet> sets = aclSets.getAclSet() == null ? Collections.emptyList() : aclSets.getAclSet();
        boolean aclSetExists = sets.stream()
                .map(AclSet::getName)
                .anyMatch(it -> it.equals(config.getSetName()));
        checkArgument(aclSetExists, "Acl: %s does not exist, cannot configure ACLs", config.getSetName());

        blockingWriteAndRead(cli, instanceIdentifier, config,
                f("interface %s", name),
                f("ipv4 access-group %s egress", config.getSetName()),
                "exit");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();

        boolean ifcExists = writeContext.readAfter(IIDs.INTERFACES.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey(name)))
                .isPresent();
        if (!ifcExists) {
            // No point in removing ACL from nonexisting ifc
            return;
        }

        blockingWriteAndRead(cli, instanceIdentifier, config,
            f("interface %s", name),
            f("no ipv4 access-group %s egress", config.getSetName()),
            "exit");
    }
}
