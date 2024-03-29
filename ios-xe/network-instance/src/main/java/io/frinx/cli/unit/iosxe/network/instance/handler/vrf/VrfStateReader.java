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

package io.frinx.cli.unit.iosxe.network.instance.handler.vrf;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfStateReader implements CliOperReader<State, StateBuilder>,
        CompositeReader.Child<State, StateBuilder> {

    private Cli cli;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    public VrfStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        // FIXME Set config attributes from operational state e.g. use sh ip vrf here instead of sh run vrf
        Optional<Config> cfg = readContext.read(RWUtils.cutId(instanceIdentifier, IIDs.NE_NETWORKINSTANCE)
                .child(Config.class));
        if (cfg.isPresent()) {
            stateBuilder.fieldsFrom(cfg.get());
        }
        // TODO set state attributes
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}