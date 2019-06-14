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

package io.frinx.cli.unit.junos.network.instance.handler.policy.forwarding;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.NiPfIfJuniperAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.NiPfIfJuniperAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.juniper.pf._interface.extension.config.Classifiers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.juniper.pf._interface.extension.config.ClassifiersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.juniper.rev171109.juniper.pf._interface.extension.config.classifiers.InetPrecedenceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    @VisibleForTesting
    static final String SHOW_CONFIG_TEMPLATE = "show configuration class-of-service interfaces %s "
        + "unit %s classifiers inet-precedence | display set";

    static final Pattern INTERFACE_ID_PATTERN = Pattern.compile("(?<ifname>[^\\.]+)\\.(?<unit>\\S+)");

    private final Cli cli;

    public PolicyForwardingInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext ctx) throws ReadFailedException {

        if (!NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class))) {
            return;
        }

        String interfaceId = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        Matcher matcher = INTERFACE_ID_PATTERN.matcher(interfaceId);

        matcher.matches();
        String output = blockingRead(
            f(SHOW_CONFIG_TEMPLATE, matcher.group("ifname"), matcher.group("unit")),
            cli,
            id,
            ctx);

        InetPrecedenceBuilder precedenceBuilder = new InetPrecedenceBuilder();

        ParsingUtils.parseField(output, 0,
            PolicyForwardingInterfaceReader.INET_PRECEDENCE_LINE::matcher,
            m -> m.group("classifier"),
            precedenceBuilder::setName);

        Classifiers classifiers = new ClassifiersBuilder()
            .setInetPrecedence(precedenceBuilder.build())
            .build();

        NiPfIfJuniperAug juniperAug = new NiPfIfJuniperAugBuilder()
            .setClassifiers(classifiers)
            .build();

        builder.addAugmentation(NiPfIfJuniperAug.class, juniperAug);
        builder.setInterfaceId(new InterfaceId(interfaceId));
    }
}
