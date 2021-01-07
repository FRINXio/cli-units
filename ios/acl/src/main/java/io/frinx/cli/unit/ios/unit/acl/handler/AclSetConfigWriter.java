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

import com.google.common.collect.ImmutableMap;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4EXTENDED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4STANDARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclSetConfigWriter implements CliListWriter<AclSet, AclSetKey> {

    private final Cli cli;

    public AclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String ACL_IPV4_STANDARD_WRITE = "configure terminal\n"
            + "ip access-list standard {$aclName}\n"
            + "end\n";
    private static final String ACL_IPV4_EXTENDED_WRITE = "configure terminal\n"
            + "ip access-list extended {$aclName}\n"
            + "end\n";
    private static final String ACL_IPV6_WRITE = "configure terminal\n"
            + "ipv6 access-list {$aclName}\n"
            + "end\n";
    private static final Map<Class<? extends ACLTYPE>, String> WRITE_COMMANDS = ImmutableMap.of(
            ACLIPV4STANDARD.class, ACL_IPV4_STANDARD_WRITE,
            ACLIPV4EXTENDED.class, ACL_IPV4_EXTENDED_WRITE,
            ACLIPV6.class, ACL_IPV6_WRITE
    );

    private static final String ACL_IPV4_STANDARD_DELETE = "configure terminal\n"
            + "no ip access-list standard {$aclName}\n"
            + "end\n";
    private static final String ACL_IPV4_EXTENDED_DELETE = "configure terminal\n"
            + "no ip access-list extended {$aclName}\n"
            + "end\n";
    private static final String ACL_IPV6_DELETE = "configure terminal\n"
            + "no ipv6 access-list {$aclName}\n"
            + "end\n";
    private static final Map<Class<? extends ACLTYPE>, String> DELETE_COMMANDS = ImmutableMap.of(
            ACLIPV4STANDARD.class, ACL_IPV4_STANDARD_DELETE,
            ACLIPV4EXTENDED.class, ACL_IPV4_EXTENDED_DELETE,
            ACLIPV6.class, ACL_IPV6_DELETE
    );

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> id,
                                       @Nonnull AclSet aclSet,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String aclName = aclSetKey.getName();
        final Class<? extends ACLTYPE> type = aclSetKey.getType();
        blockingWriteAndRead(fT(WRITE_COMMANDS.get(type), "aclName", aclName), cli, id, aclSet);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<AclSet> id,
                                        @Nonnull AclSet aclSet,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final AclSetKey aclSetKey = id.firstKeyOf(AclSet.class);
        final String aclName = aclSetKey.getName();
        final Class<? extends ACLTYPE> type = aclSetKey.getType();
        blockingWriteAndRead(fT(DELETE_COMMANDS.get(type), "aclName", aclName), cli, id, aclSet);
    }

}
