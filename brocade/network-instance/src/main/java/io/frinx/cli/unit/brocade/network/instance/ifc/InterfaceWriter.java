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

package io.frinx.cli.unit.brocade.network.instance.ifc;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.l2p2p.ifc.L2P2PInterfaceWriter;
import io.frinx.cli.unit.brocade.network.instance.vrf.ifc.VrfInterfaceWriter;
import io.frinx.cli.unit.utils.CliListWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;

public class InterfaceWriter extends CompositeWriter<Interface> implements CliListWriter<Interface, InterfaceKey> {

    public InterfaceWriter(Cli cli) {
        super(Lists.newArrayList(new VrfInterfaceWriter(cli), new L2P2PInterfaceWriter()));
    }
}
