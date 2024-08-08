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

package io.frinx.cli.unit.ios.snmp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.Community;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.CommunityBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.CommunityKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CommunityReader implements CliConfigListReader<Community, CommunityKey, CommunityBuilder> {

    public static final String SHOW_SNMP_COMMUNITIES = "show running-config | include snmp-server community";
    public static final String SHOW_SNMP_COMMUNITY = "show running-config | include snmp-server community %s";

    public static final Pattern COMMUNITY_LINE = Pattern.compile("snmp-server community (?<name>\\S+)"
            + "( view (?<view>\\S+))? (?<access>RO|RW)( (?<acl>\\S+))?");

    private final Cli cli;

    public CommunityReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<CommunityKey> getAllIds(@NotNull InstanceIdentifier<Community> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        final String output = blockingRead(SHOW_SNMP_COMMUNITIES, cli, instanceIdentifier, readContext);
        return new ArrayList<>(getCommunityKeys(output));
    }

    @VisibleForTesting
    public static List<CommunityKey> getCommunityKeys(final String output) {
        return ParsingUtils.parseFields(output, 0, COMMUNITY_LINE::matcher,
            matcher -> matcher.group("name"), CommunityKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Community> instanceIdentifier,
                                      @NotNull CommunityBuilder communityBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String viewName = instanceIdentifier.firstKeyOf(Community.class).getName();
        communityBuilder.setName(viewName);
    }
}