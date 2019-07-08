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
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.extension.rev180822.NiMplsLdpGlobalAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.ldp.global.Ldp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top.InterfaceAttributes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NiMplsLdpGlobalAugWriter implements CliWriter<NiMplsLdpGlobalAug> {

    private Cli cli;

    public NiMplsLdpGlobalAugWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<NiMplsLdpGlobalAug> id,
            @Nonnull NiMplsLdpGlobalAug data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (data.isEnabled() == true) {
            blockingWriteAndRead(cli, id, data,
                        "mpls ldp",
                        "root");
        } else {
            deleteCurrentAttributes(id, data, writeContext);
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<NiMplsLdpGlobalAug> id,
                                        @Nonnull final NiMplsLdpGlobalAug dataBefore,
                                        @Nonnull final NiMplsLdpGlobalAug dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<NiMplsLdpGlobalAug> id, @Nonnull
            NiMplsLdpGlobalAug data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final InterfaceAttributes ifaces = writeContext.readAfter(RWUtils.cutId(id,Ldp.class))
                .get().getInterfaceAttributes();
        Preconditions.checkArgument(ifaces == null
                || ifaces.getInterfaces() == null
                || ifaces.getInterfaces().getInterface() == null
                || ifaces.getInterfaces().getInterface().size() == 0,
                "Invalid request, interfaces cannot be present when mpls-ldp is disabled.");
        blockingDeleteAndRead(cli, id,
                    "no mpls ldp");
    }
}
