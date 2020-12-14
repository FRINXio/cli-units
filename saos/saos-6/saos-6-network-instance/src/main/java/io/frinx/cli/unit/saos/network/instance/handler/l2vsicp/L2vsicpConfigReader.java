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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.saos.rev200211.L2VSICP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.virtual.circuit.saos.extension.rev201204.NiVcSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.virtual.circuit.saos.extension.rev201204.NiVcSaosAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2vsicpConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String PATTERN = "virtual-circuit ethernet create vc %s vlan (.*) statistics on";

    private final Cli cli;

    public L2vsicpConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String vsicpName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        configBuilder.setName(vsicpName);
        configBuilder.setType(L2VSICP.class);
        configBuilder.setEnabled(true);

        String output = blockingRead(L2vsicpReader.SHOW_VC, cli, instanceIdentifier, readContext);

        fillBuilder(configBuilder, output, vsicpName);
    }

    @VisibleForTesting
    static void fillBuilder(@Nonnull ConfigBuilder builder, String output, String name) {
        NiVcSaosAugBuilder statBuilder = new NiVcSaosAugBuilder();
        statBuilder.setStatistics(false);

        Pattern statisticsPattern = Pattern.compile(String.format(PATTERN, name));

        ParsingUtils.parseField(output,
            statisticsPattern::matcher,
            m -> true,
            statBuilder::setStatistics);

        builder.addAugmentation(NiVcSaosAug.class, statBuilder.build());
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}