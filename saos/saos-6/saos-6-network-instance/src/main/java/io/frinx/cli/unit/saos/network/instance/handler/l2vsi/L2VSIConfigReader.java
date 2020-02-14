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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.SaosVsExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos.extension.rev200210.VsSaosAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2VSIConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private Cli cli;

    public L2VSIConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isVSI(instanceIdentifier, readContext)) {
            String vsId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
            configBuilder.setName(vsId);
            configBuilder.setType(L2VSI.class);
            configBuilder.setEnabled(true);
            getAdditionalAttributes(instanceIdentifier, configBuilder, readContext, vsId);
        }
    }

    private void getAdditionalAttributes(InstanceIdentifier<Config> instanceIdentifier,
                                         ConfigBuilder configBuilder,
                                         ReadContext readContext,
                                         String vsId) throws ReadFailedException {
        getDescForVS(cli, this, vsId, instanceIdentifier, readContext, configBuilder);

        VsSaosAugBuilder vsAug = new VsSaosAugBuilder();
        // default value
        vsAug.setEncapCosPolicy(SaosVsExtension.EncapCosPolicy.Fixed);
        getDot1dpri(cli, this, vsId, instanceIdentifier, readContext, vsAug);
        configBuilder.addAugmentation(VsSaosAug.class, vsAug.build());
    }

    @VisibleForTesting
    public static void getDescForVS(Cli cli, CliReader cliReader, String vsId,
                                       @Nonnull InstanceIdentifier<?> id,
                                       @Nonnull ReadContext readContext,
                                                ConfigBuilder configBuilder) throws ReadFailedException {
        String output = cliReader.blockingRead(L2VSIReader.SH_VIRTUAL_SWITCH_TEMPLATE, cli, id, readContext);

        Pattern pattern = Pattern.compile("virtual-switch ethernet create vs "
                + vsId + " (encap-fixed-dot1dpri (\\d+) )?vc (\\S+) description (?<desc>\\S+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            m -> m.group("desc"),
            configBuilder::setDescription);
    }

    @VisibleForTesting
    public static void getDot1dpri(Cli cli, CliReader cliReader, String vsId,
                                 @Nonnull InstanceIdentifier<?> id,
                                 @Nonnull ReadContext readContext,
                                   VsSaosAugBuilder vsAug) throws ReadFailedException {
        // default value
        vsAug.setEncapFixedDot1dpri((short) 2);

        String output = cliReader.blockingRead(L2VSIReader.SH_VIRTUAL_SWITCH_TEMPLATE, cli, id, readContext);
        Pattern pattern = Pattern.compile("virtual-switch ethernet create vs "
                + vsId + " encap-fixed-dot1dpri (?<dot1>\\d+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            m -> m.group("dot1"),
            s -> vsAug.setEncapFixedDot1dpri(Short.parseShort(s)));
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