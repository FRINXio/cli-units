/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf.table;

import io.fd.honeycomb.translate.spi.read.ListReaderCustomizer;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.table.BgpTableConnectionReader;
import io.frinx.cli.ospf.handler.table.OspfTableConnectionReader;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.TableConnectionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnection;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TableConnectionReader
        extends CompositeListReader<TableConnection, TableConnectionKey, TableConnectionBuilder>
        implements CliConfigListReader<TableConnection, TableConnectionKey, TableConnectionBuilder> {

    public TableConnectionReader(Cli cli) {
        super(new ArrayList<ListReaderCustomizer<TableConnection, TableConnectionKey, TableConnectionBuilder>>() {{
            add(new BgpTableConnectionReader(cli));
            add(new OspfTableConnectionReader(cli));
        }});
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<TableConnection> list) {
        ((TableConnectionsBuilder) builder).setTableConnection(list);
    }

    @Nonnull
    @Override
    public TableConnectionBuilder getBuilder(@Nonnull InstanceIdentifier<TableConnection> instanceIdentifier) {
        return new TableConnectionBuilder();
    }
}
