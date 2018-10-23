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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
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

    private static final String SH_RUN_BGP = "show running-config router bgp";
    private static final String SH_RUN_BGP_PER_NWINS = "show running-config router bgp %s %s %s";
    private static final Pattern AS_DOT_FORMAT_PATTERN = Pattern.compile("([0-9]+)\\.([0-9]+)");
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

        String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);

        String bgpInstance = "";
        Optional<AsNumber> optional = Optional.empty();

        if (BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(name)) {
            optional = parseDefaultAs(output);
        } else {
            bgpInstance = "instance " + name;
            optional = parseAs(output, bgpInstance);
        }

        if (optional.isPresent()) {
            output = blockingRead(String.format(SH_RUN_BGP_PER_NWINS, optional.get().getValue(),
                    bgpInstance, nwInsName),
                    cli, instanceIdentifier, readContext);
            configBuilder.setAs(optional.get());
            parseRouterId(output, configBuilder);
        }
    }

    @VisibleForTesting
    public static Optional<AsNumber> parseDefaultAs(String output) {
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(defBgp -> !defBgp.contains("instance"))
                .map(CONFIG_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("as"))
                .map(GlobalConfigReader::readASNumber)
                .map(AsNumber::new)
                .findFirst();
    }

    @VisibleForTesting
    public static Optional<AsNumber> parseAs(String output, String bgpInstance) {
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(bgp -> bgp.contains(bgpInstance))
                .map(CONFIG_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("as"))
                .map(GlobalConfigReader::readASNumber)
                .map(AsNumber::new)
                .findFirst();
    }

    @VisibleForTesting
    public static void parseRouterId(String output, ConfigBuilder configBuilder) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(ROUTER_ID_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("id"))
                .map(DottedQuad::new)
                .findFirst()
                .ifPresent(configBuilder::setRouterId);
    }

    public static Long readASNumber(String asNumber) {
        final Matcher matcher = AS_DOT_FORMAT_PATTERN.matcher(asNumber);
        if (matcher.matches()) {
            final Long firstPart = Long.valueOf(matcher.group(1));
            final Long secondPart = Long.valueOf(matcher.group(2));
            return (firstPart << 16) + secondPart;
        } else {
            return Long.valueOf(asNumber);
        }
    }
}
