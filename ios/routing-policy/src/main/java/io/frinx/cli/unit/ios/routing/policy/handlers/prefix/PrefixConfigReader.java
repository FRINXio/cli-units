/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.prefix;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.routing.policy.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.SETOPERATION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String PREFIX_LINE = "ip(v6)? prefix-list %s seq (?<sequenceId>\\d+) (?<operation>\\S+) %s"
            + "( ge (?<minimum>\\d+))?( le (?<maximum>\\d+))?.*";

    private final Cli cli;

    public PrefixConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        PrefixSetKey prefixSetKey = id.firstKeyOf(PrefixSet.class);
        PrefixKey prefixKey = id.firstKeyOf(Prefix.class);

        builder.setIpPrefix(prefixKey.getIpPrefix());
        builder.setMasklengthRange(prefixKey.getMasklengthRange());

        String output = blockingRead(PrefixSetReader.SH_ALL_PREFIX_SETS, cli, id, ctx);
        parseConfig(builder, output, Util.getIpPrefixAsString(builder.getIpPrefix()), prefixSetKey.getName());
    }

    @VisibleForTesting
    void parseConfig(ConfigBuilder builder, String output, String prefix, String key) {
        Pattern prefixPattern = Pattern.compile(f(PREFIX_LINE, key, prefix));

        PrefixConfigAugBuilder augBuilder = new PrefixConfigAugBuilder();
        setSequenceId(output, augBuilder, prefixPattern);
        setOperation(output, augBuilder, prefixPattern);
        setMinimumPrefixLength(output, augBuilder, prefixPattern);
        setMaximumPrefixLength(output, augBuilder, prefixPattern);
        builder.addAugmentation(PrefixConfigAug.class, augBuilder.build());
    }

    private void setOperation(String output, PrefixConfigAugBuilder augBuilder, Pattern prefixPattern) {
        ParsingUtils.parseField(output, prefixPattern::matcher,
            matcher -> matcher.group("operation"),
            value -> augBuilder.setOperation(getOperation(value)));
    }

    private void setSequenceId(String output, PrefixConfigAugBuilder augBuilder, Pattern prefixPattern) {
        ParsingUtils.parseField(output, prefixPattern::matcher,
            matcher -> matcher.group("sequenceId"),
            value -> augBuilder.setSequenceId(Long.parseLong(value)));
    }

    private void setMinimumPrefixLength(String output, PrefixConfigAugBuilder augBuilder, Pattern prefixPattern) {
        ParsingUtils.parseField(output, prefixPattern::matcher,
            matcher -> matcher.group("minimum"),
            value -> augBuilder.setMinimumPrefixLength(Short.parseShort(value)));
    }

    private void setMaximumPrefixLength(String output, PrefixConfigAugBuilder augBuilder, Pattern prefixPattern) {
        ParsingUtils.parseField(output, prefixPattern::matcher,
            matcher -> matcher.group("maximum"),
            value -> augBuilder.setMaximumPrefixLength(Short.parseShort(value)));
    }

    private Class<? extends SETOPERATION> getOperation(String value) {
        switch (value) {
            case "permit":
                return PERMIT.class;
            case "deny":
                return DENY.class;
            default:
                throw new IllegalArgumentException("Did not match operation for value: " + value);
        }
    }

}