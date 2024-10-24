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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.cp;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.L2VSIReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.InstanceConnectionPointConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIPointsWriter implements CompositeWriter.Child<ConnectionPoints>, CliWriter<ConnectionPoints> {


    public L2VSIPointsWriter() {
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> iid,
                                                 @NotNull ConnectionPoints data,
                                                 @NotNull WriteContext writeContext) {
        if (!L2VSIReader.BASIC_CHECK_L2VSI.canProcess(iid, writeContext, false)) {
            return false;
        }
        checkWriteData(iid, data, writeContext);

        return true;
    }

    private void checkWriteData(@NotNull InstanceIdentifier<ConnectionPoints> iid,
                                @NotNull ConnectionPoints data,
                                @NotNull WriteContext writeContext) {
        boolean vsIdPresentVcNot = (data.getConnectionPoint() == null || data.getConnectionPoint().isEmpty())
                && writeContext.readAfter(iid.firstIdentifierOf(NetworkInstance.class)).isPresent();
        Preconditions.checkArgument(!vsIdPresentVcNot, "Cannot handle only connection point");

        String cpId = Optional.ofNullable(data.getConnectionPoint())
                 .orElse(Collections.emptyList())
                 .stream()
                 .map(ConnectionPoint::getConnectionPointId)
                 .findFirst()
                 .orElse(null);
        Preconditions.checkNotNull(cpId, "Missing connection point id");
        String cpConfigId = Optional.ofNullable(data.getConnectionPoint())
                .orElse(Collections.emptyList())
                .stream()
                .map(ConnectionPoint::getConfig)
                .map(InstanceConnectionPointConfig::getConnectionPointId)
                .findFirst()
                .orElse(null);
        Preconditions.checkNotNull(cpConfigId, "Missing connection point id");

        Preconditions.checkArgument(Objects.equals(cpId, cpConfigId),
                "Connection point Id and Id from connection point config must be the same");
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> iid,
                                                  @NotNull ConnectionPoints dataBefore,
                                                  @NotNull ConnectionPoints dataAfter,
                                                  @NotNull WriteContext writeContext) {
        checkData(iid, dataAfter, writeContext);
        return true;
    }

    private void checkData(@NotNull InstanceIdentifier<ConnectionPoints> iid,
                           @NotNull ConnectionPoints data,
                           @NotNull WriteContext writeContext) {
        boolean vcIdPresentVsNot = (data.getConnectionPoint() == null || data.getConnectionPoint().isEmpty())
                && writeContext.readAfter(iid.firstIdentifierOf(NetworkInstance.class)).isPresent();
        Preconditions.checkArgument(vcIdPresentVsNot, "Cannot handle only connection point");

        String cpId = Optional.ofNullable(data.getConnectionPoint())
                .orElse(Collections.emptyList())
                .stream()
                .map(ConnectionPoint::getConnectionPointId)
                .findFirst()
                .orElse(null);
        Preconditions.checkNotNull(cpId, "Missing connection point id");
        String cpConfigId = Optional.ofNullable(data.getConnectionPoint())
                .orElse(Collections.emptyList())
                .stream()
                .map(ConnectionPoint::getConfig)
                .map(InstanceConnectionPointConfig::getConnectionPointId)
                .findFirst()
                .orElse(null);
        Preconditions.checkNotNull(cpConfigId, "Missing connection point id");

        Preconditions.checkArgument(Objects.equals(cpId, cpConfigId),
                "Connection point Id and Id from connection point config must be the same");
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> iid,
                                                  @NotNull ConnectionPoints dataBefore,
                                                  @NotNull WriteContext writeContext) {
        return L2VSIReader.BASIC_CHECK_L2VSI.canProcess(iid, writeContext, true);
    }
}