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

package io.frinx.cli.unit.ios.network.instance.handler.vrf.table;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.bgp.handler.table.BgpTableConnectionReader;
import io.frinx.cli.unit.ospf.handler.table.OspfTableConnectionReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnection;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TableConnectionReader
        extends CompositeListReader<TableConnection, TableConnectionKey, TableConnectionBuilder>
        implements CliConfigListReader<TableConnection, TableConnectionKey, TableConnectionBuilder> {

    public TableConnectionReader(Cli cli) {
        super(List.of(
            new BgpTableConnectionReader(cli),
            new OspfTableConnectionReader(cli)));
    }

    @NotNull
    @Override
    public TableConnectionBuilder getBuilder(@NotNull InstanceIdentifier<TableConnection> instanceIdentifier) {
        return new TableConnectionBuilder();
    }
}