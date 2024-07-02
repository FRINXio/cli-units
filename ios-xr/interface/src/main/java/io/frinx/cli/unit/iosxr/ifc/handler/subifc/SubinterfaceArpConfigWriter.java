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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.iosxr.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724._if.ethernet.extentions.group.arp.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceArpConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String CREATE_TEMPLATE = """
            interface {$name}
            arp timeout {$config.cache_timeout}
            root""";
    private static final String DELETE_TEMPLATE = """
            interface {$name}
            no arp timeout
            root""";

    public SubinterfaceArpConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        @NotNull InstanceIdentifier<Config> id,
        @NotNull Config dataAfter,
        @NotNull WriteContext writeContext) throws WriteFailedException {

        Long subId = id.firstKeyOf(Subinterface.class).getIndex();

        Preconditions.checkArgument(subId != AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID,
            "ARP configuration can not be set to ZERO subinterface.");

        if (dataAfter.getCacheTimeout() != null) {
            blockingWriteAndRead(cli, id, dataAfter,
                fT(CREATE_TEMPLATE,
                    "name", Util.getSubinterfaceName(id),
                    "config", dataAfter));
        }
    }

    @Override
    public void updateCurrentAttributes(
        @NotNull InstanceIdentifier<Config> id,
        @NotNull Config dataBefore,
        @NotNull Config dataAfter,
        @NotNull WriteContext writeContext) throws WriteFailedException {

        if (dataAfter.getCacheTimeout() != null) {
            writeCurrentAttributes(id, dataAfter, writeContext);
        } else {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(
        @NotNull InstanceIdentifier<Config> id,
        @NotNull Config dataBefore,
        @NotNull WriteContext writeContext) throws WriteFailedException {

        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, "name", Util.getSubinterfaceName(id)));
    }
}