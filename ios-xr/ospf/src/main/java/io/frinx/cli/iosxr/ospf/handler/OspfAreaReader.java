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
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.AreasBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfAreaReader implements OspfListReader.OspfConfigListReader<Area, AreaKey, AreaBuilder> {
    private static final Pattern AREA_ID = Pattern.compile(".*?Area (?:BACKBONE\\()?(?<areaId>[0-9.]+).*");

    private final Cli cli;

    public OspfAreaReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AreaKey> getAllIdsForType(@Nonnull InstanceIdentifier<Area> instanceIdentifier,
                                          @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        return parseAreasIds(blockingRead(String.format(GlobalConfigReader.SH_OSPF, id), cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    public static List<AreaKey> parseAreasIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                AREA_ID::matcher,
                m -> m.group("areaId"),
                area -> new AreaKey(getAreaIdentifier(area)));
    }

    public static OspfAreaIdentifier getAreaIdentifier(String area) {
        if (area.contains(".")) {
            return new OspfAreaIdentifier(new DottedQuad(area));
        } else {
            return new OspfAreaIdentifier(Long.valueOf(area));
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Area> list) {
        ((AreasBuilder) builder).setArea(list);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Area> instanceIdentifier,
                                             @Nonnull AreaBuilder areaBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        areaBuilder.setIdentifier(instanceIdentifier.firstKeyOf(Area.class).getIdentifier());
    }

    @Nonnull
    @Override
    public AreaBuilder getBuilder(@Nonnull InstanceIdentifier<Area> instanceIdentifier) {
        return new AreaBuilder();
    }
}
