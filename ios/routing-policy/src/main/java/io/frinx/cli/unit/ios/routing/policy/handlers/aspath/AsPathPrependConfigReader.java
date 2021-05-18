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

package io.frinx.cli.unit.ios.routing.policy.handlers.aspath;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.routing.policy.handlers.action.BgpActionsConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.prepend.top.set.as.path.prepend.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AsPathPrependConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public AsPathPrependConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String routeMapName = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        String statementId = instanceIdentifier.firstKeyOf(Statement.class).getName();

        String output = blockingRead(BgpActionsConfigReader.SH_ROUTE_MAP, cli, instanceIdentifier, readContext);
        parseConfig(output, routeMapName, statementId, configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, String routeMapName, String statementId, ConfigBuilder builder) {
        parsePrepand(output, routeMapName, statementId, builder);
    }

    private static void parsePrepand(String output, String routeMapName, String statementId, ConfigBuilder builder) {
        List<String> lines = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .collect(Collectors.toList());

        Pattern prependPattern = Pattern.compile("set as-path prepend (?<value>.*)");
        Pattern routeMapPattern = Pattern.compile("route-map");
        boolean run = false;

        BgpActionsConfigReader.removeTopLines(lines, routeMapName, statementId);

        if (lines.size() == 1) {
            return;
        }

        lines.remove(0);

        for (String line : lines) {
            if (!run) {
                Matcher prepandMatcher = prependPattern.matcher(line);
                Matcher routeMapMatcher = routeMapPattern.matcher(line);
                if (prepandMatcher.matches()) {
                    String value = prepandMatcher.group("value");
                    String[] asnAndRepeat = parsePrependValue(value);
                    builder.setAsn(new AsNumber(new Long(asnAndRepeat[0])));
                    builder.setRepeatN(new Short(asnAndRepeat[1]));
                    run = true;
                } else if (routeMapMatcher.find()) {
                    run = true;
                }
            } else {
                break;
            }
        }
    }

    private static String[] parsePrependValue(String value) {
        String[] asnAndRepeat = new String[2];
        String[] values = value.split(" ");

        asnAndRepeat[0] = values[0];
        asnAndRepeat[1] = String.valueOf(values.length);

        return asnAndRepeat;
    }
}
