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

package io.frinx.cli.unit.ospf.handler;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalStateReader implements CliOperReader<State, StateBuilder> {

    private Cli cli;

    public GlobalStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                             @Nonnull StateBuilder stateBuilder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        // FIXME Set config attributes from operational state e.g. use sh ip vrf here instead of sh run vrf
        Optional<Config> cfg = ctx.read(RWUtils.cutId(instanceIdentifier, IIDs.NE_NE_PR_PR_OS_GLOBAL)
                .child(Config.class));
        if (cfg.isPresent()) {
            stateBuilder.fieldsFrom(cfg.get());
        }
        // TODO set state attributes
    }

}
