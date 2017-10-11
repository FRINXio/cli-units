/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.ifc.InterfaceReader;
import io.frinx.cli.unit.utils.InitCliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NetworkInstanceInterfaceReader implements InitCliListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SH_IP_VRF_INTERFACES_ALL = "sh ip vrf interfaces";
    private static final String SH_IP_VRF_INTERFACES = "sh ip vrf interfaces %s";
    private static final String SH_IP_INTERFACES = "sh ip interface brief %s";
    private static final Pattern VRF_INTERFACE_ID_LINE = Pattern.compile("(?<id>[^\\s]+).*");
    private static final Pattern INTERFACE_ID_LINE = Pattern.compile("(?<id>[^\\s]+).*");

    private final Cli cli;
    private final InterfaceReader interfaceReader;


    public NetworkInstanceInterfaceReader(Cli cli) {
        this.cli = cli;
        this.interfaceReader = new InterfaceReader(cli);
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext ctx) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        if (name.equals(NetworInstance.DEFAULT_NETWORK_NAME)) {
            final List<InterfaceKey> interfaceKeys = interfaceReader.getAllIds(InstanceIdentifier
                            .create(org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222
                                    .interfaces.top.interfaces.Interface.class), ctx)
                    .stream()
                    .map((interfaceKey) -> new InterfaceKey(interfaceKey.getName()))
                    .collect(Collectors.toList());

            final List<InterfaceKey> networkInstanceKeys = convertInterfaceNames(SH_IP_VRF_INTERFACES_ALL,
                    instanceIdentifier,
                    ctx);

            interfaceKeys.removeAll(networkInstanceKeys);

            return interfaceKeys;
        } else {
            return convertInterfaceNames(String.format(SH_IP_VRF_INTERFACES, name), instanceIdentifier, ctx);
        }
    }

    private List<InterfaceKey> convertInterfaceNames(final String command,
                                                     final @Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                                     final @Nonnull ReadContext ctx) throws ReadFailedException {
        final String output = blockingRead(command, cli, instanceIdentifier, ctx);
        final List<InterfaceKey> expanded = new ArrayList<>();

        for (InterfaceKey id : parseInterfaceIds(output)) {
            final String shortName = id.getId();
            expanded.add(new InterfaceKey(
                    parseInterfaceName(blockingRead(String.format(SH_IP_INTERFACES, shortName),
                            cli,
                            instanceIdentifier,
                            ctx)).orElse(shortName)));
        }
        return expanded;
    }

    @VisibleForTesting
    static List<InterfaceKey> parseInterfaceIds(final String output) {
        return ParsingUtils.parseFields(output, 1,
                VRF_INTERFACE_ID_LINE::matcher,
                m -> m.group("id"),
                value -> new InterfaceKey(value.trim()));
    }

    static Optional<String> parseInterfaceName(final String output) {
        return parseField(output, 1,
                INTERFACE_ID_LINE::matcher,
                matcher -> matcher.group("id"));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Nonnull
    @Override
    public InterfaceBuilder getBuilder(@Nonnull InstanceIdentifier<Interface> instanceIdentifier) {
        return new InterfaceBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder interfaceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String ifaceId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        interfaceBuilder.setId(ifaceId);
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                                  @Nonnull Interface readValue,
                                                  @Nonnull ReadContext readContext) {
        // Direct translation
        return Initialized.create(instanceIdentifier, readValue);
    }
}
