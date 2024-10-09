/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.cubro.unit.acl.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclSetConfigWriter implements CliListWriter<AclSet, AclSetKey> {

    private final Cli cli;

    public AclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String ACL_IPV4 = """
            configure
            access-list ipv4 {$aclName}
            end
            """;

    private static final String ACL_IPV4_DELETE = """
            configure
            no access-list ip {$aclName}
            end
            """;

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<AclSet> instanceIdentifier,
                                       @NotNull AclSet aclSet,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        String aclName = aclSetKey.getName();
        Class<? extends ACLTYPE> type = aclSetKey.getType();

        Preconditions.checkArgument(type.equals(ACLIPV4.class), "Unsupported ACL type: " + type);
        blockingWriteAndRead(fT(ACL_IPV4, "aclName", aclName), cli, instanceIdentifier, aclSet);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<AclSet> instanceIdentifier,
                                        @NotNull AclSet aclSet,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        AclSetKey aclSetKey = instanceIdentifier.firstKeyOf(AclSet.class);
        String aclName = aclSetKey.getName();
        Class<? extends ACLTYPE> type = aclSetKey.getType();

        Preconditions.checkArgument(type.equals(ACLIPV4.class), "Unsupported ACL type: " + type);
        blockingWriteAndRead(fT(ACL_IPV4_DELETE, "aclName", aclName), cli, instanceIdentifier, aclSet);
    }
}