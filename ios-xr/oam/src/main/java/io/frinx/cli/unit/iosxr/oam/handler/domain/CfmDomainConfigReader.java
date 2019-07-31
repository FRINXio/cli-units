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

package io.frinx.cli.unit.iosxr.oam.handler.domain;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.types.rev190619.DomainLevel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmDomainConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final String SH_CFM_DOMAIN = "show running-config ethernet cfm | include ^ domain %s ";
    private static final Pattern CFM_DOMAIN_LINE = Pattern.compile("domain (?<domain>\\S+) level (?<level>\\d)");
    private Cli cli;

    public CfmDomainConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String domain = id.firstKeyOf(Domain.class).getDomainName();
        builder.setDomainName(domain);

        String output = blockingRead(f(SH_CFM_DOMAIN, domain), cli, id, readContext);
        ParsingUtils.parseFields(output, 0,
            CFM_DOMAIN_LINE::matcher,
            m -> m.group("level"),
            s -> builder.setLevel(new DomainLevel(Short.valueOf(s))));
    }
}
