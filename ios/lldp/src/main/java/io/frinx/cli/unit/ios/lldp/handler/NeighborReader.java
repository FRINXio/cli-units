/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.lldp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
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

    private static final String SHOW_LLDP_NEIGHBOR = "sh lldp neighbor %s detail | include System Name";
    private static final Pattern NEIGHBOR_NAME =
            Pattern.compile("System Name: (?<neighborName>\\S+)");

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
        return ParsingUtils.parseFields(showLlldpNeighborOutput, 0,
                NEIGHBOR_NAME::matcher,
                m -> m.group("neighborName"),
                NeighborKey::new);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Neighbor> list) {
        ((NeighborsBuilder) builder).setNeighbor(list);
    }

    @Nonnull
    @Override
    public NeighborBuilder getBuilder(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier) {
        return new NeighborBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                      @Nonnull NeighborBuilder neighborBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        neighborBuilder.setId(instanceIdentifier.firstKeyOf(Neighbor.class).getId());
    }
}
