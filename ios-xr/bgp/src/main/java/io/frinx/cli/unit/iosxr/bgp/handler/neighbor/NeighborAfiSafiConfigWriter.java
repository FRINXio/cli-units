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

package io.frinx.cli.unit.iosxr.bgp.handler.neighbor;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeAfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public NeighborAfiSafiConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get()
                .getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);

        blockingWriteAndRead(cli, id, config,
                f("router bgp %s %s %s", g.getConfig()
                        .getAs()
                        .getValue(), instName, nwInsName),
                f("neighbor %s", new String(id.firstKeyOf(Neighbor.class)
                        .getNeighborAddress()
                        .getValue())),
                f("address-family %s", GlobalAfiSafiReader.transformAfiToString(config.getAfiSafiName())),
                getReconfigurationCommand(config, false),
                "root");
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get()
                .getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);

        Config reconfig = dataAfter;
        boolean delete = false;
        if ((dataBefore.getAugmentation(BgpNeAfAug.class)
                != null
                && dataBefore.getAugmentation(BgpNeAfAug.class)
                .getSoftReconfiguration()
                != null)
                && (dataAfter.getAugmentation(BgpNeAfAug.class)
                == null
                || dataBefore.getAugmentation(BgpNeAfAug.class)
                .getSoftReconfiguration()
                == null)) {
            delete = true;
            reconfig = dataBefore;
        }
        blockingWriteAndRead(cli, id, dataAfter,
                f("router bgp %s %s %s", g.getConfig()
                        .getAs()
                        .getValue(), instName, nwInsName),
                f("neighbor %s", new String(id.firstKeyOf(Neighbor.class)
                        .getNeighborAddress()
                        .getValue())),
                f("address-family %s", GlobalAfiSafiReader.transformAfiToString(dataAfter.getAfiSafiName())),
                getReconfigurationCommand(reconfig, delete),
                "root");
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        if (!bgpOptional.isPresent()) {
            return;
        }
        final Global g = bgpOptional.get()
                .getGlobal();
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);

        blockingDeleteAndRead(cli, id,
                f("router bgp %s %s %s", g.getConfig()
                        .getAs()
                        .getValue(), instName, nwInsName),
                f("neighbor %s", new String(id.firstKeyOf(Neighbor.class)
                        .getNeighborAddress()
                        .getValue())),
                f("no address-family %s", GlobalAfiSafiReader.transformAfiToString(config.getAfiSafiName())),
                "root");
    }

    private String getReconfigurationCommand(Config config, boolean delete) {

        if (config.getAugmentation(BgpNeAfAug.class) != null
                &&
                config.getAugmentation(BgpNeAfAug.class)
                        .getSoftReconfiguration() != null) {
            StringBuilder command = new StringBuilder();
            if (delete) {
                command.append("no ");
            }
            command.append("soft-reconfiguration inbound");
            if (config.getAugmentation(BgpNeAfAug.class)
                    .getSoftReconfiguration()
                    .isAlways()) {
                command.append(" always");
            }
            return command.toString();
        }
        return "";
    }
}