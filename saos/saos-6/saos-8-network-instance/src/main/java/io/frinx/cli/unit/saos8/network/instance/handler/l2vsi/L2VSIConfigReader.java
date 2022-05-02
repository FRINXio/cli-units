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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2VSIConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String SH_VIRTUAL_SWITCH_TEMPLATE = "configuration search string \"virtual-switch set vs %s\"";

    private Cli cli;

    public L2VSIConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        var instance = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        if (instance.equals(NetworInstance.DEFAULT_NETWORK)) {
            return;
        }
        parseL2VSIConfig(blockingRead(String.format(SH_VIRTUAL_SWITCH_TEMPLATE, instance.getName()),
                cli, instanceIdentifier, readContext), configBuilder, instance.getName());
    }

    @VisibleForTesting
    static void parseL2VSIConfig(String output, ConfigBuilder builder, String vsName) {
        builder.setName(vsName);
        builder.setType(L2VSI.class);
        getDescForVS(output, builder, vsName);
    }

    static void getDescForVS(String output, ConfigBuilder configBuilder, String vsId) {
        Pattern portDescLine = Pattern.compile("virtual-switch set vs " + vsId + " description (?<desc>\\S+.*)");
        Optional<String> line = ParsingUtils.parseField(output, 0,
            portDescLine::matcher,
            matcher -> matcher.group("desc"));
        if (line.isPresent()) {
            Pattern portDesc = Pattern.compile("(?<desc>\\S+).*");
            if (line.get().startsWith("\"")) {
                portDesc = Pattern.compile("\"(?<desc>\\S+.*)\".*");
            }
            ParsingUtils.parseField(line.get(), 0,
                portDesc::matcher,
                m -> m.group("desc"),
                configBuilder::setDescription);
        }
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
