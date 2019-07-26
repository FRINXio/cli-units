/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.Util;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmConfigWriter implements CliWriter<Config> {
    private final Cli cli;

    private static final String CREATE_TEMPLATE = "interface {$name}\n"
        + "ethernet cfm\n"
        + "root";
    private static final String DELETE_TEMPLATE = "interface {$name}\n"
        + "no ethernet cfm\n"
        + "root";

    public CfmConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataAfter,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = Util.getSubinterfaceName(id);
        checkIfcType(name);

        if (BooleanUtils.isTrue(dataAfter.isEnabled())) {
            blockingWriteAndRead(cli, id, dataAfter, fT(CREATE_TEMPLATE, "name", name));
        }
    }

    private void checkIfcType(String ifcName) {
        Preconditions.checkArgument(new AggregateConfigReader(cli).isLAGInterface(ifcName),
            "Cannot configure cfm on non-LAG interface %s", ifcName);
    }

    @Override
    public void updateCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataBefore,
        @Nonnull Config dataAfter,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (BooleanUtils.isTrue(dataAfter.isEnabled())) {
            writeCurrentAttributes(id, dataAfter, writeContext);
        } else {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataBefore,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = Util.getSubinterfaceName(id);
        if (BooleanUtils.isTrue(dataBefore.isEnabled())) {
            blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "name", name));
        }
    }
}
