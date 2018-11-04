/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.lacp.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpPeriodType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.Member;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.MemberBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.member.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.member.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MemberConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_SINGLE_INTERFACE = "show running-config interface %s";
    private static final Pattern LACP_BUNDLE_AND_MODE_LINE = Pattern.compile("\\s*bundle id (?<id>\\d+)( mode "
            + "(?<mode>active|passive))?.*");
    private static final Pattern LACP_PERIOD_LINE = Pattern.compile("\\s*lacp period short.*");

    private final Cli cli;

    public MemberConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Member.class).getInterface();
        final String searchedInterface = String.format(SH_SINGLE_INTERFACE, ifcName);
        final String interfaceConfiguration = blockingRead(searchedInterface, cli, instanceIdentifier, readContext);
        parseLacpConfig(configBuilder, ifcName, interfaceConfiguration);
    }

    static void parseLacpConfig(@Nonnull ConfigBuilder configBuilder, String ifcName, String interfaceConfiguration) {
        configBuilder.setLacpMode(parseLacpActivityType(interfaceConfiguration).get());
        configBuilder.setInterval(parseLacpPeriodType(interfaceConfiguration).get());
        configBuilder.setInterface(ifcName);
    }

    private static AtomicReference<LacpActivityType> parseLacpActivityType(@Nonnull String interfaceConfiguration) {
        final AtomicReference<LacpActivityType> fetchedMode = new AtomicReference<>();
        // default mode should be ON; now null configuration references to ON mode
        ParsingUtils.parseField(interfaceConfiguration,
                LACP_BUNDLE_AND_MODE_LINE::matcher, matcher -> matcher.group("mode"), mode ->
                        fetchedMode.set(LacpActivityType.valueOf(mode.toUpperCase())));
        return fetchedMode;
    }

    static AtomicReference<LacpPeriodType> parseLacpPeriodType(@Nonnull String interfaceConfiguration) {
        final AtomicReference<LacpPeriodType> fetchedPeriodType = new AtomicReference<>();
        fetchedPeriodType.set(LacpPeriodType.SLOW);
        // todo: LACP period type can be manually set to value between 100 and 1000 ms, openconfig doesn't support it
        ParsingUtils.NEWLINE.splitAsStream(interfaceConfiguration)
                .map(String::trim)
                .map(LACP_PERIOD_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> LacpPeriodType.FAST)
                .findFirst()
                .ifPresent(fetchedPeriodType::set);
        return fetchedPeriodType;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((MemberBuilder) builder).setConfig(config);
    }
}
