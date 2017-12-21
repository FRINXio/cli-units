/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import java.util.regex.Pattern;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String SH_IP_VRF_CFG = "sh ip vrf %s";
    private static final Pattern VRF_ID_LINE = Pattern.compile(" *(?<vrf>[\\S]+) *(?<rd>([\\S]+):([\\S]+)).*");
    private Cli cli;

    public VrfConfigReader(Cli cli) {
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

            // TODO set other attributes i.e. description
            parseVrfConfig(blockingRead(String.format(SH_IP_VRF_CFG, name), cli, instanceIdentifier, readContext), configBuilder, name);
        }
    }

    private void parseVrfConfig(String output, ConfigBuilder builder, String vrf) {
        builder.setName(vrf);
        parseField(output,
            VRF_ID_LINE::matcher,
            matcher -> matcher.group("rd"),
            rd -> builder.setRouteDistinguisher(new RouteDistinguisher(rd)));
    }

    private boolean isVrf(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return VrfReader.getAllIds(id, readContext, cli, this).contains(id.firstKeyOf(NetworkInstance.class));
    }
}
