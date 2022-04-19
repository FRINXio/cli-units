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
import com.google.common.base.Optional;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.ext.rev180926.Saos8SubIfNameAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortVlanConfigWriter implements CliWriter<Config> {

    private static final String WRITE_L2_TRANSFORMS =
            "{% if ($ingress) %}sub-port set sub-port {$subIfcName} ingress-l2-transform {$ingress}\n{% endif %}"
            + "{% if ($egress) %}sub-port set sub-port {$subIfcName} egress-l2-transform {$egress}\n{% endif %}";

    private static final String UPDATE_L2_TRANSFORMS =
            "{% if ($ingressDiff == TRUE) %}"
            + "sub-port set sub-port {$subIfcName} ingress-l2-transform {$ingress}\n"
            + "{% elseIf ($ingressDiff == FALSE) %}"
            + "sub-port unset sub-port {$subIfcName} ingress-l2-transform\n"
            + "{% endif %}"
            + "{% if ($egressDiff == TRUE) %}"
            + "sub-port set sub-port {$subIfcName} egress-l2-transform {$egress}\n"
            + "{% elseIf ($egressDiff == FALSE) %}"
            + "sub-port unset sub-port {$subIfcName} egress-l2-transform\n"
            + "{% endif %}";

    private static final String DELETE_L2_TRANSFORMS =
            "{% if ($ingress %}sub-port unset sub-port {$subIfcName} ingress-l2-transform\n{% endif %}"
            + "{% if ($egress %}sub-port unset sub-port {$subIfcName} egress-l2-transform\n{% endif %}";

    private Cli cli;

    public SubPortVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ethernetCheck.canProcess(instanceIdentifier, writeContext, false)) {
            Optional<Subinterface> subPort = writeContext
                    .readAfter(instanceIdentifier.firstIdentifierOf(Subinterface.class));

            if (subPort.isPresent()) {
                final String subPortName = subPort.get().getConfig()
                        .getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();

                blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, subPortName));
            } else {
                throw new IllegalStateException("Cannot read subinterface name");
            }
        }
    }

    @VisibleForTesting
    String writeTemplate(Config config, String subIfcName) {
        Saos8VlanLogicalAug logicalAug = config.getAugmentation(Saos8VlanLogicalAug.class);

        return fT(WRITE_L2_TRANSFORMS, "data", config,
            "subIfcName", subIfcName,
            "ingress", logicalAug.getIngressL2Transform(),
            "egress", logicalAug.getEgressL2Transform());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(id, writeContext, false)) {
            Optional<Subinterface> subPort = writeContext.readAfter(id.firstIdentifierOf(Subinterface.class));

            if (subPort.isPresent()) {
                final String subPortName = subPort.get().getConfig()
                        .getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();

                blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter, subPortName));
            } else {
                throw new IllegalStateException("Cannot read subinterface name");
            }
        }
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter, String subIfcName) {
        return fT(UPDATE_L2_TRANSFORMS, "data", dataAfter, "before", dataBefore,
                "subIfcName", subIfcName,
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
        Saos8VlanLogicalAug logicalAug = config.getAugmentation(Saos8VlanLogicalAug.class);
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
        Saos8VlanLogicalAug logicalAug = config.getAugmentation(Saos8VlanLogicalAug.class);
        return logicalAug != null ? logicalAug.getEgressL2Transform() : null;
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (PortReader.lagCheck.canProcess(instanceIdentifier, writeContext, false)
                || PortReader.ethernetCheck.canProcess(instanceIdentifier, writeContext, false)) {
            Optional<Subinterface> subPort = writeContext
                    .readBefore(instanceIdentifier.firstIdentifierOf(Subinterface.class));

            if (subPort.isPresent()) {
                final String subPortName = subPort.get().getConfig()
                        .getAugmentation(Saos8SubIfNameAug.class).getSubinterfaceName();

                blockingDelete(deleteTemplate(config, subPortName), cli, instanceIdentifier);
            } else {
                throw new IllegalStateException("Cannot read subinterface name");
            }
        }
    }

    @VisibleForTesting
    String deleteTemplate(Config config, String subIfcName) {
        Saos8VlanLogicalAug logicalAug = config.getAugmentation(Saos8VlanLogicalAug.class);

        return fT(DELETE_L2_TRANSFORMS, "data", config,
                "subIfcName", subIfcName,
                "ingress", logicalAug.getIngressL2Transform(),
                "egress", logicalAug.getEgressL2Transform());
    }
}
