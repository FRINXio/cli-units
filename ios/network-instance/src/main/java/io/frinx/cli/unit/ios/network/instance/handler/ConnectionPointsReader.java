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

import io.fd.honeycomb.translate.spi.read.ReaderCustomizer;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2vsi.cp.L2VSIConnectionPointsReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPointsBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class ConnectionPointsReader extends CompositeReader<ConnectionPoints, ConnectionPointsBuilder>
        implements CliConfigReader<ConnectionPoints, ConnectionPointsBuilder> {

    public ConnectionPointsReader(Cli cli) {
        super(new ArrayList<ReaderCustomizer<ConnectionPoints, ConnectionPointsBuilder>>() {{
                add(new L2P2PConnectionPointsReader(cli));
                add(new L2VSIConnectionPointsReader(cli));
            }
        });
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull ConnectionPoints readValue) {
        ((NetworkInstanceBuilder) parentBuilder).setConnectionPoints(readValue);
    }
}
