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

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeWriter;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsWriter;
import io.frinx.cli.unit.ios.network.instance.handler.l2vsi.cp.L2VSIConnectionPointsWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;

public class ConnectionPointsWriter extends CompositeWriter<ConnectionPoints> {

    public ConnectionPointsWriter(Cli cli) {
        super(Lists.newArrayList(
                new L2P2PConnectionPointsWriter(cli),
                new L2VSIConnectionPointsWriter(cli)));
    }
}
