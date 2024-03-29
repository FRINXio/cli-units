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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceReader implements CliConfigListReader<Interface, InterfaceKey,
        InterfaceBuilder> {

    @VisibleForTesting
    static final String SHOW_OSPF_INT = "show configuration%s protocols ospf area %s | display set";
    private static final Pattern INTERFACE_NAME_LINE =
            Pattern.compile("set.* protocols ospf area \\S+ interface (?<name>\\S+).*");

    private Cli cli;

    public AreaInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        AreaKey areaKey = instanceIdentifier.firstKeyOf(Area.class);
        final String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);

        String output = blockingRead(String.format(SHOW_OSPF_INT, nwInsName,
                areaIdToString(areaKey.getIdentifier())), cli, instanceIdentifier, readContext);
        return parseInterfaceIds(output);
    }

    private static List<InterfaceKey> parseInterfaceIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            INTERFACE_NAME_LINE::matcher,
            matcher -> matcher.group("name"),
            InterfaceKey::new);
    }

    public static String areaIdToString(OspfAreaIdentifier id) {
        return (id.getDottedQuad() != null) ? id.getDottedQuad().getValue() : id.getUint32().toString();
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder interfaceBuilder,
                                      @NotNull ReadContext readContext) {
        interfaceBuilder.setId(instanceIdentifier.firstKeyOf(Interface.class).getId());
    }
}