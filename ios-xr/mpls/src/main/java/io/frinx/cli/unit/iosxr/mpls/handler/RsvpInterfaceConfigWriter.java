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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RsvpInterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public RsvpInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data, @NotNull
            WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();
        blockingWriteAndRead(cli, id, data,
                "rsvp",
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
                "rsvp",
                f("no interface %s", name),
                "root");
    }
}