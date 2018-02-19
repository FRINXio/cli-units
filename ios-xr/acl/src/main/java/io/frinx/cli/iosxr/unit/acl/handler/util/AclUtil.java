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

package io.frinx.cli.iosxr.unit.acl.handler.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.AclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AclUtil {

    public static Class<? extends ACLTYPE> getType(final String type) {
        if ("ipv4".equals(type)) {
            return ACLIPV4.class;
        } else if ("ipv6".equals(type)) {
            return ACLIPV6.class;
        }

        throw new TypeNotPresentException(type, null);
    }

    public static String getStringType(final Class<? extends ACLTYPE> type) {
        if (ACLIPV4.class.equals(type)) {
            return "ipv4";
        } else if (ACLIPV6.class.equals(type)) {
            return "ipv6";
        }

        throw new IllegalArgumentException(
            String.format("accepts types %s, %s", ACLIPV4.class.getSimpleName(), ACLIPV6.class.getSimpleName())
        );
    }

    private static final AclSets EMPTY_ACLS = new AclSetsBuilder().setAclSet(Collections.emptyList()).build();

    public static void checkAclExists(
        final InstanceIdentifier<AclSets> aclSetIID,
        final @Nonnull Config config,
        final @Nonnull WriteContext writeContext) {

        checkAcls(aclSetIID, config, writeContext);
    }

    public static void checkAclExists(
        final InstanceIdentifier<AclSets> aclSetIID,
        final @Nonnull org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config config,
        final @Nonnull WriteContext writeContext) {

        checkAcls(aclSetIID, config, writeContext);
    }

    private static <T extends DataObject> void checkAcls(
        final InstanceIdentifier<AclSets> aclSetIID,
        final @Nonnull T config,
        final @Nonnull WriteContext writeContext) {
        Preconditions.checkArgument(config instanceof Config ||
            config instanceof org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config);
        final String setName;
        if (config instanceof Config) {
            setName = ((Config) config).getSetName();
        } else {
            setName = ((org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config) config).getSetName();
        }

        // Check acl exists
        final AclSets aclSets = writeContext.readAfter(aclSetIID).or(EMPTY_ACLS);
        final List<AclSet> sets = aclSets.getAclSet() == null ? Collections.emptyList() : aclSets.getAclSet();
        boolean aclSetExists = sets.stream()
            .map(AclSet::getName)
            .anyMatch(it -> it.equals(setName));
        checkArgument(aclSetExists, "Acl: %s does not exist, cannot configure ACLs", setName);
    }
}
