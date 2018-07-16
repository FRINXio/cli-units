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

package io.frinx.cli.unit.iosxr.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.phys.holdtime.top.HoldTimeBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.phys.holdtime.top.hold.time.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.phys.holdtime.top.hold.time.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HoldTimeConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public HoldTimeConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        parseHoldTime(blockingRead(String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx),
                builder);
    }

    private static final Pattern CARRIER_DELAY_LINE =
            Pattern.compile("carrier-delay up (?<up>\\d+) down (?<down>\\d+)");

    @VisibleForTesting
    static void parseHoldTime(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output, 0,
                CARRIER_DELAY_LINE::matcher,
            matcher -> Long.valueOf(matcher.group("up")),
                builder::setUp);

        ParsingUtils.parseField(output, 0,
                CARRIER_DELAY_LINE::matcher,
            matcher -> Long.valueOf(matcher.group("down")),
                builder::setDown);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((HoldTimeBuilder) parentBuilder).setConfig(readValue);
    }
}
