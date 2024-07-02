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
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractL2P2PReader
        implements CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>,
        CompositeListReader.Child<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    private Cli cli;

    protected AbstractL2P2PReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    @NotNull
    @Override
    public List<NetworkInstanceKey> getAllIds(@NotNull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                              @NotNull ReadContext readContext) throws ReadFailedException {
        return getAllIds(instanceIdentifier, readContext, this);
    }

    public List<NetworkInstanceKey> getAllIds(@NotNull InstanceIdentifier<?> instanceIdentifier,
                                                 @NotNull ReadContext ctx,
                                                 @NotNull CliReader reader) throws ReadFailedException {
        if (ctx.getModificationCache().containsKey(getClass().toString() + "_l2Ids")) {
            return ((List<NetworkInstanceKey>) ctx.getModificationCache().get(getClass() + "_l2Ids"));
        }

        if (!instanceIdentifier.getTargetType().equals(NetworkInstance.class)) {
            instanceIdentifier = RWUtils.cutId(instanceIdentifier, NetworkInstance.class);
        }

        // Parse local-remote l2p2p
        List<NetworkInstanceKey> l2Ids = parseLocalRemote(reader.blockingRead(getReadLocalRemoteCommand(),
                cli, instanceIdentifier, ctx));
        // Parse local-local l2p2p
        l2Ids.addAll(parseLocalLocal(reader.blockingRead(getReadLocalLocalCommand(), cli, instanceIdentifier, ctx)));
        ctx.getModificationCache().put(getClass() + "_l2Ids", l2Ids);
        return l2Ids;
    }

    protected abstract String getReadLocalRemoteCommand();

    protected abstract String getReadLocalLocalCommand();

    protected List<NetworkInstanceKey> parseLocalRemote(String output) {
        return ParsingUtils.parseFields(output, 0,
            getLocalRemoteLine()::matcher,
            matcher -> matcher.group("network"),
            NetworkInstanceKey::new);
    }

    protected List<NetworkInstanceKey> parseLocalLocal(String output) {
        return ParsingUtils.parseFields(output, 0,
            getLocalLocalLine()::matcher,
            matcher -> matcher.group("network"),
            NetworkInstanceKey::new);
    }

    protected abstract Pattern getLocalLocalLine();

    protected abstract Pattern getLocalRemoteLine();

    public boolean isP2P(InstanceIdentifier<?> id, ReadContext readContext) throws ReadFailedException {
        return getAllIds(id, readContext, this).contains(id.firstKeyOf(NetworkInstance.class));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                      @NotNull NetworkInstanceBuilder networkInstanceBuilder,
                                      @NotNull ReadContext readContext) {
        networkInstanceBuilder.setName(instanceIdentifier.firstKeyOf(NetworkInstance.class).getName());
    }
}