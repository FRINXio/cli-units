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
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.Ma;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.MaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.MaKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmMaReader implements CliConfigListReader<Ma, MaKey, MaBuilder> {
    private static final String SH_CFM_DOMAIN_SERVICE =
        "show running-config ethernet cfm domain %s level %d | include ^ {2}service";

    private static final Pattern CFM_DOMAIN_SERVICE_LINE = Pattern.compile("service (?<service>\\S+) down-meps");
    private Cli cli;

    public CfmMaReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<MaKey> getAllIds(
        @NotNull InstanceIdentifier<Ma> id,
        @NotNull ReadContext readContext) throws ReadFailedException {

        Config domain = readDomainConfig(id, readContext);

        String output = blockingRead(f(SH_CFM_DOMAIN_SERVICE, domain.getDomainName(), domain.getLevel().getValue()),
            cli, id, readContext);

        return getMaKeys(output);
    }

    @Override
    public void readCurrentAttributes(
        @NotNull InstanceIdentifier<Ma> id,
        @NotNull MaBuilder builder,
        @NotNull ReadContext readContext) throws ReadFailedException {

        MaKey key = id.firstKeyOf(Ma.class);
        builder.setMaName(key.getMaName());
    }

    private static List<MaKey> getMaKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            CFM_DOMAIN_SERVICE_LINE::matcher,
            m -> m.group("service"),
            s -> new MaKey(s));
    }

    public static Config readDomainConfig(InstanceIdentifier<?> id, ReadContext readContext) {
        InstanceIdentifier<Config> configId = RWUtils.cutId(id, Domain.class).child(Config.class);

        return readContext.read(configId).orElse(null);
    }
}