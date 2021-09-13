/*
 * Copyright © 2021 Frinx and others.
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
package io.frinx.cli.unit.huawei.qos.handler.ifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIngressInterfaceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIngressInterfaceAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos._interface.input.top.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos._interface.input.top.input.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class IngressInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern SERVICE_POLICY_LINE = Pattern.compile("traffic-policy (?<type>\\S+) inbound");

    private final Cli cli;

    public IngressInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        final String output = blockingRead(f(InterfaceReader.SH_INTERFACE, interfaceId),
                cli, instanceIdentifier, readContext);
        fillInConfig(output, configBuilder);
    }

    public static void fillInConfig(final String output, final ConfigBuilder configBuilder) {
        final Optional<String> servicePolicy = getServicePolicy(output);
        if (servicePolicy.isPresent()) {
            final QosIngressInterfaceAugBuilder augBuilder = new QosIngressInterfaceAugBuilder();
            augBuilder.setServicePolicy(servicePolicy.get());
            configBuilder.addAugmentation(QosIngressInterfaceAug.class, augBuilder.build());
        }
    }

    private static Optional<String> getServicePolicy(final String output) {
        return ParsingUtils.parseField(output, 0, SERVICE_POLICY_LINE::matcher,
            matcher -> matcher.group("type"));
    }
}
