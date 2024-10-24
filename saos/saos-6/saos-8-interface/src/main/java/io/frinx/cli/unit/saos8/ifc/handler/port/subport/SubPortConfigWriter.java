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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosSubIfConfigAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortConfigWriter implements CliWriter<Config> {

    private static final String WRITE_SUBPORT = """
            sub-port create sub-port {$data.name} parent-port {$parentPort} classifier-precedence {$data.index}
            {% if ($ingress) %}sub-port set sub-port {$data.name} ingress-l2-transform {$ingress}
            {% endif %}{% if ($egress) %}sub-port set sub-port {$data.name} egress-l2-transform {$egress}
            {% endif %}""";

    private static final String UPDATE_SUBPORT = """
            {% if ($nameDiff == TRUE) %}sub-port set sub-port {$before.name} name {$data.name}
            {% endif %}{% if ($ingressDiff == TRUE) %}sub-port set sub-port {$data.name} ingress-l2-transform {$ingress}
            {% elseIf ($ingressDiff == FALSE) %}sub-port unset sub-port {$data.name} ingress-l2-transform
            {% endif %}{% if ($egressDiff == TRUE) %}sub-port set sub-port {$data.name} egress-l2-transform {$egress}
            {% elseIf ($egressDiff == FALSE) %}sub-port unset sub-port {$data.name} egress-l2-transform
            {% endif %}""";

    private static final String DELETE_SUBPORT = "sub-port delete sub-port {$data.name}";

    private Cli cli;

    public SubPortConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ETHERNET_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, parentPort));
        }
    }

    @VisibleForTesting
    String writeTemplate(Config config, String parentPort) {
        var augmentation = config.getAugmentation(SaosSubIfConfigAug.class);
        return fT(WRITE_SUBPORT, "data", config,
                "parentPort", parentPort,
                "ingress", augmentation != null ? augmentation.getIngressL2Transform() : null,
                "egress", augmentation != null ? augmentation.getEgressL2Transform() : null);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(id, writeContext, false)) {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
        }
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter) {
        return fT(UPDATE_SUBPORT, "data", dataAfter, "before", dataBefore,
                "nameDiff", !dataAfter.getName().equals(dataBefore.getName()),
                "ingressDiff", updateIngressTransform(dataBefore, dataAfter),
                "ingress", setIngressTransform(dataAfter),
                "egressDiff", updateEgressTransform(dataBefore, dataAfter),
                "egress", setEgressTransform(dataAfter));
    }

    private String updateIngressTransform(Config dataBefore, Config dataAfter) {
        String transformBefore = setIngressTransform(dataBefore);
        String transformAfter = setIngressTransform(dataAfter);
        if (!Objects.equal(transformAfter, transformBefore)) {
            return transformAfter == null ? "FALSE" : Chunk.TRUE;
        }
        return null;
    }

    private String setIngressTransform(Config config) {
        SaosSubIfConfigAug logicalAug = config.getAugmentation(SaosSubIfConfigAug.class);
        return logicalAug != null ? logicalAug.getIngressL2Transform() : null;
    }

    private String updateEgressTransform(Config dataBefore, Config dataAfter) {
        String transformBefore = setEgressTransform(dataBefore);
        String transformAfter = setEgressTransform(dataAfter);
        if (!Objects.equal(transformAfter, transformBefore)) {
            return transformAfter == null ? "FALSE" : Chunk.TRUE;
        }
        return null;
    }

    private String setEgressTransform(Config config) {
        SaosSubIfConfigAug logicalAug = config.getAugmentation(SaosSubIfConfigAug.class);
        return logicalAug != null ? logicalAug.getEgressL2Transform() : null;
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.LAG_CHECK.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ETHERNET_CHECK.canProcess(instanceIdentifier, writeContext, false)) {
            blockingDelete(deleteTemplate(config), cli, instanceIdentifier);
        }
    }

    @VisibleForTesting
    String deleteTemplate(Config config) {
        return fT(DELETE_SUBPORT, "data", config);
    }
}