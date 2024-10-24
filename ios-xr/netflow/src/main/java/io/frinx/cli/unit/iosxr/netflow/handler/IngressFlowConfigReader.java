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

package io.frinx.cli.unit.iosxr.netflow.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.netflow.handler.util.FlowDetails;
import io.frinx.cli.unit.iosxr.netflow.handler.util.NetflowUtils;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.ingress.flows.IngressFlow;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.ingress.flows.ingress.flow.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.ingress.flows.ingress.flow.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressFlowConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_INTERFACE_FLOW_CFG =
            "show running-config interface %s | include flow %s monitor";
    private static final Pattern FLOW_MONITOR =
            Pattern.compile("flow (?<type>.+) monitor (?<monitorName>\\S+)( sampler (?<samplerName>.+))? ingress");

    private final Cli cli;

    public IngressFlowConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                             @NotNull final ConfigBuilder builder,
                                             @NotNull final ReadContext ctx) throws ReadFailedException {
        final InterfaceId ifcName = id.firstKeyOf(Interface.class)
                .getId();
        final Class<? extends NETFLOWTYPE> flowType = id.firstKeyOf(IngressFlow.class)
                .getNetflowType();

        parseNetflowConfig(id,
                blockingRead(f(SH_SINGLE_INTERFACE_FLOW_CFG, ifcName.getValue(), NetflowUtils
                        .getNetflowStringType(flowType)), cli, id, ctx),
                builder
        );
    }

    private void parseNetflowConfig(final InstanceIdentifier<Config> iid,
                                    final String output, final ConfigBuilder builder) {
        final Class<? extends NETFLOWTYPE> flowType = iid.firstKeyOf(IngressFlow.class)
                .getNetflowType();

        ParsingUtils.parseField(output, 0,
                FLOW_MONITOR::matcher,
                FlowDetails::fromMatcher,
            flowDetails -> {
                builder.setMonitorName(flowDetails.getMonitorName());
                builder.setSamplerName(flowDetails.getSamplerName());
                builder.setNetflowType(flowType);
            }
        );
    }
}