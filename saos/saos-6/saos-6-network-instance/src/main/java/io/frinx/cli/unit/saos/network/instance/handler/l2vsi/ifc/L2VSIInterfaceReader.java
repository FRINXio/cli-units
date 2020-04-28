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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.L2VSIReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIInterfaceReader implements CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder>,
        CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SH_VIRTUAL_SWITCH_TEMPLATE = "configuration search string virtual-switch";

    private final Cli cli;

    public L2VSIInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    public static List<InterfaceKey> getAllIds(Cli cli, CliReader cliReader, String vsId,
                                        @Nonnull InstanceIdentifier<?> id,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {

        String output = cliReader.blockingRead(SH_VIRTUAL_SWITCH_TEMPLATE, cli, id, readContext);

        Pattern pattern = Pattern.compile("virtual-switch ethernet (remove|add) vs " + vsId + " port (?<id>\\d+).*");
        return ParsingUtils.parseFields(output, 0,
            pattern::matcher,
            m -> m.group("id"),
            InterfaceKey::new);
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        if (instanceIdentifier.firstKeyOf(NetworkInstance.class).equals(NetworInstance.DEFAULT_NETWORK)) {
            return Collections.emptyList();
        }
        String vsId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        return getAllIds(cli, this, vsId, instanceIdentifier, readContext);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder,
                                      @Nonnull ReadContext readContext) {
        String vsId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        interfaceBuilder.setId(vsId);
    }

    @Override
    public Check getCheck() {
        return L2VSIReader.basicCheck_L2VSI;
    }
}
