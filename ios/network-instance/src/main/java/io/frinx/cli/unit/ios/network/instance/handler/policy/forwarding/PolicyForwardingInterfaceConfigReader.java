/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.network.instance.handler.policy.forwarding;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.cisco.rev171109.NiPfIfCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.cisco.rev171109.NiPfIfCiscoAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_SERVICE_POLICY_IFC = "show running-config interface %s | include ^ service-policy";
    private static final Pattern INPUT_SERVICE_POLICY = Pattern.compile("\\s*service-policy input (?<policyMap>\\S+)"
            + ".*");
    private static final Pattern OUTPUT_SERVICE_POLICY = Pattern.compile("\\s*service-policy output "
            + "(?<policyMap>\\S+).*");

    private final Cli cli;

    public PolicyForwardingInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        if (!NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class))) {
            return;
        }

        String ifcName = id.firstKeyOf(Interface.class)
            .getInterfaceId()
            .getValue();
        String output = blockingRead(String.format(SHOW_SERVICE_POLICY_IFC, ifcName), cli, id, ctx);

        NiPfIfCiscoAugBuilder niPfIfCiscoAugBuilder = new NiPfIfCiscoAugBuilder();

        ParsingUtils.parseFields(output, 0,
            INPUT_SERVICE_POLICY::matcher,
            matcher -> matcher.group("policyMap"),
            niPfIfCiscoAugBuilder::setInputServicePolicy);


        ParsingUtils.parseFields(output, 0,
            OUTPUT_SERVICE_POLICY::matcher,
            matcher -> matcher.group("policyMap"),
            niPfIfCiscoAugBuilder::setOutputServicePolicy);

        if (niPfIfCiscoAugBuilder.getInputServicePolicy() == null
                && niPfIfCiscoAugBuilder.getOutputServicePolicy() == null) {
            return;
        }

        builder.addAugmentation(NiPfIfCiscoAug.class, niPfIfCiscoAugBuilder.build());
        builder.setInterfaceId(new InterfaceId(ifcName));
    }
}