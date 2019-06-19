/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.timers.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.timers.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceTimersConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    @VisibleForTesting
    static final String SHOW_OSPF_INT =
            "show configuration protocols ospf area %s interface %s retransmit-interval | display set";
    private static final Pattern RETRANSMIT_LINE = Pattern.compile(
            "set.* protocols ospf area \\S+ interface \\S+ "
            + "retransmit-interval (?<retransmission>.+)");
    private final Cli cli;

    public AreaInterfaceTimersConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull
            ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final String interfaceId = instanceIdentifier.firstKeyOf(Interface.class)
                .getId();
        final String areaId = AreaInterfaceReader.areaIdToString(instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier());

        String output = blockingRead(String.format(SHOW_OSPF_INT, areaId, interfaceId), cli,
                instanceIdentifier, readContext);
        parseRetransmit(output, configBuilder);
    }

    private void parseRetransmit(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output,
            RETRANSMIT_LINE::matcher,
            matcher -> matcher.group("retransmission"),
            value -> configBuilder.setRetransmissionInterval(Long.valueOf(value)));
    }
}
