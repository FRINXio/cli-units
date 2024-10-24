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

package io.frinx.cli.unit.ios.cdp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lldp.rev160516.lldp.neighbor.top.neighbors.NeighborKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborReader implements CliOperListReader<Neighbor, NeighborKey, NeighborBuilder> {

    private Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    static final String SH_CDP_NEIGH = "show cdp neighbors %s detail | include Device ID|Interface";

    @NotNull
    @Override
    public List<NeighborKey> getAllIds(@NotNull InstanceIdentifier<Neighbor> instanceIdentifier,
                                       @NotNull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String output = blockingRead(String.format(SH_CDP_NEIGH, ifcName), cli, instanceIdentifier, readContext);
        return parseNeighborIds(output);
    }

    private static final Pattern CDP_NEIGHBOR_LINE = Pattern.compile("Device ID: (?<id>[\\S]+)");

    @VisibleForTesting
    static List<NeighborKey> parseNeighborIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                CDP_NEIGHBOR_LINE::matcher,
            matcher -> matcher.group("id"),
                NeighborKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Neighbor> instanceIdentifier,
                                      @NotNull NeighborBuilder neighborBuilder,
                                      @NotNull ReadContext readContext) {
        neighborBuilder.setId(instanceIdentifier.firstKeyOf(Neighbor.class).getId());
    }
}