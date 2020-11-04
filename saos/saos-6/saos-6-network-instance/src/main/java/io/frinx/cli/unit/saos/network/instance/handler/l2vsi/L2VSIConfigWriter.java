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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.SaosVsExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;



public class L2VSIConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "{% if ($vc) %}virtual-switch ethernet create vs {$vsi_ni_name}"
            + " vc {$vsi_cp_name}\n{% endif %}"
            + "configuration save\n"
            + "{% if ($description) %}virtual-switch ethernet set vs {$vsi_ni_name}"
            + " description {$vsi_ni_description}\n{% endif %}"
            + "virtual-switch ethernet set vs {$vsi_ni_name} encap-cos-policy {$vsi_ni_encap_cos_policy}\n"
            + "virtual-switch ethernet set vs {$vsi_ni_name} encap-fixed-dot1dpri {$vsi_ni_encap_fixed_dot1dpri}\n"
            + "{% if ($l2pt == TRUE) %}l2-cft tagged-pvst-l2pt enable vs {$vsi_ni_name}\n"
            + "{% elseIf ($l2pt == FALSE) %}l2-cft tagged-pvst-l2pt disable vs {$vsi_ni_name}\n"
            + "{% endif %}"
            + "configuration save";

    private static final String DELETE_TEMPLATE =  "virtual-switch ethernet delete vs {$vsi_ni_name}\n"
            + "configuration save";


    private Cli cli;

    public L2VSIConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2VSIReader.basicCheck_L2VSI.canProcess(iid, writeContext, false)) {
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
    void writeCurrentAttributesTesting(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config data,
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
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2VSIReader.basicCheck_L2VSI.canProcess(iid, writeContext, false)) {
            return false;
        }

        checkWriteNIData(iid, dataAfter, writeContext);
        writeCurrentAttributesTesting(iid, dataAfter, "", false);

        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2VSIReader.basicCheck_L2VSI.canProcess(iid, writeContext, true)) {
            return false;
        }
        deleteCurrentAttributesTesting(iid);

        return true;
    }

    void deleteCurrentAttributesTesting(@Nonnull InstanceIdentifier<Config> iid) throws WriteFailedException {
        blockingDeleteAndRead(fT(DELETE_TEMPLATE, "vsi_ni_name",
                iid.firstKeyOf(NetworkInstance.class).getName()), cli, iid);
    }
}
