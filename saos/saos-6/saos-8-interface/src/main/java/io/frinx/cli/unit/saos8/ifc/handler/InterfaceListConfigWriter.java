/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.l2vlan.L2VLANInterfaceConfigWriter;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortConfigWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;

public class InterfaceListConfigWriter extends CompositeWriter<Config> {

    public InterfaceListConfigWriter(Cli cli) {
        super(Lists.newArrayList(
                new PortConfigWriter(cli),
                new L2VLANInterfaceConfigWriter(cli)
        ));
    }
}