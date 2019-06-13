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

package io.frinx.cli.unit.iosxr.network.instance.handler.vrf.protocol;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.BgpProtocolWriter;
import io.frinx.cli.iosxr.ospf.handler.OspfProtocolWriter;
import io.frinx.cli.iosxr.ospfv3.handler.OspfV3ProtocolWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;

public class ProtocolConfigWriter extends CompositeWriter<Config> {

    public ProtocolConfigWriter(final Cli cli) {
        super(Lists.newArrayList(
                new BgpProtocolWriter(),
                new OspfProtocolWriter(cli),
                new OspfV3ProtocolWriter(cli)
        ));
    }
}
