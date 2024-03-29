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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.NiMplsTeEnabledCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.Mpls;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeGlobalAttributes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TeInterfaceConfigWriter  implements CliWriter<Config> {

    private Cli cli;

    public TeInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data, @NotNull
            WriteContext writeContext) throws WriteFailedException {
        final Mpls mpls = writeContext.readAfter(RWUtils.cutId(id, Mpls.class))
                .get();
        Preconditions.checkArgument(mpls.getTeGlobalAttributes() != null,
                "Invalid value, mpls-te needs to be enabled.");
        TeGlobalAttributes attrs = mpls.getTeGlobalAttributes();
        Preconditions.checkArgument(attrs.getAugmentation(NiMplsTeEnabledCiscoAug.class) != null,
                "Invalid value, mpls-te needs to be enabled.");
        NiMplsTeEnabledCiscoAug aug = attrs.getAugmentation(NiMplsTeEnabledCiscoAug.class);
        Preconditions.checkArgument(aug.getConfig() != null,
                "Invalid value, mpls-te needs to be enabled.");
        Boolean enabled = aug.getConfig().isEnabled();
        Preconditions.checkArgument(enabled != null && enabled,
                "Invalid value, mpls-te needs to be enabled.");
        final String name = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();
        blockingWriteAndRead(cli, id, data,
                "mpls traffic-eng",
                f("interface %s", name),
                "root");
    }

    @Override
    public void updateCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                        @NotNull final Config dataBefore,
                                        @NotNull final Config dataAfter,
                                        @NotNull final WriteContext writeContext) {
        // NOOP
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data, @NotNull
            WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();
        blockingWriteAndRead(cli, id, data,
                "mpls traffic-eng",
                f("no interface %s", name),
                "root");
    }
}