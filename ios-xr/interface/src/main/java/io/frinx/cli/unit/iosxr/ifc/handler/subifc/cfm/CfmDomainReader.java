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
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.DomainBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.DomainKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.domain.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.domain.MepBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmDomainReader implements CliConfigListReader<Domain, DomainKey, DomainBuilder> {
    private static final String SH_CFM_DOMAIN_LIST =
        "show running-config interface %s ethernet cfm | include ^ {2}mep domain";
    private static final String SH_CFM_DOMAIN =
        "show running-config interface %s ethernet cfm | include ^ {2}mep domain %s";

    private static final Pattern CFM_DOMAIN_LINE =
        Pattern.compile("mep domain (?<domain>\\S+) service (?<service>\\S+) mep-id (?<mepid>\\d+).*");

    private Cli cli;

    public CfmDomainReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<DomainKey> getAllIds(
        @Nonnull InstanceIdentifier<Domain> id,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String ifcName = Util.getSubinterfaceName(id);
        String output = blockingRead(f(SH_CFM_DOMAIN_LIST, ifcName), cli, id, readContext);
        return getDomainKeys(output);
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Domain> id,
        @Nonnull DomainBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String ifcName = Util.getSubinterfaceName(id);
        String domain = id.firstKeyOf(Domain.class).getDomainName();
        String output = blockingRead(f(SH_CFM_DOMAIN, ifcName, domain), cli, id, readContext);

        parseDomainLine(output, builder);
    }

    private static List<DomainKey> getDomainKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            CFM_DOMAIN_LINE::matcher,
            m -> m.group("domain"),
            s -> new DomainKey(s));
    }

    private static void parseDomainLine(String output, DomainBuilder builder) {
        ParsingUtils.parseField(output, 0,
            CFM_DOMAIN_LINE::matcher,
            m -> m,
            m -> {
                builder.setDomainName(m.group("domain"));
                builder.setConfig(new ConfigBuilder()
                    .setDomainName(m.group("domain"))
                    .build());
                builder.setMep(new MepBuilder()
                    .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet
                        .cfm._interface.cfm.domains.domain.mep.ConfigBuilder()
                        .setMaName(m.group("service"))
                        .setMepId(Integer.valueOf(m.group("mepid")))
                        .build())
                    .build());
            });
    }
}
