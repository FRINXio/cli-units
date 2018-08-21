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

package io.frinx.cli.unit.nexus.lldp.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborReader implements CliOperListReader<Neighbor, NeighborKey, NeighborBuilder> {

    static final String SHOW_LLDP_NEIGHBOR = "show lldp neighbors interface %s detail";
    private static final Pattern CHASSIS = Pattern.compile("Chassis id: (?<chassis>.+)");
    private static final Pattern PORT = Pattern.compile("Port id: (?<portId>.+)");
    static final String KEY_FORMAT = "%s Port:%s";

    private Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<NeighborKey> getAllIds(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                       @Nonnull ReadContext readContext) throws ReadFailedException {
        String intName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String showCommand = String.format(SHOW_LLDP_NEIGHBOR, intName);
        return parseNeighborIds(blockingRead(showCommand, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<NeighborKey> parseNeighborIds(String showLlldpNeighborOutput) {
        List<String> collect = ParsingUtils.NEWLINE.splitAsStream(showLlldpNeighborOutput)
                .map(String::trim)
                .filter(l -> CHASSIS.matcher(l).matches() || PORT.matcher(l).matches())
                .collect(Collectors.toList());

        List<NeighborKey> keys = Lists.newArrayList();
        for (int i = 0; i < collect.size(); i = i + 2) {
            String chassisString = collect.get(i);
            String portString = collect.get(i + 1);

            Matcher chMatcher = CHASSIS.matcher(chassisString);
            Preconditions.checkState(chMatcher.matches());
            String chassis = chMatcher.group("chassis");
            Matcher ppMatcher = PORT.matcher(portString);
            Preconditions.checkState(ppMatcher.matches());
            String port = ppMatcher.group("portId");

            keys.add(new NeighborKey(String.format(KEY_FORMAT, chassis, port)));
        }

        return keys;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Neighbor> list) {
        ((NeighborsBuilder) builder).setNeighbor(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                      @Nonnull NeighborBuilder neighborBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        neighborBuilder.setId(instanceIdentifier.firstKeyOf(Neighbor.class).getId());
    }
}
