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

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.mpls.MplsWriter;
import io.frinx.cli.io.Cli;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.global.config.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.Mpls;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.mpls.top.mpls.TeInterfaceAttributes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TeConfigWriter implements MplsWriter<Config> {

    private static final String MPLS_COMMAND = "mpls traffic-eng\n"
                                                + "root";
    private static final String NO_MPLS_COMMAND = "no mpls traffic-eng";

    private Cli cli;

    public TeConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (config.isEnabled()) {
            blockingWriteAndRead(cli, instanceIdentifier, config, MPLS_COMMAND);
        }
    }

    @Override
    public void updateCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore, @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (dataAfter.isEnabled()) {
            writeCurrentAttributesForType(id, dataAfter, writeContext);
        } else {
            deleteCurrentAttributesForType(id, dataBefore, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                               @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        final TeInterfaceAttributes ifaces = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Mpls.class))
                .get().getTeInterfaceAttributes();
        // sometimes the interface list remains empty in the data, this shouldn't happen
        Preconditions.checkArgument(ifaces == null || ifaces.getInterface() == null || ifaces.getInterface().isEmpty(),
                "Invalid request, interfaces cannot be present when mpls is disabled.");
        blockingDeleteAndRead(cli, instanceIdentifier, NO_MPLS_COMMAND);
    }
}
