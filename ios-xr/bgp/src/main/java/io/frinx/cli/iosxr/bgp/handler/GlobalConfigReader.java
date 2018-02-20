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

package io.frinx.cli.iosxr.bgp.handler;

import static io.frinx.cli.iosxr.bgp.handler.BgpProtocolReader.DEFAULT_BGP_INSTANCE;
import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigReader implements BgpReader.BgpConfigReader<Config, ConfigBuilder> {

    private static final String SH_RUN_BGP = "do show running-config router bgp";
    private static final Pattern CONFIG_LINE = Pattern.compile(".*router bgp (?<as>\\S+).*");
    private static final Pattern ROUTER_ID_LINE = Pattern.compile(".*bgp router-id (?<id>\\S+).*");

    private Cli cli;

    public GlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((GlobalBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        String output = blockingRead(SH_RUN_BGP, cli, instanceIdentifier, readContext);

        if (DEFAULT_BGP_INSTANCE.equals(name)) {
            parseDefaultAs(output, configBuilder);
        } else {
            parseAs(output, name, configBuilder);
        }

        parseRouterId(output, configBuilder);
    }

    @VisibleForTesting
    public static void parseDefaultAs(String output, ConfigBuilder configBuilder) {
        NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(defBgp -> !defBgp.contains("instance"))
                .map(CONFIG_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("as"))
                .map(Long::valueOf)
                .map(AsNumber::new)
                .findFirst()
                .ifPresent(configBuilder::setAs);
    }

    @VisibleForTesting
    public static void parseAs(String output, String name, ConfigBuilder cBuilder) {
        NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(bgp -> bgp.contains(String.format("instance %s", name)))
                .map(CONFIG_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("as"))
                .map(Long::valueOf)
                .map(AsNumber::new)
                .findFirst()
                .ifPresent(cBuilder::setAs);
    }

    public static void parseRouterId(String output, ConfigBuilder configBuilder) {
        NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(ROUTER_ID_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("id"))
                .map(DottedQuad::new)
                .findFirst()
                .ifPresent(configBuilder::setRouterId);
    }
}
