/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.acl.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.VrpAclSetAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class AclConfigWriter implements CliListWriter<AclSet, AclSetKey> {

    private final Cli cli;

    public AclConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String ACL_IPV4_EXTENDED_WRITE = "system-view\n"
            + "acl name {$aclName} {$type2}\n"
            + "return\n";

    private static final String ACL_IPV4_EXTENDED_DELETE = "system-view\n"
            + "undo acl name {$aclName}\n"
            + "return\n";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> id,
                                       @Nonnull AclSet aclSet,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String aclName = aclSetKey.getName();
        String accessList = aclSet.getConfig().getAugmentation(VrpAclSetAug.class).getType2();
        blockingWriteAndRead(fT(ACL_IPV4_EXTENDED_WRITE, "aclName", aclName,
                                                            "type2", accessList), cli, id, aclSet);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> id,
                                        @Nonnull AclSet aclSet,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String aclName = aclSetKey.getName();
        blockingWriteAndRead(fT(ACL_IPV4_EXTENDED_DELETE, "aclName", aclName), cli, id, aclSet);
    }

}
