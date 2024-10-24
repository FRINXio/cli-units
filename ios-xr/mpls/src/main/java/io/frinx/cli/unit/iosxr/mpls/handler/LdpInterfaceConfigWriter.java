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

package io.frinx.cli.unit.iosxr.mpls.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension.rev180822.NiMplsLdpGlobalAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.ldp.global.Ldp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.ldp.global.ldp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LdpInterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public LdpInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data,
            @NotNull WriteContext writeContext) throws WriteFailedException {
        final Ldp ldp = writeContext.readAfter(RWUtils.cutId(id,Ldp.class))
                .get();
        Preconditions.checkArgument(ldp != null,
                "Invalid value, mpls-ldp needs to be enabled.");
        Global global = ldp.getGlobal();
        Preconditions.checkArgument(global != null && global.getClass() != null,
                "Invalid value, mpls-ldp needs to be enabled.");
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp.global.Config
            config = global.getConfig();
        Preconditions.checkArgument(config != null && config.getAugmentation(NiMplsLdpGlobalAug.class) != null,
                "Invalid value, mpls-ldp needs to be enabled.");
        Boolean enabled = config.getAugmentation(NiMplsLdpGlobalAug.class).isEnabled();
        Preconditions.checkArgument(enabled != null && enabled,
                "Invalid value, mpls-ldp needs to be enabled.");
        final String name = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingWriteAndRead(cli, id, data,
            "mpls ldp",
            f("interface %s", name),
            "root");
    }

    @Override
    public void updateCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                        @NotNull final Config dataBefore,
                                        @NotNull final Config dataAfter,
                                        @NotNull final WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }


    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data,
            @NotNull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingDeleteAndRead(cli, id,
            "mpls ldp",
            f("no interface %s", name),
            "root");
    }
}