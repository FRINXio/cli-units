/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy.handlers.tags;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.Tag;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.TagBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.TagKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TagReader implements CliConfigListReader<Tag, TagKey, TagBuilder> {

    private final Cli cli;

    public TagReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<TagKey> getAllIds(@NotNull InstanceIdentifier<Tag> instanceIdentifier,
                                  @NotNull ReadContext readContext) throws ReadFailedException {
        final String routeMapName = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        final String sequence = instanceIdentifier.firstKeyOf(Statement.class).getName();
        String commandForPermit = String.format("show running-config | section route-map %s permit %s",
                routeMapName, sequence);
        String commandForDeny = String.format("show running-config | section route-map %s deny %s",
                routeMapName, sequence);
        String output = blockingRead(commandForPermit, cli, instanceIdentifier, readContext);
        if (output.isEmpty()) {
            output = blockingRead(commandForDeny, cli, instanceIdentifier, readContext);
        }
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<TagKey> getAllIds(String output) {
        final String regex = String.format(".*tag (?<tagList>\\d+(( \\d+)+)?).*");
        return ParsingUtils.NEWLINE.splitAsStream(output)
                .map(Pattern.compile(regex)::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("tagList"))
                .findFirst()
                .map(v -> Arrays.stream(v.split(" "))
                .map(Long::valueOf)
                .map(TagKey::new)
                .collect(Collectors.toList()))
                .orElse(List.of());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Tag> instanceIdentifier,
                                      @NotNull TagBuilder tagSetBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        tagSetBuilder.setName(instanceIdentifier.firstKeyOf(Tag.class).getName());
    }
}