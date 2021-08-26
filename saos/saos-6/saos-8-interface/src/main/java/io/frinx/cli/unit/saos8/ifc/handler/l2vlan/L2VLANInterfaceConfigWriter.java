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

package io.frinx.cli.unit.saos8.ifc.handler.l2vlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VLANInterfaceConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
            "cpu-interface sub-interface create cpu-subinterface {$name}\n"
                    + "configuration save";
    private static final String DELETE_TEMPLATE =
            "cpu-interface sub-interface delete cpu-subinterface {$name}\n"
                    + "configuration save";

    private final Cli cli;

    public L2VLANInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                 @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (L2vlan.class.equals(data.getType())) {
            blockingWriteAndRead(cli, iid, data, fT(WRITE_TEMPLATE, "name",
                    data.getName().replace("cpu_subintf_", "")));
            return true;
        }

        return false;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (L2vlan.class.equals(dataBefore.getType())) {
            throw new WriteFailedException.UpdateFailedException(iid, dataBefore, dataAfter,
                    new IllegalArgumentException("Updating interface is not permitted"));
        }
        return false;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (L2vlan.class.equals(dataBefore.getType())) {
            blockingDeleteAndRead(cli, iid, fT(DELETE_TEMPLATE, "name",
                    dataBefore.getName().replace("cpu_subintf_", "")));
            return true;
        }
        return false;
    }
}