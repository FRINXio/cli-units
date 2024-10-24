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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.Util;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.DomainBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.DomainKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.domain.MepBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.domain.mep.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmDomainReader implements CliConfigListReader<Domain, DomainKey, DomainBuilder> {

    private static final String SH_INTERFACE_CFM = "show running-config interface %s ethernet cfm";

    private static final Pattern MEP_DOMAIN_LINE = Pattern.compile("mep domain (?<domain>\\S+) .*");
    private static final Pattern SQUASHED_CFM_DOMAIN_LINE = Pattern.compile(
            "^domain (?<domain>\\S+) service (?<service>\\S+) mep-id (?<mepid>\\d+) (\\s*cos (?<cos>\\d+))?.*");

    private Cli cli;

    public CfmDomainReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<DomainKey> getAllIds(
        @NotNull InstanceIdentifier<Domain> id,
        @NotNull ReadContext readContext) throws ReadFailedException {

        String ifcName = Util.getSubinterfaceName(id);
        String output = blockingRead(f(SH_INTERFACE_CFM, ifcName), cli, id, readContext);
        return getDomainKeys(output);
    }

    @Override
    public void readCurrentAttributes(
        @NotNull InstanceIdentifier<Domain> id,
        @NotNull DomainBuilder builder,
        @NotNull ReadContext readContext) throws ReadFailedException {

        String ifcName = Util.getSubinterfaceName(id);
        String domain = id.firstKeyOf(Domain.class).getDomainName();
        builder.setDomainName(domain);
        builder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619
            .ethernet.cfm._interface.cfm.domains.domain.ConfigBuilder()
            .setDomainName(domain)
            .build());

        String output = blockingRead(f(SH_INTERFACE_CFM, ifcName), cli, id, readContext);
        parseDomainLine(output, domain, builder);
    }

    private static List<DomainKey> getDomainKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            MEP_DOMAIN_LINE::matcher,
            m -> m.group("domain"),
            DomainKey::new);
    }

    private static void parseDomainLine(String output, String domainName, DomainBuilder builder) {
        final String squashedOutput = output.replaceAll("[\\n\\r]", " ");
        final String outputDividedByMepDomains = squashedOutput.replace("mep ", "\n");
        ParsingUtils.parseField(outputDividedByMepDomains, 0,
            SQUASHED_CFM_DOMAIN_LINE::matcher,
            m -> {
                // there is usually only one domain under interface, so this is not expense filtering
                final String domain = m.group("domain");
                if (domainName.equals(domain)) {
                    return m;
                } else {
                    return null;
                }
            },
            m -> {
                final ConfigBuilder configBuilder = new ConfigBuilder()
                        .setMaName(m.group("service"))
                        .setMepId(Integer.valueOf(m.group("mepid")));

                final String cosField = m.group("cos");
                if (cosField != null) {
                    configBuilder.setCos(Short.valueOf(m.group("cos")));
                }
                builder.setMep(new MepBuilder()
                        .setConfig(configBuilder.build())
                        .build());
            });
    }
}