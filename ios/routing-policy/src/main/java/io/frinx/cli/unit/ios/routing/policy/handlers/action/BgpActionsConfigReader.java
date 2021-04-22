/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.action;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.bgp.actions.top.bgp.actions.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpActionsConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_ROUTE_MAP = "show running-config | include ^route-map |^ set";

    private final Cli cli;

    public BgpActionsConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String routeMapName = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        String statementId = instanceIdentifier.firstKeyOf(Statement.class).getName();

        String output = blockingRead(SH_ROUTE_MAP, cli, instanceIdentifier, readContext);
        parseConfig(output, routeMapName, statementId, configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, String routeMapName, String statementId, ConfigBuilder builder) {
        parsePreference(output, routeMapName, statementId, builder);
    }

    private static void parsePreference(String output, String routeMapName, String statementId, ConfigBuilder builder) {
        List<String> lines = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .collect(Collectors.toList());

        Pattern preferencePattern = Pattern.compile("set local-preference (?<value>\\S+)");
        Pattern routeMapPattern = Pattern.compile("route-map");
        boolean run = false;

        removeTopLines(lines, routeMapName, statementId);

        if (lines.size() == 1) {
            return;
        }

        lines.remove(0);

        for (String line : lines) {
            if (!run) {
                Matcher preferenceMatcher = preferencePattern.matcher(line);
                Matcher routeMapMatcher = routeMapPattern.matcher(line);
                if (preferenceMatcher.matches()) {
                    String value = preferenceMatcher.group("value");
                    builder.setSetLocalPref(Long.parseLong(value));
                    run = true;
                } else if (routeMapMatcher.find()) {
                    run = true;
                }
            } else {
                break;
            }
        }
    }

    public static void removeTopLines(List<String> lines, String routeMapName, String statementId) {
        Pattern pattern = Pattern.compile("route-map " + routeMapName + " permit " + statementId);
        Pattern patternDeny = Pattern.compile("route-map " + routeMapName + " deny " + statementId);
        boolean run = false;

        while (!run) {
            String line = lines.get(0);
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find() && !patternDeny.matcher(line).find()) {
                lines.remove(0);
            } else {
                run = true;
            }
        }
    }
}
