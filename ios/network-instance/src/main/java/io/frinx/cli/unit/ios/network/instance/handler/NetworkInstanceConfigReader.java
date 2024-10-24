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

package io.frinx.cli.unit.ios.network.instance.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.handlers.def.DefaultConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2vsi.L2VSIConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.L3VrfConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.List;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;

public final class NetworkInstanceConfigReader extends CompositeReader<Config, ConfigBuilder>
        implements CliConfigReader<Config, ConfigBuilder> {

    public NetworkInstanceConfigReader(Cli cli) {
        super(List.of(
            new L3VrfConfigReader(cli),
            new DefaultConfigReader(),
            new L2P2PConfigReader(cli),
            new L2VSIConfigReader(cli)
        ));
    }
}
