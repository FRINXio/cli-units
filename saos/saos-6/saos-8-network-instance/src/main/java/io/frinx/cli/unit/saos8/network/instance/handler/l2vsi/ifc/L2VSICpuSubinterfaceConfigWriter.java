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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ifc;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;

import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAug;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSICpuSubinterfaceConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_CPUSUBINTERFACE =
            "virtual-switch interface attach cpu-subinterface {$data.id} vs {$vsName}\nconfiguration save";

    private static final String DELETE_CPUSUBINTERFACE =
            "virtual-switch interface detach cpu-subinterface {$data.id}\nconfiguration save";

    private Cli cli;

    public L2VSICpuSubinterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                 @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkNotNull(data.getAugmentation(Saos8NiIfcAug.class),
                "Interface type is not set");

        if (!data.getAugmentation(Saos8NiIfcAug.class).getType().equals(L2vlan.class)) {
            return false;
        }
        blockingWriteAndRead(fT(WRITE_CPUSUBINTERFACE, "data", data,
                "vsName", iid.firstKeyOf(NetworkInstance.class).getName()), cli, iid, data);
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        throw new WriteFailedException.UpdateFailedException(iid, dataBefore, dataAfter,
                new IllegalArgumentException("Cpu-subinterface cannot be updated"));
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!dataBefore.getAugmentation(Saos8NiIfcAug.class).getType().equals(L2vlan.class)) {
            return false;
        }
        blockingDeleteAndRead(fT(DELETE_CPUSUBINTERFACE, "data", dataBefore), cli, iid);
        return true;
    }
}


