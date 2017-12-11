/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfListReader;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceReader implements OspfListReader.OspfConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SHOW_OSPF_INT = "sh run router ospf %s area %s interface";
    private static final Pattern INTERFACE_NAME_LINE = Pattern.compile("interface (?<name>.*)");

    private Cli cli;

    public AreaInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIdsForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);
        AreaKey areaKey = instanceIdentifier.firstKeyOf(Area.class);

        String output = blockingRead(String.format(SHOW_OSPF_INT, protocolKey.getName(), areaIdToString(areaKey.getIdentifier())), cli, instanceIdentifier, readContext);
        return parseInterfaceIds(output);
    }

    @VisibleForTesting
    public static List<InterfaceKey> parseInterfaceIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                INTERFACE_NAME_LINE::matcher,
                matcher -> matcher.group("name"),
                InterfaceKey::new);
    }

    public static String areaIdToString(OspfAreaIdentifier id) {
        return (id.getDottedQuad() != null) ? id.getDottedQuad().getValue() : id.getUint32().toString();
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
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                             @Nonnull InterfaceBuilder interfaceBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        interfaceBuilder.setId(instanceIdentifier.firstKeyOf(Interface.class).getId());
    }
}
