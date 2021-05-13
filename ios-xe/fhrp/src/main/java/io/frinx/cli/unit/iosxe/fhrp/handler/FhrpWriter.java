/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.fhrp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.fhrp.Version;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FhrpWriter implements CliWriter<Version> {

    private static final String WRITE_TEMPLATE_VLAN = "configure terminal\n"
            + "fhrp version vrrp {$data.vrrp.name}\n"
            + "exit\n"
            + "end";

    private static final String UPDATE_TEMPLATE_VLAN = "configure terminal\n"
            + "no fhrp version vrrp {$dataBefore.vrrp.name}\n"
            + "fhrp version vrrp {$dataAfter.vrrp.name}\n"
            + "exit\n"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no fhrp version vrrp {$data.vrrp.name}\n"
            + "end";

    private final Cli cli;

    public FhrpWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Version> instanceIdentifier,
                                       @Nonnull Version data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, data,
                fT(WRITE_TEMPLATE_VLAN,"data", data));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Version> instanceIdentifier,
                                        @Nonnull Version dataBefore,
                                        @Nonnull Version dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE_VLAN,"dataBefore", dataBefore, "dataAfter", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Version> instanceIdentifier,
                                        @Nonnull Version data,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, data,
                fT(DELETE_TEMPLATE, "data", data));

    }
}
