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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.unit.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.EgressAclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EgressAclSetConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public EgressAclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        final Class<? extends ACLTYPE> aclType = config.getType();
        Preconditions.checkArgument(aclType != null, "Missing acl type");

        checkEgressAclSetConfigExists(instanceIdentifier, aclType, writeContext, interfaceName);

        blockingWriteAndRead(cli, instanceIdentifier, config,
            "configure terminal\n",
            f("interface %s", interfaceName),
            f("%s %s out", AclUtil.chooseIpCommand(aclType), config.getSetName()),
            "end");
    }

    private void checkEgressAclSetConfigExists(final InstanceIdentifier<Config> instanceIdentifier,
                                               final Class<? extends ACLTYPE> aclType,
                                               final WriteContext writeContext,
                                               final String interfaceName) {
        // find multiple egress acl sets already set for type (ipv4/ipv6)
        final Optional<EgressAclSets> egressAclSetsOptional =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, EgressAclSets.class));

        final long storedAclSetsCount = Stream
                .of(egressAclSetsOptional)
                .filter(Optional::isPresent)
                .map(item -> item.get().getEgressAclSet())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(aclSet -> aclSet.getType().equals(aclType))
                .count();

        if (storedAclSetsCount > 1) {
            throw new IllegalArgumentException(f(
                    "Could not add more than one egress-acl-set config for type %s for interface %s.",
                    aclType, interfaceName));
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                        @NotNull final Config dataBefore,
                                        @NotNull final Config dataAfter,
                                        @NotNull final WriteContext writeContext) throws WriteFailedException {
        // should not happen until model changes (augment or name moved out from composite key)
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();

        boolean ifcExists = writeContext.readAfter(IIDs.INTERFACES.child(
                org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
                        .interfaces.Interface.class,
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top
                        .interfaces.InterfaceKey(
                        name)))
                .isPresent();
        if (!ifcExists) {
            // No point in removing ACL from nonexisting ifc
            return;
        }

        final Class<? extends ACLTYPE> aclType = config.getType();
        Preconditions.checkArgument(aclType != null, "Missing acl type");

        blockingWriteAndRead(cli, instanceIdentifier, config,
                "configure terminal\n",
                f("interface %s", name),
                f("no %s %s out", AclUtil.chooseIpCommand(aclType), config.getSetName()),
                "end");
    }
}