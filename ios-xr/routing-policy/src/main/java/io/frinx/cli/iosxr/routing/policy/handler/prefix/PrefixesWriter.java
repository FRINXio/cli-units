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

package io.frinx.cli.iosxr.routing.policy.handler.prefix;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.Prefixes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixesWriter implements CliWriter<Prefixes> {

    private static final Pattern MASK_RANGE_PATTERN = Pattern.compile("(?<ge>[0-9]+)\\.\\.(?<le>[0-9]+)|exact");

    @VisibleForTesting
    static final String TEMPLATE = "prefix-set {$name}\n"
            + "{% loop in $prefixes as $p %}\n"
            + "{$p.prefix}"
            + "{.if ($p.ge) } ge {$p.ge}{/if}"
            + "{.if ($p.le) } le {$p.ge}{/if}"
            + "{% divider %}"
            + ",\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "\n\n"
            + "end-set";

    private final Cli cli;

    public PrefixesWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Prefixes> instanceIdentifier,
                                       @Nonnull Prefixes prefixes,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        PrefixSetKey prefixSetKey = instanceIdentifier.firstKeyOf(PrefixSet.class);
        List<Prefix> prefixList = prefixes.getPrefix() == null ? Collections.emptyList() : prefixes.getPrefix();

        List<ConfigDto> transformedPrefixes = transformPrefixes(prefixList);

        blockingWriteAndRead(cli, instanceIdentifier, prefixes,
                fT(TEMPLATE,
                        "name", prefixSetKey.getName(),
                        "prefixes", transformedPrefixes));
    }

    @VisibleForTesting
    static List<ConfigDto> transformPrefixes(List<Prefix> prefixList) {
        return prefixList.stream()
                .filter(p -> p.getConfig() != null)
                .map(Prefix::getConfig)
                .map(PrefixesWriter::configToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Prefixes> id,
                                        @Nonnull Prefixes dataBefore,
                                        @Nonnull Prefixes dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        // on xr, when updating prefix-set it deletes previous content, so just put the new set directly
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    private static ConfigDto configToDto(Config config) {
        return ConfigDto.fromConfig(config);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Prefixes> instanceIdentifier,
                                        @Nonnull Prefixes prefixes,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        PrefixSetKey prefixSetKey = instanceIdentifier.firstKeyOf(PrefixSet.class);

        // For delete, it's sufficient to just open the prefix set and close, it will delete all of its content
        blockingWriteAndRead(cli, instanceIdentifier, prefixes,
                fT(TEMPLATE,
                        "name", prefixSetKey.getName(),
                        "prefixes", Collections.emptyList()));
    }

    public static final class ConfigDto {

        private final String prefix;
        private final Integer ge;
        private final Integer le;

        ConfigDto(String prefix, Integer ge, Integer le) {
            this.prefix = prefix;
            this.ge = ge;
            this.le = le;
        }

        public String getPrefix() {
            return prefix;
        }

        public int getGe() {
            return ge;
        }

        public int getLe() {
            return le;
        }

        private static ConfigDto fromConfig(Config config) {
            Matcher matcher = MASK_RANGE_PATTERN.matcher(config.getMasklengthRange());
            checkArgument(matcher.matches(), "Mask length range in unsupported format: %s, should be: %s",
                    config.getMasklengthRange(), MASK_RANGE_PATTERN.pattern());

            String geGroup = matcher.group("ge");
            String leGroup = matcher.group("le");
            Integer ge = Strings.isNullOrEmpty(geGroup) ? null : Integer.parseInt(geGroup);
            Integer le = Strings.isNullOrEmpty(leGroup) ? null : Integer.parseInt(leGroup);

            return new ConfigDto(new String(config.getIpPrefix()
                    .getValue()).intern(), ge, le);
        }
    }
}
