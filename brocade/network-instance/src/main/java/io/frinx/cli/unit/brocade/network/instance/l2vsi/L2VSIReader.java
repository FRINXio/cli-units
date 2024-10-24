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
import io.fd.honeycomb.translate.util.RWUtils;
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

    public static final String SH_L2_VSI = "show running-config | include ^ vpls";
    private static final Pattern L2_VSI_LINE = Pattern.compile("vpls (?<network>\\S+)\\s+(?<vccid>\\S+).*");

    private Cli cli;

    public L2VSIReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<NetworkInstanceKey> getAllIds(@NotNull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                              @NotNull ReadContext readContext) throws ReadFailedException {
        return getAllIds(instanceIdentifier, readContext, this.cli, this);
    }

    public static List<NetworkInstanceKey> getAllIds(@NotNull InstanceIdentifier<?> instanceIdentifier,
                                                     @NotNull ReadContext readContext,
                                                     @NotNull Cli cli,
                                                     @NotNull CliReader reader) throws ReadFailedException {
        // Caching here to speed up reading
        if (readContext.getModificationCache().get(new AbstractMap.SimpleEntry<>(L2VSIReader.class, reader)) != null) {
            return (List<NetworkInstanceKey>) readContext.getModificationCache()
                    .get(new AbstractMap.SimpleEntry<>(L2VSIReader.class, reader));
        }

        if (!instanceIdentifier.getTargetType().equals(NetworkInstance.class)) {
            instanceIdentifier = RWUtils.cutId(instanceIdentifier, NetworkInstance.class);
        }

        List<NetworkInstanceKey> allIds =
                parseL2Vsis(reader.blockingRead(SH_L2_VSI, cli, instanceIdentifier, readContext));
        readContext.getModificationCache().put(new AbstractMap.SimpleEntry<>(L2VSIReader.class, reader), allIds);
        return allIds;
    }

    @VisibleForTesting
    static List<NetworkInstanceKey> parseL2Vsis(String output) {

        return ParsingUtils.parseFields(output, 0,
            L2_VSI_LINE::matcher,
            m -> m.group("network"),
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