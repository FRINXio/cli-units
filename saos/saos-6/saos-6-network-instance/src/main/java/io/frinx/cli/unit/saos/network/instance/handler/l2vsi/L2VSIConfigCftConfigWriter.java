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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.CtrlProtocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.cft.config.ctrl.protocols.CtrlProtocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigCftConfigWriter implements CliWriter<Config> {

    private static final String WRITE_PROTOCOL =
            "l2-cft protocol add profile {$data.cft_name} ctrl-protocol {$protocol} "
            + "untagged-disposition {$mode}\n";

    private static final String UPDATE_PROTOCOL =
            "l2-cft protocol set profile {$data.cft_name} ctrl-protocol {$protocol} "
            + "untagged-disposition {$mode}\n";

    private static final String REMOVE_PROTOCOL =
            "l2-cft protocol remove profile {$data.cft_name} ctrl-protocol {$protocol}\n";

    private Cli cli;

    public L2VSIConfigCftConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config));
    }

    @VisibleForTesting
    String writeTemplate(Config config) {
        StringBuilder commands = new StringBuilder();
        commands.append("l2-cft create profile " + config.getCftName() + "\n");
        CtrlProtocols ctrlProtocols = config.getCtrlProtocols();

        if (ctrlProtocols != null) {
            List<CtrlProtocol> ctrlProtocol = ctrlProtocols.getCtrlProtocol();
            for (CtrlProtocol protocol : ctrlProtocol) {
                commands.append(fT(WRITE_PROTOCOL, "data", config,
                        "protocol", protocol.getName().getName(),
                        "mode", protocol.getDisposition().getName()));
            }
        }
        return commands.toString();
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
    }

    @VisibleForTesting
    String updateTemplate(Config before, Config after) {
        StringBuilder commands = new StringBuilder();

        if (after.getCtrlProtocols() != null) {
            commands.append(updateProtocol(before, after));
        }
        if (before.getCtrlProtocols() != null) {
            commands.append(removeProtocol(before, after));
        }

        return commands.toString();
    }

    private String updateProtocol(Config before, Config after) {
        List<CtrlProtocol> protocolsBefore = before.getCtrlProtocols() != null
                ? before.getCtrlProtocols().getCtrlProtocol() : null;
        List<CtrlProtocol> protocolsAfter = after.getCtrlProtocols()
                .getCtrlProtocol();
        StringBuilder commands = new StringBuilder();

        for (CtrlProtocol protocolAfter : protocolsAfter) {
            CtrlProtocol protocolBefore = (protocolsBefore != null) ? protocolsBefore.stream()
                .filter(ctrlProtocol -> ctrlProtocol.getName().equals(protocolAfter.getName()))
                .findFirst().orElse(null) : null;

            if (protocolBefore == null) {
                commands.append(fT(WRITE_PROTOCOL, "data", after, "before", before,
                    "protocol", protocolAfter.getName().getName(),
                    "mode", protocolAfter.getDisposition().getName()));
            } else if (!protocolAfter.getDisposition().equals(protocolBefore.getDisposition())) {
                commands.append(fT(UPDATE_PROTOCOL, "data", after, "before", before,
                    "protocol", protocolAfter.getName().getName(),
                    "mode", protocolAfter.getDisposition().getName()));
            }
        }
        return commands.toString();
    }

    private String removeProtocol(Config before, Config after) {
        List<CtrlProtocol> protocolsBefore = before.getCtrlProtocols()
                .getCtrlProtocol();
        List<CtrlProtocol> protocolsAfter = after.getCtrlProtocols() != null
                ? after.getCtrlProtocols().getCtrlProtocol() : null;
        StringBuilder commands = new StringBuilder();

        for (CtrlProtocol protocolBefore : protocolsBefore) {
            CtrlProtocol protocolAfter = (protocolsAfter != null) ? protocolsAfter.stream()
                .filter(ctrlProtocol -> ctrlProtocol.getName().equals(protocolBefore.getName()))
                .findFirst().orElse(null) : null;

            if (protocolAfter == null) {
                commands.append(fT(REMOVE_PROTOCOL, "data", after, "before", before,
                        "protocol", protocolBefore.getName().getName()));
            }
        }
        return commands.toString();
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDelete(f("l2-cft delete profile %s\nconfiguration save\n",
                config.getCftName()), cli, instanceIdentifier);
    }
}
