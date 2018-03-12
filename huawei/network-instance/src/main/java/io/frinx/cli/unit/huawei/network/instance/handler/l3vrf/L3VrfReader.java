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

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfReader implements CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>,
        CompositeListReader.Child<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    private final Cli cli;

    private static final String DISPLAY_CONF_VRF = "display current-configuration | include ^ip vpn-instance| include";
    private static final Pattern VRF_CONFIGURATION_LINE = Pattern.compile("ip vpn-instance (?<vrfName>\\S+).*");

    public L3VrfReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<NetworkInstanceKey> getAllIds(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {

        return getAllIds(cli, this, instanceIdentifier, readContext);
    }

    static List<NetworkInstanceKey> getAllIds(Cli cli, CliReader cliReader,
                                              @Nonnull InstanceIdentifier<?> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {
        InstanceIdentifier<?> id = !instanceIdentifier.getTargetType().equals(NetworkInstance.class)
                ? RWUtils.cutId(instanceIdentifier, NetworkInstance.class)
                : instanceIdentifier;

        return parseVrfIds(cliReader.blockingRead(DISPLAY_CONF_VRF, cli, id, readContext));
    }

    private static List<NetworkInstanceKey> parseVrfIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                VRF_CONFIGURATION_LINE::matcher,
                matcher -> matcher.group("vrfName"),
                NetworkInstanceKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                      @Nonnull NetworkInstanceBuilder networkInstanceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        networkInstanceBuilder.setName(name);
    }
}
