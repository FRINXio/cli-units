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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.AbstractMap;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2VSIReader implements
        CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>,
        CompositeListReader.Child<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    private static final String SH_VIRTUAL_SWITCH_IDS = "configuration search string \"virtual-switch create vs\"";

    private static final Pattern VIRTUAL_SWITCH_LINE_PATTERN =
            Pattern.compile("virtual-switch create vs (?<vs>\\S+).*");

    private Cli cli;

    public L2VSIReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<NetworkInstanceKey> getAllIds(@NotNull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                              @NotNull ReadContext readContext) throws ReadFailedException {
        return checkCachedIds(cli, this, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    public static List<NetworkInstanceKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            VIRTUAL_SWITCH_LINE_PATTERN::matcher,
            m -> m.group("vs"),
            NetworkInstanceKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                      @NotNull NetworkInstanceBuilder networkInstanceBuilder,
                                      @NotNull ReadContext readContext) {
        networkInstanceBuilder.setName(instanceIdentifier.firstKeyOf(NetworkInstance.class).getName());
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    private static List<NetworkInstanceKey> checkCachedIds(Cli cli, CliReader reader,
                                                           @NotNull InstanceIdentifier id,
                                                           @NotNull ReadContext context) throws ReadFailedException {

        if (context.getModificationCache()
                .get(new AbstractMap.SimpleEntry<>(L2VSIReader.class, reader)) != null) {
            return (List<NetworkInstanceKey>) context.getModificationCache()
                    .get(new AbstractMap.SimpleEntry<>(L2VSIReader.class, reader));
        }
        String output = reader.blockingRead(SH_VIRTUAL_SWITCH_IDS, cli, id, context);
        context.getModificationCache().put(
                new AbstractMap.SimpleEntry<>(L2VSIReader.class, reader), getAllIds(output));
        return getAllIds(output);
    }
}