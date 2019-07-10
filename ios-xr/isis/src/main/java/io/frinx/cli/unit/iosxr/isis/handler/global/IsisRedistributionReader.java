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

package io.frinx.cli.unit.iosxr.isis.handler.global;

import com.google.common.collect.ImmutableSet;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.Redistribution;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.RedistributionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.RedistributionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.Af;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.AfKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisRedistributionReader
        implements CliConfigListReader<Redistribution, RedistributionKey, RedistributionBuilder> {

    private Cli cli;

    public static final Set<String> SUPPORTED_REDISTRIBUTION_PROTOCOLS = ImmutableSet.of("isis");

    private static final String SH_AFI = "show running-config router isis %s address-family %s %s";
    private static final Pattern REDISTRIBUTE_LINE = Pattern.compile(
        String.format("redistribute (?<protocol>%s) (?<instance>\\S+).*",
            StringUtils.join(SUPPORTED_REDISTRIBUTION_PROTOCOLS, "|")));

    public IsisRedistributionReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<RedistributionKey> getAllIds(
        @Nonnull InstanceIdentifier<Redistribution> id,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String insName = id.firstKeyOf(Protocol.class).getName();
        AfKey afKey = id.firstKeyOf(Af.class);

        String output = blockingRead(
            f(SH_AFI,
                insName,
                IsisGlobalAfiSafiReader.convertAfiTypeToString(afKey.getAfiName()),
                IsisGlobalAfiSafiReader.convertSafiTypeToString(afKey.getSafiName())),
            cli, id, readContext);
        return getRedistributionKeys(output);
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Redistribution> id,
        @Nonnull RedistributionBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        RedistributionKey key = id.firstKeyOf(Redistribution.class);

        builder.setProtocol(key.getProtocol());
        builder.setInstance(key.getInstance());
    }

    private static List<RedistributionKey> getRedistributionKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            REDISTRIBUTE_LINE::matcher,
            m -> m,
            m -> new RedistributionKey(m.group("instance"), m.group("protocol")));
    }
}
