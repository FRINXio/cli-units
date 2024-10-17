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
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.SaosVsExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            {% if ($vc) %}virtual-switch ethernet create vs {$vsi_ni_name} vc {$vsi_cp_name}
            {% endif %}{% if ($description) %}virtual-switch ethernet set vs {$vsi_ni_name} description "{$vsi_ni_description}"
            {% endif %}virtual-switch ethernet set vs {$vsi_ni_name} encap-cos-policy {$vsi_ni_encap_cos_policy}
            virtual-switch ethernet set vs {$vsi_ni_name} encap-fixed-dot1dpri {$vsi_ni_encap_fixed_dot1dpri}
            {% if ($l2pt == TRUE) %}l2-cft tagged-pvst-l2pt enable vs {$vsi_ni_name}
            {% elseIf ($l2pt == FALSE) %}l2-cft tagged-pvst-l2pt disable vs {$vsi_ni_name}
            {% endif %}""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String UPDATE_TEMPLATE = """
            {$data|update(description,virtual-switch ethernet set vs `$vsi_ni_name` description "`$data.description`"
            ,)}{% if ($l2pt) %}l2-cft tagged-pvst-l2pt {$l2pt} vs {$vsi_ni_name}
            {% endif %}{% if ($encapPolicy) %}virtual-switch ethernet set vs {$vsi_ni_name} encap-cos-policy {$encapPolicy}
            {% endif %}{% if ($encapFixed) %}virtual-switch ethernet set vs {$vsi_ni_name} encap-fixed-dot1dpri {$encapFixed}
            {% endif %}""";

    private static final String DELETE_TEMPLATE =  "virtual-switch ethernet delete vs {$vsi_ni_name}";


    private Cli cli;

    public L2VSIConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid, @NotNull Config data,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!L2VSIReader.BASIC_CHECK_L2VSI.canProcess(iid, writeContext, false)) {
            return false;
        }
        checkWriteNIData(iid, data, writeContext);

        Optional<NetworkInstance> networkInstance = writeContext
                .readAfter(iid.firstIdentifierOf(NetworkInstance.class));

        String connectionPointId = java.util.Optional.of(networkInstance.get())
                .map(NetworkInstance::getConnectionPoints)
                .map(ConnectionPoints::getConnectionPoint)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(ConnectionPoint::getConnectionPointId)
                .findFirst()
                .orElse(null);

        Preconditions.checkNotNull(connectionPointId, "Cannot delete only connection point");

        writeCurrentAttributesTesting(iid, data, connectionPointId, true);
        return true;
    }

    private String getWriteTemplate(String niName, String description, String encapCosPolicy,
                                    String encapFixedDot1dpri, String taggedPvstL2pt,
                                    String vcName, Boolean desc, Boolean vc) {
        if (vcName.isEmpty()) {
            return fT(WRITE_TEMPLATE, "vsi_ni_name", niName, "vsi_ni_description", description,
                    "vsi_ni_encap_cos_policy", encapCosPolicy, "vsi_ni_encap_fixed_dot1dpri", encapFixedDot1dpri,
                    "l2pt", taggedPvstL2pt, "description", desc ? Chunk.TRUE : null, "vc", vc ? Chunk.TRUE : null);
        }
        else {
            return fT(WRITE_TEMPLATE, "vsi_ni_name", niName, "vsi_ni_description", description,
                    "vsi_ni_encap_cos_policy", encapCosPolicy, "vsi_ni_encap_fixed_dot1dpri", encapFixedDot1dpri,
                    "l2pt", taggedPvstL2pt, "vsi_cp_name", vcName, "description", desc ? Chunk.TRUE : null, "vc",
                    vc ? Chunk.TRUE : null);
        }

    }

    private void checkWriteNIData(InstanceIdentifier<Config> instanceIdentifier,
                                  Config data,
                                  WriteContext writeContext) {

        Optional<NetworkInstance> networkInstance = writeContext.readAfter(instanceIdentifier
                .firstIdentifierOf(NetworkInstance.class));

        ConnectionPoint connectionPoint = java.util.Optional.of(networkInstance.get())
                .map(NetworkInstance::getConnectionPoints)
                .map(ConnectionPoints::getConnectionPoint)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        boolean vsIdPresentVcNot = !instanceIdentifier.firstKeyOf(NetworkInstance.class).getName().isEmpty()
                && connectionPoint != null;
        Preconditions.checkArgument(vsIdPresentVcNot,
                "Cannot create virtual-switch without virtual-circuit id");

        String vsId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        Preconditions.checkNotNull(vsId, "Missing virtual-switch id");

        String vsConfigId = data.getName();
        Preconditions.checkNotNull(vsConfigId, "Missing virtual-switch id as ID in config");

        Preconditions.checkArgument(Objects.equals(vsId, vsConfigId), "virtual-switch id as NI_ID "
                + "and virtual-switch config id must be the same");
    }

    @VisibleForTesting
    void writeCurrentAttributesTesting(@NotNull InstanceIdentifier<Config> iid, @NotNull Config data,
                                       String vcName, Boolean vc) throws WriteFailedException {

        String niName = iid.firstKeyOf(NetworkInstance.class).getName();
        String description = data.getDescription() != null ? data.getDescription() : "";
        String encapFixedDot1dpri = "2";
        String encapCosPolicy = SaosVsExtension.EncapCosPolicy.Fixed.getName();
        String taggedPvstL2pt = null;

        VsSaosAug vsSaosAug = data.getAugmentation(VsSaosAug.class);

        if (vsSaosAug != null) {
            if (vsSaosAug.getEncapFixedDot1dpri() != null) {
                encapFixedDot1dpri = String.valueOf(vsSaosAug.getEncapFixedDot1dpri());
            }
            if (vsSaosAug.getEncapCosPolicy() != null) {
                encapCosPolicy = vsSaosAug.getEncapCosPolicy().getName();
            }
            if (vsSaosAug.isTaggedPvstL2pt() != null) {
                taggedPvstL2pt = vsSaosAug.isTaggedPvstL2pt() ? Chunk.TRUE : "FALSE";
            }
        }

        if (description.isEmpty()) {
            blockingWriteAndRead(cli, iid, data, getWriteTemplate(niName, description,
                    encapCosPolicy, encapFixedDot1dpri, taggedPvstL2pt, vcName, false, vc));
        }
        else {
            blockingWriteAndRead(cli, iid, data, getWriteTemplate(niName, description,
                    encapCosPolicy, encapFixedDot1dpri, taggedPvstL2pt, vcName, true, vc));
        }

    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid, @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!L2VSIReader.BASIC_CHECK_L2VSI.canProcess(iid, writeContext, false)) {
            return false;
        }

        checkWriteNIData(iid, dataAfter, writeContext);
        String niName = iid.firstKeyOf(NetworkInstance.class).getName();
        blockingWriteAndRead(cli, iid, dataAfter, updateTemplate(dataBefore, dataAfter, niName));

        return true;
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter, String niName) {
        return fT(UPDATE_TEMPLATE, "data", dataAfter, "before", dataBefore,
                "vsi_ni_name", niName,
                "l2pt", updateL2pt(dataBefore, dataAfter),
                "encapPolicy", updateEncapCosPolicy(dataBefore, dataAfter),
                "encapFixed", updateEncapFixed(dataBefore, dataAfter));
    }

    private String updateL2pt(Config dataBefore, Config dataAfter) {
        Boolean l2ptBefore = setL2pt(dataBefore);
        Boolean l2ptAfter = setL2pt(dataAfter);
        if (!Objects.equals(l2ptAfter, l2ptBefore)) {
            if (l2ptAfter != null) {
                return l2ptAfter ? "enable" : "disable";
            }
        }
        return null;
    }

    @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
    private Boolean setL2pt(Config config) {
        VsSaosAug vsSaosAug = config.getAugmentation(VsSaosAug.class);
        return vsSaosAug != null ? vsSaosAug.isTaggedPvstL2pt() : null;
    }

    private String updateEncapCosPolicy(Config dataBefore, Config dataAfter) {
        String cosPolicyBefore = setEncapCosPolicy(dataBefore);
        String cosPolicyAfter = setEncapCosPolicy(dataAfter);
        if (!Objects.equals(cosPolicyAfter, cosPolicyBefore)) {
            if (cosPolicyAfter != null) {
                return cosPolicyAfter;
            }
        }
        return null;
    }

    private String setEncapCosPolicy(Config config) {
        VsSaosAug vsSaosAug = config.getAugmentation(VsSaosAug.class);
        if (vsSaosAug != null) {
            SaosVsExtension.EncapCosPolicy encapCosPolicy = vsSaosAug.getEncapCosPolicy();
            return encapCosPolicy != null ? encapCosPolicy.getName() : null;
        }
        return null;
    }

    private String updateEncapFixed(Config dataBefore, Config dataAfter) {
        String fixedBefore = setEncapFixed(dataBefore);
        String fixedAfter = setEncapFixed(dataAfter);
        if (!Objects.equals(fixedAfter, fixedBefore)) {
            if (fixedAfter != null) {
                return fixedAfter;
            }
        }
        return null;
    }

    private String setEncapFixed(Config config) {
        VsSaosAug vsSaosAug = config.getAugmentation(VsSaosAug.class);
        if (vsSaosAug != null) {
            Short fixedDot1dpri = vsSaosAug.getEncapFixedDot1dpri();
            return fixedDot1dpri != null ? fixedDot1dpri.toString() : null;
        }
        return null;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid, @NotNull Config dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!L2VSIReader.BASIC_CHECK_L2VSI.canProcess(iid, writeContext, true)) {
            return false;
        }
        deleteCurrentAttributesTesting(iid);

        return true;
    }

    void deleteCurrentAttributesTesting(@NotNull InstanceIdentifier<Config> iid) throws WriteFailedException {
        blockingDeleteAndRead(fT(DELETE_TEMPLATE, "vsi_ni_name",
                iid.firstKeyOf(NetworkInstance.class).getName()), cli, iid);
    }
}