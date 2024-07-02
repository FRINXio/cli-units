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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.netflow.handler.util.NetflowUtils;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.EgressFlow;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.EgressFlowBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.EgressFlowKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EgressFlowReader implements CliConfigListReader<EgressFlow, EgressFlowKey, EgressFlowBuilder> {

    private static final String SH_NETFLOW_INTF = "show running-config interface %s | include egress";
    private static final Pattern FLOW_LINE = Pattern.compile("flow (?<type>.+) monitor \\S+( sampler .+)? egress");

    private final Cli cli;

    public EgressFlowReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<EgressFlowKey> getAllIds(@NotNull InstanceIdentifier<EgressFlow> instanceIdentifier, @NotNull
            ReadContext readContext) throws ReadFailedException {
        InterfaceId interfaceId = instanceIdentifier.firstKeyOf(Interface.class)
                .getId();
        return parseFlowKeys(blockingRead(String.format(SH_NETFLOW_INTF, interfaceId.getValue()), cli,
                instanceIdentifier, readContext));
    }

    @VisibleForTesting
    public static List<EgressFlowKey> parseFlowKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
                FLOW_LINE::matcher,
            matcher -> NetflowUtils.getType(matcher.group("type")),
                EgressFlowKey::new
        );
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<EgressFlow> instanceIdentifier, @NotNull
            EgressFlowBuilder ingressFlowBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        final EgressFlowKey key = instanceIdentifier.firstKeyOf(EgressFlow.class);
        ingressFlowBuilder.setNetflowType(key.getNetflowType());
    }
}