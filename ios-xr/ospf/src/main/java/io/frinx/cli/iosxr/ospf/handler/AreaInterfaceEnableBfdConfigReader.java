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
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.enable.EnableBfdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.enable.enable.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.enable.enable.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceEnableBfdConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_OSPF_INT = "show running-config router ospf %s %s area %s interface %s";
    private static final Pattern ENABLE_BFD_LINE = Pattern.compile("bfd fast-detect(?<disable> disable)*");
    private final Cli cli;

    public AreaInterfaceEnableBfdConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((EnableBfdBuilder) parentBuilder).setConfig(readValue);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull
            ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final String interfaceId = instanceIdentifier.firstKeyOf(Interface.class)
                .getId();
        final String ospfId = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        final String areaId = AreaInterfaceReader.areaIdToString(instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier());
        final String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);

        String output = blockingRead(String.format(SHOW_OSPF_INT, ospfId, nwInsName, areaId, interfaceId), cli,
                instanceIdentifier, readContext);
        parseBfd(output, configBuilder);
    }

    private void parseBfd(String output, ConfigBuilder configBuilder) {
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(ENABLE_BFD_LINE::matcher)
                .filter(Matcher::find)
                .findAny()
                .ifPresent(matcher -> {
                    if (matcher.group("disable") == null) {
                        configBuilder.setEnabled(true);
                    } else {
                        configBuilder.setEnabled(false);
                    }
                });
    }
}
