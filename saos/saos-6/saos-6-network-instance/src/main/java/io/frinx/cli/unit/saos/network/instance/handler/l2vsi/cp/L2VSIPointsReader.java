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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.cp;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.L2VSIReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPointsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIPointsReader implements CompositeReader.Child<ConnectionPoints, ConnectionPointsBuilder>,
        CliConfigReader<ConnectionPoints, ConnectionPointsBuilder> {

    private final Cli cli;

    public L2VSIPointsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public Check getCheck() {
        return L2VSIReader.basicCheck_L2VSI;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ConnectionPoints> instanceIdentifier,
                                      @Nonnull ConnectionPointsBuilder connectionPointsBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String vsId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        ConnectionPointBuilder connectionPointBuilder = new ConnectionPointBuilder();
        getVcIdForVs(instanceIdentifier, readContext, vsId, connectionPointBuilder);
        connectionPointsBuilder.setConnectionPoint(Collections.singletonList(connectionPointBuilder.build()));
    }

    private void getVcIdForVs(InstanceIdentifier<ConnectionPoints> id,
                                ReadContext readContext,
                                String vsId,
                                ConnectionPointBuilder connectionPointBuilder)
            throws ReadFailedException {
        getVcIdForVs(cli, this, vsId, id, readContext, connectionPointBuilder);
    }

    @VisibleForTesting
    public static void getVcIdForVs(Cli cli, CliReader cliReader, String vsId,
                                      @Nonnull InstanceIdentifier<?> id,
                                      @Nonnull ReadContext readContext,
                                      ConnectionPointBuilder connectionPointBuilder) throws ReadFailedException {
        String output = cliReader.blockingRead(L2VSIReader.SH_VIRTUAL_SWITCH_TEMPLATE, cli, id, readContext);
        Pattern pattern = Pattern.compile("virtual-switch ethernet create vs "
                + vsId + " (encap-fixed-dot1dpri (\\d+) )?vc (?<vcId>\\S+).*");

        ConfigBuilder config = new ConfigBuilder();
        ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            m -> m.group("vcId"),
                config::setConnectionPointId);

        Config config1 = config.build();
        connectionPointBuilder.setConfig(config1);
        connectionPointBuilder.setConnectionPointId(config1.getConnectionPointId());
    }
}