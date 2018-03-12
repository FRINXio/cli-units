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

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf.ifc;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfListReader;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfInterfaceReader implements L3VrfListReader.L3VrfConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String DISPLAY_IFC_VRF_CONFIG =
            "display current-configuration interface | include ^interface|^ ip binding vpn-instance";
    private static final Pattern INTERFACE_ID_PATTERN = Pattern.compile("^interface (?<id>\\S+).*");


    private final Cli cli;

    public L3VrfInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIdsForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                               @Nonnull ReadContext ctx) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        String output = blockingRead(DISPLAY_IFC_VRF_CONFIG, cli, instanceIdentifier, ctx);

        return parseVrfInterfacesIds(output, name);
    }

    @VisibleForTesting
    static List<InterfaceKey> parseVrfInterfacesIds(String output, String vrfName) {
        String realignedOutput = realignVrfInterfacesOutput(output);

        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            return parseDefaultVrfInterfacesIds(realignedOutput);
        }

        return NEWLINE.splitAsStream(realignedOutput)
                .map(String::trim)
                .filter(line -> line.contains(String.format("ip binding vpn-instance %s", vrfName)))
                .map(INTERFACE_ID_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("id"))
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    private static List<InterfaceKey> parseDefaultVrfInterfacesIds(String realignedOutput) {
        return NEWLINE.splitAsStream(realignedOutput)
                .map(String::trim)
                .filter(line -> !line.contains("ip binding vpn-instance"))
                .map(INTERFACE_ID_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("id"))
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                             @Nonnull InterfaceBuilder interfaceBuilder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifaceId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        interfaceBuilder.setId(ifaceId);
    }

    private static String realignVrfInterfacesOutput(String output) {
        String withoutNewlines = output.replaceAll("[\r\n]", "");
        return withoutNewlines.replace("interface ", " \ninterface ");
    }
}
