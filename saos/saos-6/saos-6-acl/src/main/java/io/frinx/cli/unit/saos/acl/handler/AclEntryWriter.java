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

package io.frinx.cli.unit.saos.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.acl.Util;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryWriter implements CliWriter<AclEntry> {

    private static final String WRITE_TEMPLATE = "access-list add profile {$aclSetName} rule {$termName} precedence "
            + "{$data.sequence_id} filter-action {$action} any\n"
            + "configuration save";

    private static final String UPDATE_TEMPLATE = "access-list set profile {$aclSetName} rule {$termName} precedence "
            + "{$data.sequence_id} filter-action {$action}\n"
            + "configuration save";

    private static final String DELETE_TEMPLATE = "access-list remove profile {$aclSetName} rule {$termName}\n"
            + "configuration save";

    private final Cli cli;

    public AclEntryWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> instanceIdentifier,
                                       @Nonnull AclEntry aclEntry,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String aclSetName = instanceIdentifier.firstKeyOf(AclSet.class).getName();
        blockingWriteAndRead(writeTemplate(aclEntry, aclSetName), cli, instanceIdentifier, aclEntry);
    }

    @VisibleForTesting
    String writeTemplate(AclEntry aclEntry, String aclSetName) {
        return fT(WRITE_TEMPLATE, "data", aclEntry,
                "aclSetName", aclSetName,
                "termName", aclEntry.getConfig().getAugmentation(Config2.class).getTermName(),
                "action", Util.getFwdActionString(aclEntry.getActions().getConfig().getForwardingAction()));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> id,
                                        @Nonnull AclEntry dataBefore,
                                        @Nonnull AclEntry dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String aclSetName = id.firstKeyOf(AclSet.class).getName();
        blockingWriteAndRead(updateTemplate(dataBefore, dataAfter, aclSetName), cli, id, dataAfter);
    }

    @VisibleForTesting
    String updateTemplate(AclEntry before, AclEntry after, String aclSetName) {
        return fT(UPDATE_TEMPLATE, "data", after, "before", before,
                "aclSetName", aclSetName,
                "termName", after.getConfig().getAugmentation(Config2.class).getTermName(),
                "action", Util.getFwdActionString(after.getActions().getConfig().getForwardingAction()));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<AclEntry> instanceIdentifier,
                                        @Nonnull AclEntry aclEntry,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String aclSetName = instanceIdentifier.firstKeyOf(AclSet.class).getName();
        blockingDelete(deleteTemplate(aclEntry, aclSetName), cli, instanceIdentifier);
    }

    @VisibleForTesting
    String deleteTemplate(AclEntry aclEntry, String aclSetName) {
        return fT(DELETE_TEMPLATE, "data", aclEntry,
                "aclSetName", aclSetName,
                "termName", aclEntry.getConfig().getAugmentation(Config2.class).getTermName());
    }
}
