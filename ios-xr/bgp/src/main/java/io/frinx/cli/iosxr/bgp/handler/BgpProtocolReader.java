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

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        BgpReader.BgpConfigReader<Protocol, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {


    public static final String DEFAULT_BGP_INSTANCE = "default";

    private static final String SHOW_RUN_ROUTER_BGP = "do show running-config router bgp | include ^router bgp";
    private static final Pattern INSTANCE_PATTERN = Pattern.compile("router bgp (?<as>[0-9.]+) instance (?<instance>[\\S]+)");
    private static final Pattern DEFAULT_INSTANCE_PATTERN = Pattern.compile("router bgp (?<as>[0-9]+)");
    private final Cli cli;

    public BgpProtocolReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> iid,
                                       @Nonnull ReadContext context) throws ReadFailedException {
        String output = blockingRead(SHOW_RUN_ROUTER_BGP, cli, iid, context);

        return parseGbpProtocolKeys(output);
    }

    @VisibleForTesting
    public static List<ProtocolKey> parseGbpProtocolKeys(String output) {
        List<ProtocolKey> bgpProtocolKeys = ParsingUtils.parseFields(output, 0,
                INSTANCE_PATTERN::matcher,
                matcher -> matcher.group("instance"),
                v -> new ProtocolKey(TYPE, v));

        boolean isDefaultInstanceConfigured = NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(DEFAULT_INSTANCE_PATTERN::matcher)
                .anyMatch(Matcher::matches);

        if (isDefaultInstanceConfigured) {
            bgpProtocolKeys.add(new ProtocolKey(TYPE, DEFAULT_BGP_INSTANCE));
        }

        return bgpProtocolKeys;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Protocol> iid,
                                             @Nonnull ProtocolBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        ProtocolKey key = iid.firstKeyOf(Protocol.class);
        builder.setName(key.getName());
        builder.setIdentifier(key.getIdentifier());
    }
}
