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

package io.frinx.cli.unit.brocade.network.instance.vrf.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfInterfaceReader implements CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder>,
        CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SH_IP_VRF_INTERFACES_ALL = "show running-config | include ^interface|^ vrf "
            + "forwarding";
    private static final Pattern VRF_INTERFACE_ID_LINE = Pattern.compile("interface (?<id>\\S+ \\S+)\\s+vrf forwarding "
            + "(?<vrfId>\\S+)");
    private static final Pattern INTERFACE_ID_LINE = Pattern.compile("interface (?<id>\\S+ \\S+)");

    public static final Check VRF_CHECK = BasicCheck.checkData(
            ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_DEFAULTINSTANCE)
            .or(BasicCheck.checkData(
                    ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                    ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L3VRF));

    private final Cli cli;

    public VrfInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public Check getCheck() {
        return VRF_CHECK;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext ctx) throws ReadFailedException {
        if (ctx.getModificationCache().get(this) != null && ctx.getModificationCache().get(this).equals("SKIP")) {
            return Collections.emptyList();
        }

        final String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String output = blockingRead(SH_IP_VRF_INTERFACES_ALL, cli, instanceIdentifier, ctx);
        return parseIds(name, output);
    }

    @VisibleForTesting
    static List<InterfaceKey> parseIds(String vrfName, String output) {
        String noNewlines = ParsingUtils.NEWLINE.matcher(output)
                .replaceAll("");
        String ifcPerLine = noNewlines.replaceAll("interface", "\ninterface");

        if (vrfName.equals(NetworInstance.DEFAULT_NETWORK_NAME)) {
            return ParsingUtils.parseFields(ifcPerLine, 0, INTERFACE_ID_LINE::matcher, m -> m.group("id"),
                    InterfaceKey::new);
        } else {
            return ParsingUtils.parseFields(ifcPerLine, 0,
                    VRF_INTERFACE_ID_LINE::matcher,
                m -> new AbstractMap.SimpleEntry<>(m.group("id"), m.group("vrfId")),
                e -> new InterfaceKey(e.getKey()),
                e -> e.getValue()
                        .equals(vrfName));
        }
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder interfaceBuilder,
                                      @NotNull ReadContext ctx) {
        interfaceBuilder.setId(instanceIdentifier.firstKeyOf(Interface.class).getId());
    }
}