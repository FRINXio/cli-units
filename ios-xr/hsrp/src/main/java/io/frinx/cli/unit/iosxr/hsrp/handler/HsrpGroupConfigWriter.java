/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.hsrp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.hsrp.handler.util.HsrpUtil;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.hsrp.group.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpGroupConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public HsrpGroupConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data,
            @NotNull WriteContext writeContext) throws WriteFailedException {
        InterfaceKey interfaceKey = id.firstKeyOf(Interface.class);
        HsrpGroupKey hsrpGroupKey = id.firstKeyOf(HsrpGroup.class);

        blockingWriteAndRead(cli, id, data,
                "router hsrp",
                f("interface %s", interfaceKey.getInterfaceId()),
                f("address-family %s", HsrpUtil.getStringType(hsrpGroupKey.getAddressFamily())),
                data.getVirtualRouterId() == null
                        ? "" : f("hsrp %s version %s", data.getVirtualRouterId(), data.getVersion()),
                data.getPriority() == null ? "" : f("priority %s", data.getPriority()),
                "root");
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        InterfaceKey interfaceKey = id.firstKeyOf(Interface.class);
        HsrpGroupKey hsrpGroupKey = id.firstKeyOf(HsrpGroup.class);

        blockingWriteAndRead(cli, id, dataAfter, "router hsrp", f("interface %s", interfaceKey.getInterfaceId()),
                f("address-family %s", HsrpUtil.getStringType(hsrpGroupKey.getAddressFamily())),
                dataAfter.getVirtualRouterId() == null
                        ? f("no hsrp %s version %s", dataBefore.getVirtualRouterId(), dataBefore.getVersion())
                        : f("hsrp %s version %s", dataAfter.getVirtualRouterId(), dataAfter.getVersion()),
                dataAfter.getPriority() == null
                        ? dataAfter.getVirtualRouterId() != null && dataBefore.getPriority() != null ? "no priority"
                                : ""
                        : f("priority %s", dataAfter.getPriority()),
                "root");
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        InterfaceKey interfaceKey = id.firstKeyOf(Interface.class);
        HsrpGroupKey hsrpGroupKey = id.firstKeyOf(HsrpGroup.class);

        blockingDeleteAndRead(cli, id,
                "router hsrp",
                f("interface %s", interfaceKey.getInterfaceId()),
                f("address-family %s", HsrpUtil.getStringType(hsrpGroupKey.getAddressFamily())),
                f("no hsrp %s version %s", data.getVirtualRouterId(), data.getVersion()),
                "root");
    }
}