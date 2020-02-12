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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.cp;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.L2vsicpReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPointsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2vsicpPointsReader implements CompositeReader.Child<ConnectionPoints, ConnectionPointsBuilder>,
        CliConfigReader<ConnectionPoints, ConnectionPointsBuilder> {

    private final Cli cli;

    public L2vsicpPointsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public Check getCheck() {
        return L2vsicpReader.L2VSICP_CHECK;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ConnectionPoints> instanceIdentifier,
                                      @Nonnull ConnectionPointsBuilder connectionPointsBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        connectionPointsBuilder.setConnectionPoint(Lists.newArrayList(getConnectionPoint(name)));
    }

    private ConnectionPoint getConnectionPoint(String name) {
        ConnectionPointBuilder cpBuilder = new ConnectionPointBuilder();
        cpBuilder.setConnectionPointId(name).setConfig(new ConfigBuilder().setConnectionPointId(name).build());
        return cpBuilder.build();
    }
}