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
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
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
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        if (isVSI(instanceIdentifier, readContext)) {
            String vsId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
            String output = blockingRead(L2VSIReader.SH_VIRTUAL_SWITCH_TEMPLATE, cli, instanceIdentifier, readContext);

            parseConfig(output, configBuilder, vsId);
        }
    }

    @VisibleForTesting
    void parseConfig(String output, ConfigBuilder builder, String vsId) {
        final VsSaosAugBuilder vsAug = new VsSaosAugBuilder();

        builder.setName(vsId);
        builder.setType(L2VSI.class);
        builder.setEnabled(true);
        getDescForVS(output, builder, vsId);
        getDot1dpri(output, vsAug, vsId);
        getL2pt(output, vsAug, vsId);
        vsAug.setEncapCosPolicy(SaosVsExtension.EncapCosPolicy.Fixed);

        builder.addAugmentation(VsSaosAug.class, vsAug.build());
    }

    private void getDescForVS(String output, ConfigBuilder configBuilder, String vsId) {
        String descriptionCommnadTemplate = "virtual-switch ethernet create vs " + vsId
                + " (encap-fixed-dot1dpri (\\d+) )?vc (\\S+) description ";
        Pattern portDesc = Pattern.compile(descriptionCommnadTemplate + "(?<desc>\\S+).*");
        if (output.contains("\"")) {
            portDesc = Pattern.compile(descriptionCommnadTemplate + "\"(?<desc>\\S+.*)\".*");
        }
        ParsingUtils.parseField(output, 0,
            portDesc::matcher,
            m -> m.group("desc"),
            configBuilder::setDescription);
    }

    private void getDot1dpri(String output, VsSaosAugBuilder vsAug, String vsId) {
        // default value
        vsAug.setEncapFixedDot1dpri((short) 2);

        Pattern pattern = Pattern.compile("virtual-switch ethernet create vs "
                + vsId + " encap-fixed-dot1dpri (?<dot1>\\d+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            m -> m.group("dot1"),
            s -> vsAug.setEncapFixedDot1dpri(Short.parseShort(s)));
    }

    private void getL2pt(String output, VsSaosAugBuilder vsAug, String vsId) {
        Pattern pattern = Pattern.compile("l2-cft tagged-pvst-l2pt enable vs " + vsId);

        ParsingUtils.parseField(output,
            pattern::matcher,
            matcher -> true,
            enable -> vsAug.setTaggedPvstL2pt(true));
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