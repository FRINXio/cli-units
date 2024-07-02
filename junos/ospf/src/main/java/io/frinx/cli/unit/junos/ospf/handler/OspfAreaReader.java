/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfAreaReader implements CliConfigListReader<Area, AreaKey, AreaBuilder> {

    @VisibleForTesting
    static final String SH_RUN_OSPF_AREA = "show configuration%s protocols ospf | display set | match area";

    private static final Pattern AREA_ID = Pattern.compile("set.*protocols ospf area (?<areaId>\\S+).*");

    private final Cli cli;

    public OspfAreaReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AreaKey> getAllIds(@NotNull InstanceIdentifier<Area> instanceIdentifier,
                                          @NotNull ReadContext readContext) throws ReadFailedException {
        String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);

        return parseAreasIds(blockingRead(String.format(SH_RUN_OSPF_AREA, nwInsName),
                cli, instanceIdentifier, readContext));
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
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Area> instanceIdentifier,
                                             @NotNull AreaBuilder areaBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        areaBuilder.setIdentifier(instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier());
    }
}