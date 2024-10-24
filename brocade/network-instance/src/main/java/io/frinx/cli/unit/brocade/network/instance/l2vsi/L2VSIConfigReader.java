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

package io.frinx.cli.unit.brocade.network.instance.l2vsi;

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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2VSIConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final Pattern MTU_LINE = Pattern.compile("\\s*vpls-mtu (?<mtu>.+)");
    private static final String SH_VPLS = "show running-config | begin vpls %s";
    private final Cli cli;

    public L2VSIConfigReader(Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    void parseL2Vsi(String output, ConfigBuilder builder) {
        int endIndex = output.indexOf("\n\n");
        ParsingUtils.parseField(endIndex == -1 ? output : output.substring(0, endIndex),
            getMtuLine()::matcher,
            matcher -> Integer.valueOf(matcher.group("mtu")),
            builder::setMtu);
    }

    protected Pattern getMtuLine() {
        return MTU_LINE;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        if (isVSI(id, ctx)) {
            String vplsName = id.firstKeyOf(NetworkInstance.class).getName();
            configBuilder.setName(vplsName);
            configBuilder.setType(L2VSI.class);
            parseL2Vsi(blockingRead(getReadCommand(vplsName), cli, id, ctx), configBuilder);
            // TODO set other attributes i.e. description
        }
    }

    private String getReadCommand(String vplsName) {
        return f(SH_VPLS, vplsName);
    }

    private boolean isVSI(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return L2VSIReader.getAllIds(id, readContext, cli, this).contains(id.firstKeyOf(NetworkInstance.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}