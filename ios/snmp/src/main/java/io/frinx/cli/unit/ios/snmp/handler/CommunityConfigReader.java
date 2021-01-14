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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.snmp.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.Community;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.community.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.community.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CommunityConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public CommunityConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Community.class).getName();
        final String output = blockingRead(f(CommunityReader.SHOW_SNMP_COMMUNITY, name),
                cli, instanceIdentifier, readContext);
        fillInConfig(name, output, configBuilder);
    }

    public static void fillInConfig(final String name, final String output, final ConfigBuilder configBuilder) {
        configBuilder.setName(name);
        getAccess(output).ifPresent(s -> configBuilder.setAccess(Util.getAccessType(s)));
        getView(output).ifPresent(configBuilder::setView);
        getAcl(output).ifPresent(configBuilder::setAccessList);
    }

    private static Optional<String> getView(final String line) {
        return ParsingUtils.parseField(line, 0, CommunityReader.COMMUNITY_LINE::matcher,
            m -> m.group("view"));
    }

    private static Optional<String> getAccess(final String line) {
        return ParsingUtils.parseField(line, 0, CommunityReader.COMMUNITY_LINE::matcher,
            m -> m.group("access"));
    }

    private static Optional<String> getAcl(final String line) {
        return ParsingUtils.parseField(line, 0, CommunityReader.COMMUNITY_LINE::matcher,
            m -> m.group("acl"));
    }

}