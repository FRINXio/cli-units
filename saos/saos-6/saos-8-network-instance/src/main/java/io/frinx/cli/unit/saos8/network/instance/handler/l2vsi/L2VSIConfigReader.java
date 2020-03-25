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
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2VSIConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    public static final String SH_VIRTUAL_SWITCH_TEMPLATE = "configuration search string \"set vs\"";

    private Cli cli;

    public L2VSIConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isVSI(instanceIdentifier, readContext)) {
            String vsName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
            String output = blockingRead(SH_VIRTUAL_SWITCH_TEMPLATE, cli, instanceIdentifier, readContext);
            parseL2VSIConfig(output, configBuilder, vsName);
        }
    }

    @VisibleForTesting
    static void parseL2VSIConfig(String output, ConfigBuilder builder, String vsName) {
        Pattern desc = Pattern.compile("virtual-switch set vs " + vsName + " description (?<desc>(\\S+\\s*)+).*");
        builder.setName(vsName);
        builder.setType(L2VSI.class);
        ParsingUtils.parseField(output, 0,
            desc::matcher,
            m -> m.group("desc"),
            builder::setDescription);
    }

    private boolean isVSI(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return L2VSIReader.getAllIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(NetworkInstance.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}