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

package io.frinx.cli.unit.dasan.utils;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class DasanCliUtil {

    private DasanCliUtil() {

    }

    private static final Pattern FIELD_SEPARATOR_PATTERN = Pattern.compile(",");

    private static final Pattern RANGE_PATTERN = Pattern.compile("^(?<start>[^-]+)-(?<end>.*)");

    private static final Pattern PHYS_PORT_ID_LINE = Pattern.compile("^(?<id>[^\\s]+)\\s+.*");

    private static final String SHOW_ALL_PORTS = "show port";

    /**
     * Execute "show port" command to obtain the list of physical ports.
     * @param cli specified as the cli argument of the blockingRead method
     * @param cliReader reader for calling blockingRead method.
     * @param id specified as the id argument of the blockingRead method
     * @param readContext specified as the readContext argument of the blockingRead method
     * @return physical ports
     * @throws ReadFailedException CLI execution error
     */
    public static <O extends DataObject> List<String> getPhysicalPorts(Cli cli,
            CliReader<O, ? extends Builder<O>> cliReader, @Nonnull InstanceIdentifier<O> id,
            @Nonnull ReadContext readContext) throws ReadFailedException {

        return parsePhysicalPorts(cliReader.blockingRead(SHOW_ALL_PORTS, cli, id, readContext));
    }

    /**
     * Skip the title line from the execution result of the "show port" command<br>
     * and extract the physical port from the line data.
     * <ul>
     *   <li> skip 4 rows which are title lines.
     *   <li> extract the first field delimited by spaces, and treat it as a physical port id.
     *   <li> return list of the extracted physical port ids.
     * </ul>
     * @param output result of the "show port" command
     * @return physical port ids
     */
    public static List<String> parsePhysicalPorts(String output) {

        return ParsingUtils.NEWLINE.splitAsStream(output)
                .skip(4)
                .map(String::trim)
                .map(PHYS_PORT_ID_LINE::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("id"))
                .collect(Collectors.toList());
    }

    /**
     * Check if the target port is within range.
     * @param ports all physical ports
     * @param ranges range
     * @param target target port
     * @return true: Target port is included in range.
     */
    public static boolean containsPort(List<String> ports, String ranges, String target) {
        return parsePortRanges(ports, ranges).contains(target);
    }

    /**
     * convert range notation to individual notation.
     * @param ports all physical ports
     * @param ranges range notation
     * @return List of ports in range
     */
    public static Set<String> parsePortRanges(List<String> ports, String ranges) {
        Map<String, Integer> portIndex = new HashMap<>();
        int count = 0;
        for (String name : ports) {
            portIndex.put(name, Integer.valueOf(count));
            count ++;
        }

        return parsePortRanges(ports, portIndex, ranges);
    }

    /**
     * convert range notation to individual notation.
     * @param ports all physical ports
     * @param portIndex index of physical ports
     * @param ranges range notation
     * @return List of ports in range
     */
    public static Set<String> parsePortRanges(List<String> ports, Map<String, Integer> portIndex, String ranges) {

        Set<String> ret = new HashSet<>();

        Matcher fieldMatcher = FIELD_SEPARATOR_PATTERN.matcher(ranges);
        Matcher rangeMatcher = RANGE_PATTERN.matcher(ranges);

        if (fieldMatcher.find()) {
            ret.addAll(
                    FIELD_SEPARATOR_PATTERN.splitAsStream(ranges)
                    .flatMap(range -> parsePortRanges(ports, portIndex, range).stream())
                    .collect(Collectors.toSet())
            );
        } else if (rangeMatcher.matches()) {
            Integer start = portIndex.get(rangeMatcher.group("start"));
            Integer end   = portIndex.get(rangeMatcher.group("end"));

            ret.addAll(ports.subList(start.intValue(), end.intValue() + 1));

        } else if (portIndex.containsKey(ranges)) {
            ret.add(ranges);
        }
        return ret;
    }

    /**
     * Check if the target is within range (only number format).
     * @param ranges range notation
     * @param target target id
     * @return true: Target id is included in range.
     */
    public static boolean containsId(String ranges, String target) {
        return parseIdRanges(ranges).contains(target);
    }

    /**
     * convert range notation to individual notation.
     * @param ranges range notation
     * @return list of individual notation
     */
    public static Set<String> parseIdRanges(String ranges) {
        Matcher fieldMatcher = FIELD_SEPARATOR_PATTERN.matcher(ranges);
        Matcher rangeMatcher = RANGE_PATTERN.matcher(ranges);

        Set<String> ret = new HashSet<>();

        if (fieldMatcher.find()) {
            ret.addAll(
                FIELD_SEPARATOR_PATTERN.splitAsStream(ranges)
                .flatMap(range -> parseIdRanges(range).stream())
                .collect(Collectors.toSet())
            );
        } else if (rangeMatcher.matches()) {
            Integer start = Integer.valueOf(rangeMatcher.group("start"));
            Integer end   = Integer.valueOf(rangeMatcher.group("end"));
            for (int i = start; i <= end; i++) {
                ret.add(String.valueOf(i));
            }
        } else {
            ret.add(ranges);
        }
        return ret;
    }
}
