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
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.L2VSIReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPointKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIPointsReader
        implements CompositeListReader.Child<ConnectionPoint, ConnectionPointKey, ConnectionPointBuilder>,
        CliConfigListReader<ConnectionPoint, ConnectionPointKey, ConnectionPointBuilder> {

    private final Cli cli;

    public L2VSIPointsReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ConnectionPointKey> getAllIds(@Nonnull InstanceIdentifier<ConnectionPoint> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {
        String vsId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String output = blockingRead(L2VSIReader.SH_VIRTUAL_SWITCH_TEMPLATE, cli, instanceIdentifier, readContext);

        return getAllIds(output, vsId);
    }

    @VisibleForTesting
    static List<ConnectionPointKey> getAllIds(String output, String vsId) {
        Pattern pattern = Pattern.compile("virtual-switch ethernet create vs "
                + vsId + ".* vc (?<vcId>\\S+).*");

        return ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            matcher -> matcher.group("vcId"),
            ConnectionPointKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ConnectionPoint> instanceIdentifier,
                                      @Nonnull ConnectionPointBuilder connectionPointBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        connectionPointBuilder.setConnectionPointId(instanceIdentifier.firstKeyOf(ConnectionPoint.class)
                .getConnectionPointId());
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}