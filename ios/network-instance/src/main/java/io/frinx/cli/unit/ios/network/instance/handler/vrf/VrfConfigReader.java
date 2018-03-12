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

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String SH_IP_VRF_CFG = "show running-config | include ^ip vrf|^ rd";
    private static final Pattern RD_LINE = Pattern.compile(".*rd (?<rd>(\\S+):(\\S+)).*");
    private Cli cli;

    public VrfConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isVrf(instanceIdentifier, readContext)) {
            String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
            parseVrfConfig(blockingRead(SH_IP_VRF_CFG, cli, instanceIdentifier, readContext),
                    configBuilder, name);
        }
    }

    @VisibleForTesting
    static void parseVrfConfig(String output, ConfigBuilder builder, String vrf) {
        String realignedOutput = realignOutput(output);

        String config = NEWLINE.splitAsStream(realignedOutput)
                .filter(vrfConfigLine -> vrfConfigLine.contains(String.format("ip vrf %s ", vrf)))
                .findAny()
                .orElse("");

        builder.setName(vrf);
        builder.setType(L3VRF.class);

        parseField(config,
            RD_LINE::matcher,
            matcher -> matcher.group("rd"),
            rd -> builder.setRouteDistinguisher(new RouteDistinguisher(rd)));

        // TODO set other attributes i.e. description
    }

    private boolean isVrf(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return VrfReader.getAllIds(id, readContext, cli, this).contains(id.firstKeyOf(NetworkInstance.class));
    }

    private static String realignOutput(String output) {
        String withoutNewlines = output.replaceAll("[\r\n]", "");
        // We want to see the output in the form
        // " \nip vrf VRF  rd RD ip vrf VRF_WITHOUT_RD \nip vrf ANOTHER_VRF  rd RD \nip vrf ANOTHER_VRF_WITHOUT_RD ".
        // Note the space after each vrf name. That way we can distinguish VRFs
        // with names that are prefixes of name of some another VRFs in the
        // following processing of the config.
        return withoutNewlines.replaceAll("$", " ").replaceAll("ip vrf ", " \nip vrf ");
    }
}
