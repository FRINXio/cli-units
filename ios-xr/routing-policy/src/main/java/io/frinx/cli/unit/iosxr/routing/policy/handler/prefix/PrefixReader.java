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

package io.frinx.cli.unit.iosxr.routing.policy.handler.prefix;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.PrefixKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixReader implements CliConfigListReader<Prefix, PrefixKey, PrefixBuilder> {

    private static final String SH_PREFIX_SET = "show running-config prefix-set %s";
    private static final Pattern PREFIX_PATTERN = Pattern.compile("\\s+(?<network>[0-9a-f.:]+)/?(?<mask>[0-9]*)?\\s*"
            + "(?<modifiers>.*)\\s*");

    private static final Pattern GE_PATTERN = Pattern.compile(".*ge (?<value>[0-9]+)\\s*.*");
    private static final Pattern LE_PATTERN = Pattern.compile(".*le (?<value>[0-9]+)\\s*.*");
    private static final Pattern EQ_PATTERN = Pattern.compile(".*eq (?<value>[0-9]+)\\s*.*");

    private static final String MASK_LENGTH_RANGE_PATTERN = "%s..%s";
    public static final String EXACT_MASK_LENGTH = "exact";

    private final Cli cli;

    public PrefixReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<PrefixKey> getAllIds(@Nonnull InstanceIdentifier<Prefix> id,
                                     @Nonnull ReadContext context) throws ReadFailedException {
        PrefixSetKey prefixSetKey = id.firstKeyOf(PrefixSet.class);
        String output = blockingRead(f(SH_PREFIX_SET, prefixSetKey.getName()), cli, id, context);
        return parseIds(output);
    }

    @VisibleForTesting
    static List<PrefixKey> parseIds(String output) {
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(PREFIX_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(PrefixReader::toKey)
                .distinct()
                .collect(Collectors.toList());
    }

    private static PrefixKey toKey(Matcher matcher) {
        String network = matcher.group("network")
                .trim();
        String mask = matcher.group("mask")
                .trim();
        String modifiers = matcher.group("modifiers")
                .trim();

        int maxLength = network.contains(":") ? 128 : 32;

        String networkString = mask.isEmpty() ? network + "/" + Integer.toString(maxLength) : network + "/" + mask;
        IpPrefix ipPrefix = new IpPrefix(networkString.toCharArray());

        String maskLengthRange = parseMaskLength(modifiers, ipPrefix);

        return new PrefixKey(ipPrefix, maskLengthRange);
    }

    private static String parseMaskLength(String modifiers, IpPrefix ipPrefix) {
        Matcher geMatcher = GE_PATTERN.matcher(modifiers);
        Matcher leMatcher = LE_PATTERN.matcher(modifiers);
        Matcher eqMatcher = EQ_PATTERN.matcher(modifiers);

        int maxLength = ipPrefix.getIpv4Prefix() != null ? 32 : 128;
        String maskLengthRange;

        // Possible combinations of mask lengths

        if (eqMatcher.matches()) {
            //   1.2.3.4/4 eq 5
            maskLengthRange = String.format(MASK_LENGTH_RANGE_PATTERN,
                    eqMatcher.group("value"),
                    eqMatcher.group("value"));
        } else if (geMatcher.matches() && leMatcher.matches()) {
            //   1.2.3.4/4 ge 5 le 16
            maskLengthRange = String.format(MASK_LENGTH_RANGE_PATTERN,
                    geMatcher.group("value"),
                    leMatcher.group("value"));
        } else if (geMatcher.matches() && !leMatcher.matches()) {
            //   1.2.3.4/4 ge 5
            maskLengthRange = String.format(MASK_LENGTH_RANGE_PATTERN,
                    geMatcher.group("value"),
                    maxLength);
        } else if (!geMatcher.matches() && leMatcher.matches()) {
            //   1.2.3.4/4 le 16
            maskLengthRange = String.format(MASK_LENGTH_RANGE_PATTERN,
                    0,
                    leMatcher.group("value"));
        } else {
            //   1.2.3.4/4 or 1.2.3.4
            maskLengthRange = EXACT_MASK_LENGTH;
        }
        return maskLengthRange;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Prefix> id,
                                      @Nonnull PrefixBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        PrefixKey prefixKey = id.firstKeyOf(Prefix.class);
        builder.setIpPrefix(prefixKey.getIpPrefix());
        builder.setMasklengthRange(prefixKey.getMasklengthRange());
    }
}
