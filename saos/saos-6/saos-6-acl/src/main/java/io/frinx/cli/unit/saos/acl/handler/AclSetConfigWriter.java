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
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.acl.Util;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Saos6AclSetAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclSetConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
            "access-list create acl-profile {$data.name} default-filter-action {$action}\n"
            + "{% if ($enabled == FALSE) %}access-list disable profile {$data.name}\n{% endif %}"
            + "configuration save";

    private static final String UPDATE_TEMPLATE =
            "{% if ($enabled == TRUE) %}access-list enable profile {$data.name}\n"
            + "{% elseIf ($enabled == FALSE) %}access-list disable profile {$data.name}\n"
            + "{% endif %}"
            + "configuration save";

    private static final String DELETE_TEMPLATE =
            "access-list delete profile {$data.name}\n"
            + "configuration save";

    private final Cli cli;

    public AclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(writeTemplate(config), cli, instanceIdentifier, config);
    }

    @VisibleForTesting
    String writeTemplate(Config config) {
        Preconditions.checkArgument(config.getType().equals(ACLTYPE.class),
                "The type must be " + ACLTYPE.class);

        Saos6AclSetAug aug = config.getAugmentation(Saos6AclSetAug.class);

        if (aug.isEnabled() != null) {
            Preconditions.checkArgument(!aug.isEnabled(),
                    "\"frinx-acl-extension:enabled\": true. It is not necessary, because \"true\" is default value.",
                    aug.isEnabled());
        }

        return fT(WRITE_TEMPLATE, "data", config,
                "action", Util.getFwdActionString(aug.getDefaultFwdAction()),
                "enabled", aug.isEnabled() == null ? null : "FALSE");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(updateTemplate(dataBefore, dataAfter), cli, id, dataAfter);
    }

    @VisibleForTesting
    String updateTemplate(Config before, Config after) {
        Preconditions.checkArgument(after.getType().equals(ACLTYPE.class),
                "Is not possible to change the type");

        Saos6AclSetAug augBefore = before.getAugmentation(Saos6AclSetAug.class);
        Saos6AclSetAug augAfter = after.getAugmentation(Saos6AclSetAug.class);

        Preconditions.checkArgument(augBefore.getDefaultFwdAction().equals(augAfter.getDefaultFwdAction()),
                "Is not possible to change the default forwarding action");

        return fT(UPDATE_TEMPLATE, "data", after, "before", before,
                "enabled", setEnabled(augBefore.isEnabled(), augAfter.isEnabled()));
    }

    private String setEnabled(Boolean before, Boolean after) {
        if (!Objects.equals(before, after)) {
            if (before == null || before) {
                if (after != null) {
                    Preconditions.checkArgument(!after, "\"frinx-acl-extension:enabled\": true. "
                            + "It is not necessary, because \"true\" is default value.", after);
                }
                return after == null ? null : "FALSE";
            } else {
                return Chunk.TRUE;
            }
        }
        return null;
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDelete(deleteTemplate(config), cli, instanceIdentifier);
    }

    @VisibleForTesting
    String deleteTemplate(Config config) {
        return fT(DELETE_TEMPLATE, "data", config);
    }
}
