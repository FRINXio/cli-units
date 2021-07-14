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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.L2protocolConfig.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.L2protocolConfig.ProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.L2protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.l2protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.l2protocol.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class L2protocolConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public L2protocolConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        String protocolName = id.firstKeyOf(L2protocol.class).getName();
        Long serviceInstanceId = id.firstKeyOf(ServiceInstance.class).getId();
        final String showCommand = f(ServiceInstanceConfigReader.SH_SERVICE_INSTANCE, ifcName, serviceInstanceId);
        final String serviceInstanceOutput = blockingRead(showCommand, cli, id, ctx);
        parseL2Protocol(serviceInstanceOutput, protocolName, builder);
    }

    @VisibleForTesting
    static void parseL2Protocol(String output, String protocolType, ConfigBuilder builder) {
        final Pattern l2protocolLine =
                Pattern.compile(String.format("l2protocol %s (?<protocol>.+)", protocolType));
        final Optional<String> protocols = ParsingUtils.parseField(output, 0,
            l2protocolLine::matcher,
            matcher -> matcher.group("protocol"));

        if (protocols.isPresent()) {
            builder.setProtocolType(getL2protocolProtocolType(protocolType));
            builder.setProtocol(getL2protocolProtocol(Arrays.asList(protocols.get().split(" "))));
        }
    }

    private static ProtocolType getL2protocolProtocolType(final String name) {
        for (final ProtocolType type : ProtocolType.values()) {
            if (name.equalsIgnoreCase(type.getName())) {
                return type;
            }
        }
        return null;
    }

    private static List<Protocol> getL2protocolProtocol(final List<String> names) {
        List<String> namesLowered = names.stream().map(String::toLowerCase).collect(Collectors.toList());
        List<Protocol> protocols = new ArrayList<>();
        for (final Protocol protocol : Protocol.values()) {
            if (namesLowered.contains(protocol.getName().toLowerCase())) {
                protocols.add(protocol);
            }
        }
        return protocols.isEmpty() ? null : protocols;
    }
}
