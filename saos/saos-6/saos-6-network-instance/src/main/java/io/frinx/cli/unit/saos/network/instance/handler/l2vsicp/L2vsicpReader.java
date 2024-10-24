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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2vsicpReader implements
        CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>,
        CompositeListReader.Child<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    public static final Check L2VSICP_CHECK = BasicCheck.checkData(
            ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2VSICP);

    public static final String SHOW_VC = "configuration search string \"virtual-circuit ethernet create vc\"";
    private static Pattern L2VSICP_IDS = Pattern
            .compile("virtual-circuit ethernet create vc (?<name>\\S+) vlan (\\S+).*");
    private final Cli cli;

    public L2vsicpReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<NetworkInstanceKey> getAllIds(@NotNull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                              @NotNull ReadContext readContext) throws ReadFailedException {
        return getAllIds(cli, this, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    static List<NetworkInstanceKey> getAllIds(Cli cli, CliReader cliReader,
                                              @NotNull InstanceIdentifier<?> id,
                                              @NotNull ReadContext readContext) throws ReadFailedException {
        String output = cliReader.blockingRead(SHOW_VC, cli, id, readContext);
        return ParsingUtils.parseFields(output, 0,
            L2VSICP_IDS::matcher,
            m -> m.group("name"),
            NetworkInstanceKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                      @NotNull NetworkInstanceBuilder networkInstanceBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        networkInstanceBuilder.setName(name);
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}