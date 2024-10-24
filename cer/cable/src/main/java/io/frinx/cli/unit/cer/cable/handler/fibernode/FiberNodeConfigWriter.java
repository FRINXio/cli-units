/*
 * Copyright © 2023 Frinx and others.
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
package io.frinx.cli.unit.cer.cable.handler.fibernode;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.FiberNodeConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.fiber.node.config.extension.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.fiber.node.top.fiber.nodes.fiber.node.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FiberNodeConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            configure
            cable fiber-node {$config.id}
            init
            {% if($cable_upstream) %}cable-upstream {$cable_upstream}
            {% endif %}{% if($cable_downstream) %}cable-downstream {$cable_downstream}
            {% endif %}{% if($rpd) %}rpd {$rpd.name} ds-conn {$rpd.ds_conn} us-conn {$rpd.us_conn}
            {% endif %}end""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String UPDATE_TEMPLATE = """
            configure
            cable fiber-node {$before.id}
            {% if($cable_upstream) %}{% if($before_cable_upstream) %}no cable-upstream {$before_cable_upstream}
            {% endif %}cable-upstream {$cable_upstream}
            {% else %}{% if($before_cable_upstream) %}no cable-upstream {$before_cable_upstream}
            {% endif %}{% endif %}{% if($cable_downstream) %}{% if($before_cable_upstream) %}no cable-downstream {$before_cable_downstream}
            {% endif %}cable-downstream {$cable_downstream}
            {% else %}{% if($cable_upstream) %}no cable-downstream {$before_cable_downstream}
            {% endif %}{% endif %}{% if($rpd) %}{% if($before_rpd.name) %}no rpd {$before_rpd.name}
            {% endif %}rpd {$rpd.name}
            {% else %}{% if($before_rpd.name) %}no rpd {$before_rpd.name}
            {% endif %}{% endif %}end""";

    private static final String DELETE_TEMPLATE = """
            configure
            cable fiber-node {$config.id}
            {% if($cable_upstream) %}no cable-upstream {$cable_upstream}
            {% endif %}{% if($cable_downstream) %}no cable-downstream {$cable_downstream}
            {% endif %}{% if($rpd.name) %}no rpd {$rpd.name}
            {% endif %}end""";

    private final Cli cli;

    public FiberNodeConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String cableUpstream = getCableUpstream(config);
        String cableDownstream = getCableDownstream(config);
        Rpd rpd = getRpd(config);
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "config", config,
                        "cable_upstream", cableUpstream,
                        "cable_downstream", cableDownstream,
                        "rpd", rpd));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String beforeCableUpstream = getCableUpstream(dataBefore);
        String beforeCableDownstream = getCableDownstream(dataBefore);
        Rpd beforeRpd = getRpd(dataBefore);
        String cableUpstream = getCableUpstream(dataAfter);
        String cableDownstream = getCableDownstream(dataAfter);
        Rpd rpd = getRpd(dataAfter);
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE,
                        "before", dataBefore,
                        "config", dataAfter,
                        "cable_upstream", cableUpstream,
                        "cable_downstream", cableDownstream,
                        "rpd", rpd,
                        "before_cable_upstream", beforeCableUpstream,
                        "before_cable_downstream", beforeCableDownstream,
                        "before_rpd", beforeRpd));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String cableUpstream = getCableUpstream(config);
        String cableDownstream = getCableDownstream(config);
        Rpd rpd = getRpd(config);
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(DELETE_TEMPLATE,
                        "config", config,
                        "cable_upstream", cableUpstream,
                        "cable_downstream", cableDownstream,
                        "rpd", rpd));
    }

    private static String getCableUpstream(Config config) {
        FiberNodeConfigAug fiberNodeConfigAug = config.getAugmentation(FiberNodeConfigAug.class);
        if (fiberNodeConfigAug != null && fiberNodeConfigAug.getCableUpstream() != null) {
            return fiberNodeConfigAug.getCableUpstream();
        }

        return null;
    }

    private static String getCableDownstream(Config config) {
        FiberNodeConfigAug fiberNodeConfigAug = config.getAugmentation(FiberNodeConfigAug.class);
        if (fiberNodeConfigAug != null && fiberNodeConfigAug.getCableDownstream() != null) {
            return fiberNodeConfigAug.getCableDownstream();
        }

        return null;
    }

    private static Rpd getRpd(Config config) {
        FiberNodeConfigAug fiberNodeConfigAug = config.getAugmentation(FiberNodeConfigAug.class);
        if (fiberNodeConfigAug != null && fiberNodeConfigAug.getRpd() != null) {
            return fiberNodeConfigAug.getRpd();
        }

        return null;
    }
}