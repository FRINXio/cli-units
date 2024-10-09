/*
 * Copyright © 2024 Frinx and others.
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

package io.frinx.cli.unit.ios.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.DefaultRouteDistance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.DefaultRouteDistanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base._default.route.distance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base._default.route.distance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalDefaultRouteDistanceReader
        implements CliConfigReader<DefaultRouteDistance, DefaultRouteDistanceBuilder> {

    private static final String SH_DEFAULT_ROUTE_DISTANCE = "show running-config | section ^ address-family ipv4$";
    private static final String SH_DEFAULT_ROUTE_DISTANCE_VRF =
            "show running-config | section ^ address-family ipv4 vrf %s";

    private final Cli cli;

    public GlobalDefaultRouteDistanceReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<DefaultRouteDistance> id,
                                      @NotNull final DefaultRouteDistanceBuilder builder,
                                      @NotNull final ReadContext ctx) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();

        String output;
        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            String rawOutput = blockingRead(SH_DEFAULT_ROUTE_DISTANCE, cli, id, ctx);
            output = rawOutput.replace(f("address-family ipv4"), "");
        } else {
            String rawOutput = blockingRead(f(SH_DEFAULT_ROUTE_DISTANCE_VRF, vrfName), cli, id, ctx);
            output = rawOutput.replace(f("address-family ipv4 vrf %s", vrfName), "");
        }

        Config config = parseConfig(output);
        builder.setConfig(config);
    }

    @VisibleForTesting
    static Config parseConfig(final String output) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(output);

        List<Short> numbers = new ArrayList<>();

        while (matcher.find()) {
            numbers.add(Short.parseShort(matcher.group()));
        }

        short external;
        short internal;
        if (numbers.isEmpty()) {
            external = 20;
            internal = 200;
        } else {
            external = numbers.get(0);
            internal = numbers.get(1);
        }

        return new ConfigBuilder()
                .setExternalRouteDistance(external)
                .setInternalRouteDistance(internal)
                .build();
    }
}
