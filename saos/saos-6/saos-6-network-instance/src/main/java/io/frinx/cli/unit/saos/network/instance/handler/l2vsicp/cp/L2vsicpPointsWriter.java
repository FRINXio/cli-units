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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.cp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.L2vsicpReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.InstanceConnectionPointConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2vsicpPointsWriter implements CompositeWriter.Child<ConnectionPoints>, CliWriter<ConnectionPoints> {

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> iid,
                                                 @Nonnull ConnectionPoints data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, false)) {
            return false;
        }

        checkData(data, iid.firstKeyOf(NetworkInstance.class).getName(),
                writeContext.readAfter(iid.firstIdentifierOf(NetworkInstance.class)));
        return true;
    }

    @VisibleForTesting
    static void checkData(@Nonnull ConnectionPoints data,
                          String name, com.google.common.base.Optional<NetworkInstance> niOptional) {
        boolean niIsPresentAndPointIsNot = (data.getConnectionPoint() == null
                || data.getConnectionPoint().isEmpty())
                && niOptional.isPresent();
        Preconditions.checkArgument(!niIsPresentAndPointIsNot,
                String.format("Cannot delete connection point from network instance: '%s'", name));

        String cpKey = Optional.ofNullable(data.getConnectionPoint())
                .orElse(Collections.emptyList())
                .stream()
                .map(ConnectionPoint::getConnectionPointId)
                .findFirst()
                .orElse(null);
        Preconditions.checkNotNull(cpKey, "Missing connection point id to create virtual circuit");

        String cpId = Optional.ofNullable(data.getConnectionPoint())
                .orElse(Collections.emptyList())
                .stream()
                .map(ConnectionPoint::getConfig)
                .map(InstanceConnectionPointConfig::getConnectionPointId)
                .findFirst()
                .orElse(null);
        Preconditions.checkNotNull(cpId, "Missing connection point id to create virtual circuit");

        String niName = niOptional.get().getName();
        Preconditions.checkArgument(Objects.equals(cpId, cpKey) && Objects.equals(cpId, niName),
                "Connection point Id and network instance name must be the same");
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> iid,
                                                  @Nonnull ConnectionPoints dataBefore,
                                                  @Nonnull ConnectionPoints dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        return writeCurrentAttributesWResult(iid, dataAfter, writeContext);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> iid,
                                                  @Nonnull ConnectionPoints dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, true)) {
            return false;
        }

        String name = iid.firstKeyOf(NetworkInstance.class).getName();
        Preconditions.checkArgument(!writeContext.readAfter(iid.firstIdentifierOf(NetworkInstance.class)).isPresent(),
                f("Cannot delete connection point from network instance: '%s'", name));
        return true;
    }
}
