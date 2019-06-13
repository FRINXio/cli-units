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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.BfdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceBfdConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    @VisibleForTesting
    static final String SHOW_OSPF_INT =
            "show configuration%s protocols ospf area %s interface %s bfd-liveness-detection | display set";
    private static final Pattern BFD_MIN_INTERVAL_LINE = Pattern.compile(
            "set.* protocols ospf area \\S+ interface \\S+ "
            + "bfd-liveness-detection minimum-interval (?<interval>.+)");
    private static final Pattern BFD_MIN_RECEIVE_LINE = Pattern.compile(
            "set.* protocols ospf area \\S+ interface \\S+ "
            + "bfd-liveness-detection minimum-receive-interval (?<receive>.+)");
    private static final Pattern BFD_MULTIPLIER_LINE = Pattern.compile(
            "set.* protocols ospf area \\S+ interface \\S+ "
            + "bfd-liveness-detection multiplier (?<multiplier>.+)");
    private final Cli cli;

    public AreaInterfaceBfdConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((BfdBuilder) parentBuilder).setConfig(readValue);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull
            ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final String interfaceId = instanceIdentifier.firstKeyOf(Interface.class)
                .getId();
        final String areaId = AreaInterfaceReader.areaIdToString(instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier());
        final String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);

        String output = blockingRead(String.format(SHOW_OSPF_INT, nwInsName, areaId, interfaceId), cli,
                instanceIdentifier, readContext);
        parseBfdInterval(output, configBuilder);
        parseBfdReceiveInterval(output, configBuilder);
        parseBfdMultiplier(output, configBuilder);
    }

    private void parseBfdInterval(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output,
            BFD_MIN_INTERVAL_LINE::matcher,
            matcher -> matcher.group("interval"),
            value -> configBuilder.setMinInterval(Long.valueOf(value)));
    }

    private void parseBfdReceiveInterval(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output,
            BFD_MIN_RECEIVE_LINE::matcher,
            matcher -> matcher.group("receive"),
            value -> configBuilder.setMinReceiveInterval(Long.valueOf(value)));
    }

    private void parseBfdMultiplier(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output,
            BFD_MULTIPLIER_LINE::matcher,
            matcher -> matcher.group("multiplier"),
            value -> configBuilder.setMultiplier(Long.valueOf(value)));
    }
}
