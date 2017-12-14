/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.cisco.rev171109.NiPfIfCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.cisco.rev171109.NiPfIfCiscoAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_SERVICE_POLICY_IFC = "sh run int %s | include ^ service-policy";
    private static final Pattern INPUT_SERVICE_POLICY = Pattern.compile("\\s*service-policy input (?<policyMap>\\S+).*");
    private static final Pattern OUTPUT_SERVICE_POLICY = Pattern.compile("\\s*service-policy output (?<policyMap>\\S+).*");

    private final Cli cli;

    public PolicyForwardingInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        if (!DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class))) {
            return;
        }

        String ifcName = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
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

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((InterfaceBuilder) parentBuilder).setConfig(readValue);
    }
}
