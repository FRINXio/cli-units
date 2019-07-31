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
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.Ma;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.ma.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.ma.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.types.rev190619.CcmInterval;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmMaConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final String SH_CFM_DOMAIN_SERVICE =
        "show running-config ethernet cfm domain %s level %d service %s down-meps";
    private static final Pattern CONTINUITY_CHECK_LINE =
        Pattern.compile("continuity-check interval (?<interval>\\S+) loss-threshold (?<threshold>\\d)");
    private static final Pattern EFD_LINE = Pattern.compile("efd");

    private static final String SH_CFM_MEP_CROSSCHECK =
        "show running-config ethernet cfm domain %s level %d service %s down-meps mep crosscheck";
    private static final Pattern CROSSCHECK_MEPID_LINE = Pattern.compile("mep-id (?<id>\\d+)");

    private Cli cli;

    public CfmMaConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String ma = id.firstKeyOf(Ma.class).getMaName();
        builder.setMaName(ma);

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain
            .Config domain = CfmMaReader.readDomainConfig(id, readContext);

        String output = blockingRead(f(SH_CFM_DOMAIN_SERVICE, domain.getDomainName(), domain.getLevel().getValue(), ma),
            cli, id, readContext);

        ParsingUtils.parseField(output, 0,
            CONTINUITY_CHECK_LINE::matcher,
            m -> m,
            m -> {
                builder.setContinuityCheckInterval(convertCcmIntervalFromString(m.group("interval")));
                builder.setContinuityCheckLossThreshold(Long.valueOf(m.group("threshold")));
            });

        ParsingUtils.findMatch(output, EFD_LINE, builder::setEfd);

        output = blockingRead(f(SH_CFM_MEP_CROSSCHECK, domain.getDomainName(), domain.getLevel().getValue(), ma),
            cli, id, readContext);

        List<Integer> mepIds = ParsingUtils.parseFields(output, 0,
            CROSSCHECK_MEPID_LINE::matcher,
            m -> m.group("id"),
            Integer::valueOf);

        if (!mepIds.isEmpty()) {
            builder.setMepCrosscheck(mepIds);
        }
    }

    private static CcmInterval convertCcmIntervalFromString(String text) {
        if (text == null) {
            return null;
        }
        return Stream.of(CcmInterval.values())
            .filter(v -> text.equals(v.getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown continuity check message interval type " + text));
    }
}
