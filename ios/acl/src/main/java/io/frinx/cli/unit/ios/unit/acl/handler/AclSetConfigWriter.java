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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import javax.annotation.Nonnull;
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

    private static final String ACL_IPV4 = "configure terminal\n"
            + "ip access-list extended {$aclName}\n"
            + "end\n";
    private static final String ACL_IPV6 = "configure terminal\n"
            + "ipv6 access-list {$aclName}\n"
            + "end\n";
    private static final String ACL_IPV4_DELETE = "configure terminal\n"
            + "no ip access-list extended {$aclName}\n"
            + "end\n";
    private static final String ACL_IPV6_DELETE = "configure terminal\n"
            + "no ipv6 access-list {$aclName}\n"
            + "end\n";


    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> id,
                                       @Nonnull AclSet aclSet,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        String aclName = aclSetKey.getName();
        Class<? extends ACLTYPE> type = aclSetKey.getType();
        blockingWriteAndRead(fT(type.equals(ACLIPV4.class) ? ACL_IPV4 : ACL_IPV6,
                "aclName", aclName), cli, id, aclSet);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> id,
                                        @Nonnull AclSet aclSet,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        String aclName = aclSetKey.getName();
        Class<? extends ACLTYPE> type = aclSetKey.getType();
        blockingWriteAndRead(fT(type.equals(ACLIPV4.class) ? ACL_IPV4_DELETE : ACL_IPV6_DELETE,
                    "aclName", aclName), cli, id, aclSet);
    }
}
