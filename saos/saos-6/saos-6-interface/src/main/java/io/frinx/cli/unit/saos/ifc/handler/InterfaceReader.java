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

package io.frinx.cli.unit.saos.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SH_PORTS = "configuration search string \"port \"";
    public static final String LAG_PORTS = "configuration search string \"aggregation create\"";
    private static final Pattern INTERFACE_ID_LINE = Pattern.compile(".*port (?<id>\\d+)($|\\s).*");
    private static final Pattern LAST_INTERFACE_ID_LINE = Pattern.compile(".*,(?<id>\\d+)($|\\s).*");
    private static final Pattern LAG_INTERFACE_ID_LINE = Pattern.compile(".*agg (?<id>\\S+)");
    private static final Pattern INTERFACE_RANGE_ID_LINE = Pattern.compile(".*port (?<id1>\\d+)-(?<id2>\\d+).*");
    private static final Pattern NEXT_RANGE_ID_LINE = Pattern.compile(".*,(?<id1>\\d+)-(?<id2>\\d+),\\d+.*");
    private static final Pattern LAST_RANGE_ID_LINE = Pattern.compile(".*,(?<id1>\\d+)-(?<id2>\\d+)[ \n].*");
    private Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        List<InterfaceKey> portIds = getAllIds(blockingRead(SH_PORTS, cli, instanceIdentifier, readContext));
        List<InterfaceKey> aggIds = getAllIds(blockingRead(LAG_PORTS, cli, instanceIdentifier, readContext));
        List<InterfaceKey> ids = new ArrayList<>();
        ids.addAll(portIds);
        ids.addAll(aggIds);
        return ids;
    }

    @VisibleForTesting
    public List<InterfaceKey> getAllIds(String output) {
        List<Pattern> patterns = Arrays.asList(INTERFACE_ID_LINE, LAST_INTERFACE_ID_LINE, LAG_INTERFACE_ID_LINE);
        List<InterfaceKey> allIds = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(l -> !l.startsWith("lldp"))
                .filter(l -> !l.startsWith("port tdm"))
                .map(line -> patterns.stream().map(pattern -> pattern.matcher(line))
                        .filter(Matcher::matches)
                        .map(matcher -> matcher.group("id"))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(InterfaceKey::new)
                        .orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Pattern> patterns2 = Arrays.asList(INTERFACE_RANGE_ID_LINE, NEXT_RANGE_ID_LINE, LAST_RANGE_ID_LINE);
        allIds.addAll(ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(l -> !l.startsWith("lldp"))
                .filter(l -> !l.startsWith("port tdm"))
                .flatMap(line -> patterns2.stream().map(pattern -> pattern.matcher(line))
                        .filter(Matcher::matches)
                        .map(InterfaceReader::getRangeOffIds)
                        .flatMap(List::stream))
                .distinct()
                .collect(Collectors.toList()));
        return allIds.stream().distinct().collect(Collectors.toList());
    }

    @VisibleForTesting
    public static List<InterfaceKey> getRangeOffIds(Matcher matcher) {
        Set<Integer> rangeIds = new HashSet<>();
        if (matcher.groupCount() == 2) {
            int id1 = Integer.parseInt(matcher.group("id1"));
            int id2 = Integer.parseInt(matcher.group("id2"));
            if (id2 > id1) {
                for (int i = id1; i <= id2; i++) {
                    rangeIds.add(i);
                }
            } else {
                throw new IllegalArgumentException("Invalid range for ports set: " + id1 + "-" + id2);
            }
        }
        return rangeIds.stream()
                .sorted()
                .map((Integer ids) -> Integer.toString(ids))
                .map(InterfaceKey::new)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder, @Nonnull ReadContext readContext) {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}