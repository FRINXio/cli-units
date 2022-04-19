/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.BondingGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.BondingGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.upstream.upstream.bonding.groups.BondingGroupKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableUpstreamReader implements CliConfigListReader<BondingGroup, BondingGroupKey, BondingGroupBuilder> {

    static final Pattern BONDING_GROUP_ID = Pattern.compile("cable upstream bonding-group (?<id>.+)");

    static final String SH_UPSTREAM_BONDING_GROUPS =
            InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG + " | section cable upstream bonding-group";

    private final Cli cli;

    public CableUpstreamReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<BondingGroupKey> getAllIds(@Nonnull InstanceIdentifier<BondingGroup> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (ifcName.startsWith("Cable")) {
            final String ifcOutput =
                blockingRead(f(SH_UPSTREAM_BONDING_GROUPS, ifcName), cli, instanceIdentifier, readContext);
            return parseIds(ifcOutput);
        }
        else {
            return Collections.emptyList();
        }
    }

    @VisibleForTesting
    static List<BondingGroupKey> parseIds(final String ifcOutput) {
        return ParsingUtils.parseFields(ifcOutput, 0,
            BONDING_GROUP_ID::matcher,
            matcher -> matcher.group("id"),
            BondingGroupKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<BondingGroup> instanceIdentifier,
                                      @Nonnull BondingGroupBuilder bondingGroupBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String bondingGroupId = instanceIdentifier.firstKeyOf(BondingGroup.class).getId();
        bondingGroupBuilder.setId(bondingGroupId);
        bondingGroupBuilder.setKey(new BondingGroupKey(bondingGroupId));
    }
}
