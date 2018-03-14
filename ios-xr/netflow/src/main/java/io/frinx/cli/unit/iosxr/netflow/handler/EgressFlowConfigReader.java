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
import io.frinx.cli.registry.common.TypedReader;
import io.frinx.cli.unit.iosxr.netflow.handler.util.FlowDetails;
import io.frinx.cli.unit.iosxr.netflow.handler.util.InterfaceCheckUtil;
import io.frinx.cli.unit.iosxr.netflow.handler.util.NetflowUtils;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.EgressFlow;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.EgressFlowBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.egress.flow.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.egress.flow.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EgressFlowConfigReader implements TypedReader<Config, ConfigBuilder>, CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_INTERFACE_FLOW_CFG =
        "do show running-config interface %s | include flow %s monitor";
    private static final Pattern FLOW_MONITOR =
        Pattern.compile("flow (?<type>.+) monitor (?<monitorName>\\S+)( sampler (?<samplerName>.+))? egress");

    private final Cli cli;

    public EgressFlowConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean containsAllKeys(final Stream<? extends Identifier<? extends DataObject>> keys,
                                   final InstanceIdentifier<Config> instanceIdentifier) {
        return InterfaceCheckUtil.checkInterfaceType(instanceIdentifier, EthernetCsmacd.class, Ieee8023adLag.class);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull final InstanceIdentifier<Config> id,
                                      @Nonnull final ConfigBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        final InterfaceId ifcName = id.firstKeyOf(Interface.class).getId();
        final Class<? extends NETFLOWTYPE> flowType = id.firstKeyOf(EgressFlow.class).getNetflowType();

        parseNetflowConfig(id,
            blockingRead(f(SH_SINGLE_INTERFACE_FLOW_CFG, ifcName.getValue(), NetflowUtils.getNetflowStringType(flowType)), cli, id, ctx),
            builder
        );
    }

    private void parseNetflowConfig(final InstanceIdentifier<Config> iid,
                                    final String output,
                                    final ConfigBuilder builder) {
        final Class<? extends NETFLOWTYPE> flowType = iid.firstKeyOf(EgressFlow.class).getNetflowType();

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

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> parentBuilder, @Nonnull final Config readValue) {
        ((EgressFlowBuilder) parentBuilder).setConfig(readValue);
    }
}
