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

package io.frinx.cli.unit.junos.unit.acl.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.unit.acl.handler.util.AclUtil;
import io.frinx.cli.unit.utils.CliListWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclSetConfigWriter implements CliListWriter<AclSet, AclSetKey> {

    private final Cli cli;

    public AclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String ACL_IP = "set firewall family {$type} filter {$aclName}\n";
    private static final String ACL_IP_DELETE = "delete firewall family {$type} filter {$aclName}\n";

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<AclSet> id,
                                       @NotNull AclSet aclSet,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        writeOrDelete(id, aclSet, ACL_IP);
    }

    private void writeOrDelete(@NotNull InstanceIdentifier<AclSet> id, @NotNull AclSet aclSet, String command)
            throws WriteFailedException.CreateFailedException {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        blockingWriteAndRead(fT(command, "type", AclUtil.getStringType(aclSetKey.getType()),
                "aclName", aclSetKey.getName()), cli, id, aclSet);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<AclSet> id,
                                        @NotNull AclSet aclSet,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        writeOrDelete(id, aclSet, ACL_IP_DELETE);
    }
}