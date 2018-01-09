/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf;

import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String DISPLAY_VRF_CFG = "display current-configuration configuration vpn-instance %s";
    private static final Pattern DESC_CONFIG = Pattern.compile("description (?<desc>.*)");
    private static final Pattern RD_CONFIG = Pattern.compile("route-distinguisher (?<rd>\\S+)");
    private Cli cli;

    public L3VrfConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isVrf(instanceIdentifier, readContext)) {
            String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
            configBuilder.setName(name);
            configBuilder.setType(L3VRF.class);

            parseVrfConfig(blockingRead(String.format(DISPLAY_VRF_CFG, name),
                    cli, instanceIdentifier, readContext), configBuilder, name);
        }
    }

    private void parseVrfConfig(String output, ConfigBuilder builder, String vrf) {
        builder.setName(vrf);
        parseField(output,
                RD_CONFIG::matcher,
                matcher -> matcher.group("rd"),
                rd -> builder.setRouteDistinguisher(new RouteDistinguisher(rd)));

        parseField(output,
                DESC_CONFIG::matcher,
                matcher -> matcher.group("desc"),
                builder::setDescription);
    }

    private boolean isVrf(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return L3VrfReader.getAllIds(cli, this, id, readContext).contains(id.firstKeyOf(NetworkInstance.class));
    }
}
