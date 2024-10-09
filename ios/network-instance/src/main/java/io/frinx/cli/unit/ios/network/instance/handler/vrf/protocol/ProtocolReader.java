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

package io.frinx.cli.unit.ios.network.instance.handler.vrf.protocol;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.ios.local.routing.StaticLocalRoutingProtocolReader;
import io.frinx.cli.unit.ospf.handler.OspfProtocolReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;

public class ProtocolReader extends CompositeListReader<Protocol, ProtocolKey, ProtocolBuilder>
        implements CliConfigListReader<Protocol, ProtocolKey, ProtocolBuilder> {

    public ProtocolReader(Cli cli) {
        super(List.of(
            new OspfProtocolReader(cli),
            new BgpProtocolReader(cli),
            new StaticLocalRoutingProtocolReader()
        ));
    }
}
