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

package io.frinx.cli.unit.ni.base.handler.l2p2p;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.handlers.NetUtils;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2P2P;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractL2P2ConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private final Cli cli;
    private AbstractL2P2PReader parentReader;

    protected AbstractL2P2ConfigReader(AbstractL2P2PReader parentReader, Cli cli) {
        this.parentReader = parentReader;
        this.cli = cli;
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        if (parentReader.isP2P(id, ctx)) {
            String vllName = id.firstKeyOf(NetworkInstance.class).getName();
            configBuilder.setType(L2P2P.class);
            parseL2p2(blockingRead(getReadCommand(vllName), cli, id, ctx), configBuilder, vllName);
        }
    }

    protected abstract String getReadCommand(String vllName);

    protected void parseL2p2(String output, ConfigBuilder builder, String neName) {
        builder.setName(neName);

        ParsingUtils.parseField(output,
            getMtuLine()::matcher,
            matcher -> Integer.valueOf(matcher.group("mtu")),
            builder::setMtu);
    }

    protected Pattern getMtuLine() {
        return NetUtils.NO_MATCH;
    }
}
