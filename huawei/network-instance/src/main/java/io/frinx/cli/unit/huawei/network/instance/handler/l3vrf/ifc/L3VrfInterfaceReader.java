/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf.ifc;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfListReader;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
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

    private final Cli cli;

    public L3VrfInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIdsForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                               @Nonnull ReadContext ctx) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        String output = blockingRead(DISPLAY_IFC_VRF_CONFIG, cli, instanceIdentifier, ctx)
                .replace("display current-configuration interface | include ^interface|^ ip binding vpn-instance", "");
        String realignedOutput = realignVrfInterfacesOutput(output);

        return NEWLINE.splitAsStream(realignedOutput)
                .map(String::trim)
                .filter(line -> line.contains(String.format("ip binding vpn-instance %s", name))
                        == !name.equals(NetworInstance.DEFAULT_NETWORK_NAME))
                .map(line -> line.split(" ")[0])
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
        String withoutNewlines = output.replaceAll(NEWLINE.pattern(), "");
        return withoutNewlines.replace("interface ", "\n");
    }
}
