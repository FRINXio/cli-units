/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.protocol;

import io.fd.honeycomb.translate.spi.read.ReaderCustomizer;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.ospf.handler.OspfProtocolConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.ArrayList;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.ConfigBuilder;

public class ProtocolConfigReader extends CompositeReader<Config, ConfigBuilder>
    implements CliConfigReader<Config, ConfigBuilder> {

    public ProtocolConfigReader(Cli cli) {
        super(new ArrayList<ReaderCustomizer<Config, ConfigBuilder>>() {{
                add(new OspfProtocolConfigReader(cli));
            }
        });
    }
}
