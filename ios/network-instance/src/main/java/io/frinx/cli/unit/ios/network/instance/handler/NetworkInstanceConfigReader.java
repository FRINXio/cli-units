/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler;

import io.fd.honeycomb.translate.spi.read.ReaderCustomizer;
import io.frinx.cli.handlers.def.DefaultConfigReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2vsi.L2VSIConfigReader;
import io.frinx.cli.unit.ios.network.instance.handler.vrf.VrfConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NetworkInstanceConfigReader extends CompositeReader<Config, ConfigBuilder>
    implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public NetworkInstanceConfigReader(Cli cli) {
        super(new ArrayList<ReaderCustomizer<Config, ConfigBuilder>>() {{
            add(new VrfConfigReader(cli));
            add(new DefaultConfigReader());
            add(new L2P2PConfigReader(cli));
            add(new L2VSIConfigReader(cli));
        }});
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((NetworkInstanceBuilder) builder).setConfig(config);
    }
}
