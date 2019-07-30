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

package io.frinx.cli.unit.brocade.network.instance.vrf.policy.forwarding;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.vrf.ifc.VrfInterfaceReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.brocade.rev190726.NiPfIfBrocadeAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.brocade.rev190726.NiPfIfBrocadeAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfPFInterfaceConfigReader implements CompositeReader.Child<Config, ConfigBuilder>,
        CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_PF_CONFIG = "show running-config interface %s";
    private static final Pattern POLICYMAP_IN_PATTERN =
            Pattern.compile("rate-limit input policy-map (?<policymap>\\S+)");
    private static final Pattern POLICYMAP_OUT_PATTERN =
            Pattern.compile("rate-limit output policy-map (?<policymap>\\S+)");

    private final Cli cli;

    public VrfPFInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext ctx) throws ReadFailedException {

        String interfaceId = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        String output = blockingRead(f(SH_PF_CONFIG, interfaceId), cli, id, ctx);

        NiPfIfBrocadeAugBuilder brocadeAug = new NiPfIfBrocadeAugBuilder();

        ParsingUtils.parseField(output, 0,
            POLICYMAP_IN_PATTERN::matcher,
            m -> m.group("policymap"),
            brocadeAug::setInputServicePolicy);
        ParsingUtils.parseField(output, 0,
            POLICYMAP_OUT_PATTERN::matcher,
            m -> m.group("policymap"),
            brocadeAug::setOutputServicePolicy);

        builder.addAugmentation(NiPfIfBrocadeAug.class, brocadeAug.build());
        builder.setInterfaceId(new InterfaceId(interfaceId));
    }

    @Override
    public Check getCheck() {
        return VrfInterfaceReader.VRF_CHECK;
    }
}
