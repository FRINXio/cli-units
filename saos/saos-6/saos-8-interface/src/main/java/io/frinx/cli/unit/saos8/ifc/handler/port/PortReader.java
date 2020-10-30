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

package io.frinx.cli.unit.saos8.ifc.handler.port;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;

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

public class PortReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SH_PORTS = "configuration search string \" port \"";
    public static final String LAG_PORTS = "configuration search string \"aggregation create\"";
    private static final Pattern SINGLE_INTERFACE_ID_LINE = Pattern.compile(".*port (?<id>\\d+)($|\\s).*");
    private static final Pattern LAST_SINGLE_INTERFACE_ID_LINE = Pattern.compile(".*port.*,(?<id>\\d+)($|\\s).*");
    private static final Pattern LAG_SINGLE_INTERFACE_ID_LINE = Pattern.compile(".*agg (?<id>\\S+)");
    private static final Pattern SINGLE_INTERFACE_RANGE_ID_LINE = Pattern.compile(".*port (?<id1>\\d+)-(?<id2>\\d+).*");
    private static final Pattern INTERFACE_ID_LINE = Pattern.compile(".*port (?<id1>\\d+)/(?<id2>\\d+).*");
    private static final Pattern INTERFACE_NAME_ID_LINE = Pattern.compile(".*port (?<id>[A-Z]{2}\\S+[^, \n]).*");
    private static final Pattern LAST_INTERFACE_ID_LINE = Pattern.compile(".*port.*,(?<id1>\\d+)/(?<id2>\\d+).*");
    private static final Pattern LAG_INTERFACE_ID_LINE = Pattern.compile(".*agg (?<id>\\S+)");
    private static final Pattern INTERFACE_RANGE_ID_LINE =
            Pattern.compile(".*port (?<id1>\\d+)/(?<id2>\\d+)-(?<id3>\\d+)/(?<id4>\\d+).*");
    private static final Pattern NEXT_RANGE_ID_LINE =
            Pattern.compile(".*port.*,(?<id1>\\d+)/(?<id2>\\d+)-(?<id3>\\d+)/(?<id4>\\d+),\\d+.*");
    private static final Pattern LAST_RANGE_ID_LINE =
            Pattern.compile(".*port.*,(?<id1>\\d+)/(?<id2>\\d+)-(?<id3>\\d+)/(?<id4>\\d+).*");

    public static Check ethernetCheck = BasicCheck.checkData(
            ChecksMap.DataCheck.InterfaceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.InterfaceConfig.TYPE_ETHERNET_CSMACD);

    public static Check lagCheck = BasicCheck.checkData(
            ChecksMap.DataCheck.InterfaceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.InterfaceConfig.TYPE_IEEE802AD_LAG);

    private Cli cli;

    public PortReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(cli, this, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    public static List<InterfaceKey> getAllIds(Cli cli, CliReader reader,
                                               @Nonnull InstanceIdentifier id,
                                               @Nonnull ReadContext context) throws ReadFailedException {

        String portOutput = reader.blockingRead(SH_PORTS, cli, id, context);
        String lagOutput = reader.blockingRead(LAG_PORTS, cli, id, context);
        String output = portOutput.concat(lagOutput);
        List<Pattern> patterns = Arrays.asList(LAST_INTERFACE_ID_LINE, INTERFACE_ID_LINE);
        List<InterfaceKey> allIds = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(l -> !l.startsWith("port tdm"))
                .filter(l -> (l.contains("port") || l.contains("agg")))
                .map(line -> patterns.stream().map(pattern -> pattern.matcher(line))
                        .filter(Matcher::matches)
                        .map(matcher -> (matcher.group("id1") + "/" + matcher.group("id2")))
                        .findFirst()
                        .map(InterfaceKey::new)
                        .orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Pattern> patterns1 = Arrays.asList(SINGLE_INTERFACE_RANGE_ID_LINE);
        allIds.addAll(ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(l -> !l.startsWith("port tdm"))
                .filter(l -> (l.contains("port") || l.contains("agg")))
                .flatMap(line -> patterns1.stream().map(pattern -> pattern.matcher(line))
                        .filter(Matcher::matches)
                        .map(PortReader::getSingleRangeOffIds)
                        .flatMap(List::stream))
                .distinct()
                .collect(Collectors.toList()));
        List<Pattern> patterns2 = Arrays.asList(INTERFACE_NAME_ID_LINE, LAG_INTERFACE_ID_LINE,
                SINGLE_INTERFACE_ID_LINE, LAST_SINGLE_INTERFACE_ID_LINE, LAG_SINGLE_INTERFACE_ID_LINE);
        allIds.addAll(ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(l -> !l.startsWith("port tdm"))
                .filter(l -> (l.contains("port") || l.contains("agg")))
                .map(line -> patterns2.stream().map(pattern -> pattern.matcher(line))
                        .filter(Matcher::matches)
                        .map(matcher -> matcher.group("id"))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(InterfaceKey::new)
                        .orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));
        List<Pattern> patterns3 = Arrays.asList(INTERFACE_RANGE_ID_LINE, NEXT_RANGE_ID_LINE, LAST_RANGE_ID_LINE);
        allIds.addAll(ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(l -> !l.startsWith("port tdm"))
                .filter(l -> (l.contains("port") || l.contains("agg")))
                .flatMap(line -> patterns3.stream().map(pattern -> pattern.matcher(line))
                        .filter(Matcher::matches)
                        .map(PortReader::getRangeOffIds)
                        .flatMap(List::stream))
                .distinct()
                .collect(Collectors.toList()));
        return allIds.stream().distinct().collect(Collectors.toList());
    }

    @VisibleForTesting
    public static List<InterfaceKey> getRangeOffIds(Matcher matcher) {
        List<String> rangeIds = new ArrayList<>();
        if (matcher.groupCount() == 4) {
            int id1 = Integer.parseInt(matcher.group("id1"));
            int id2 = Integer.parseInt(matcher.group("id2"));
            int id3 = Integer.parseInt(matcher.group("id3"));
            int id4 = Integer.parseInt(matcher.group("id4"));
            if (id3 > id1 || (id3 == id1 && id4 > id2)) {
                while ((id3 > id1) || (id4 >= id2)) {
                    rangeIds.add(id1 + "/" + id2);
                    if (id2 == 20) {
                        id2 = 1;
                        id1++;
                    } else {
                        id2++;
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid range for ports set: " + id1 + "/"
                        + id2 + " - " + id3 + "/" + id4);
            }
        }
        return rangeIds.stream()
                .map(InterfaceKey::new)
                .distinct()
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    public static List<InterfaceKey> getSingleRangeOffIds(Matcher matcher) {
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

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
