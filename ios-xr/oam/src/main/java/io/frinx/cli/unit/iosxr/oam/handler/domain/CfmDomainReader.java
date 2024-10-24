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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.DomainBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.DomainKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmDomainReader implements CliConfigListReader<Domain, DomainKey, DomainBuilder> {
    private static final String SH_CFM_DOMAIN = "show running-config ethernet cfm | include ^ domain";
    private static final Pattern CFM_DOMAIN_LINE = Pattern.compile("domain (?<domain>\\S+) .*");
    private Cli cli;

    public CfmDomainReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<DomainKey> getAllIds(
        @NotNull InstanceIdentifier<Domain> id,
        @NotNull ReadContext readContext) throws ReadFailedException {

        String output = blockingRead(SH_CFM_DOMAIN, cli, id, readContext);
        return getDomainKeys(output);
    }

    @Override
    public void readCurrentAttributes(
        @NotNull InstanceIdentifier<Domain> id,
        @NotNull DomainBuilder builder,
        @NotNull ReadContext readContext) throws ReadFailedException {

        DomainKey key = id.firstKeyOf(Domain.class);
        builder.setDomainName(key.getDomainName());
    }

    private static List<DomainKey> getDomainKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            CFM_DOMAIN_LINE::matcher,
            m -> m.group("domain"),
            s -> new DomainKey(s));
    }
}