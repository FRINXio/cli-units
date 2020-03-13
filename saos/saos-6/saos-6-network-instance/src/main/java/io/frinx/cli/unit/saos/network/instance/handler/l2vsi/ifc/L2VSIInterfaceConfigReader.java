/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.L2VSIReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.L2CftIfExt;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.L2CftIfExtBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft._if.extension.InterfaceCft;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft._if.extension.InterfaceCftBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String SH_IF_CFT = "configuration search running-config string \"l2-cft\"";

    private Cli cli;

    public L2VSIInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Interface.class).getId();
        String output = blockingRead(SH_IF_CFT, cli, instanceIdentifier, readContext);
        configBuilder.setId(id);
        configBuilder.setInterface(id);
        parseCftAttributes(output, id, configBuilder);
    }

    @VisibleForTesting
    static InterfaceCft parseCftAttributes(String output, String portId, ConfigBuilder configBuilder) {
        Pattern profilePattern = Pattern.compile("l2-cft set port " + portId + " profile (?<profile>\\S+)");
        Pattern enablePattern = Pattern.compile("(?<enable>l2-cft enable port " + portId + ")?");

        InterfaceCftBuilder interfaceCftBuilder = new InterfaceCftBuilder();
        interfaceCftBuilder.setEnabled(false);

        ParsingUtils.parseFields(output, 0,
            enablePattern::matcher,
            m -> m.group("enable"),
            s -> interfaceCftBuilder.setEnabled(true));

        ParsingUtils.parseFields(output, 0,
            profilePattern::matcher,
            m -> m.group("profile"),
            interfaceCftBuilder::setProfile);

        L2CftIfExtBuilder l2CftIfExtBuilder = new L2CftIfExtBuilder();
        l2CftIfExtBuilder.setInterfaceCft(interfaceCftBuilder.build());
        configBuilder.addAugmentation(L2CftIfExt.class, l2CftIfExtBuilder.build());

        return interfaceCftBuilder.build();
    }

    @Override
    public Check getCheck() {
        return L2VSIReader.basicCheck_L2VSI;
    }
}
