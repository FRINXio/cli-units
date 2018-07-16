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

package io.frinx.cli.iosxr.ospf.handler;

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

    private static final String SH_RUN_OSPF_AREA = "show running-config router ospf %s | include ^ area";

    private static final Pattern AREA_ID = Pattern.compile("area (?<areaId>[0-9.]+)");

    private final Cli cli;

    public OspfAreaReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AreaKey> getAllIdsForType(@Nonnull InstanceIdentifier<Area> instanceIdentifier,
                                          @Nonnull ReadContext readContext) throws ReadFailedException {
        String id = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        return parseAreasIds(blockingRead(String.format(SH_RUN_OSPF_AREA, id), cli, instanceIdentifier, readContext));
    }

    private static List<AreaKey> parseAreasIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                AREA_ID::matcher,
            m -> m.group("areaId"),
            area -> new AreaKey(getAreaIdentifier(area)));
    }

    private static OspfAreaIdentifier getAreaIdentifier(String area) {
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
        areaBuilder.setIdentifier(instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier());
    }
}
