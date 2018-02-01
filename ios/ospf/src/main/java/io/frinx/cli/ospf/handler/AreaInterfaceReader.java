/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfListReader;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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

    private static final String SHOW_OSPF_IFC = "sh run | include ^interface |^ ip ospf";

    private static final Pattern ROUTER_ID = Pattern.compile(".*?interface (?<ifcId>[^\\s]+).*");

    private Cli cli;

    public AreaInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIdsForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                               @Nonnull ReadContext readContext) throws ReadFailedException {
        AreaKey areaKey = instanceIdentifier.firstKeyOf(Area.class);
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);

        String output = blockingRead(SHOW_OSPF_IFC, cli, instanceIdentifier, readContext);

        return parseInterfaceIds(protocolKey.getName(), output, areaKey.getIdentifier());
    }

    static List<InterfaceKey> parseInterfaceIds(String ospfId, String output, OspfAreaIdentifier areaId) {
        String realignedOutput = realignOSPFInterfaces(output);

        // No need to check VRFs, it is impossible to configure ip ospf for an interface that does not match ospf's vrf

        return NEWLINE.splitAsStream(realignedOutput)
                .map(String::trim)
                .filter(ifcLine -> ifcLine.contains(String.format("ip ospf %s area %s", ospfId, areaIdToString(areaId))))
                .map(ROUTER_ID::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("ifcId"))
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
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        interfaceBuilder.setId(instanceIdentifier.firstKeyOf(Interface.class).getId());
    }

    public static String areaIdToString(OspfAreaIdentifier areaId) {
        return areaId.getUint32() != null ? areaId.getUint32().toString() : areaId.getDottedQuad().getValue();
    }

    private static String realignOSPFInterfaces(String output) {
        String withoutNewlines = output.replaceAll("\\n|\\r", "");
        return withoutNewlines.replace("interface", "\ninterface");
    }
}
