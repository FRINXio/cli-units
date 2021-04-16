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
import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceL2protocol.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceL2protocol.ProtocolType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.L2protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.L2protocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2protocolReader implements CliConfigReader<L2protocol, L2protocolBuilder> {

    private static final Pattern SERVICE_INSTANCE_L2PROTOCOL_LINE =
            Pattern.compile("l2protocol (?<operation>\\S+) (?<protocol>.+)");

    private Cli cli;

    public L2protocolReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<L2protocol> id, @Nonnull L2protocolBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        Long serviceInstanceId = id.firstKeyOf(ServiceInstance.class).getId();
        final String showCommand = f(ServiceInstanceConfigReader.SH_SERVICE_INSTANCE, ifcName, serviceInstanceId);
        final String serviceInstanceOutput = blockingRead(showCommand, cli, id, ctx);
        parseL2Protocol(serviceInstanceOutput, builder);
    }

    @VisibleForTesting
    static void parseL2Protocol(String output, L2protocolBuilder builder) {
        final Optional<String> protocolType = ParsingUtils.parseField(output, 0,
            SERVICE_INSTANCE_L2PROTOCOL_LINE::matcher,
            matcher -> matcher.group("operation"));
        final Optional<String> protocols = ParsingUtils.parseField(output, 0,
            SERVICE_INSTANCE_L2PROTOCOL_LINE::matcher,
            matcher -> matcher.group("protocol"));

        if (protocols.isPresent() && protocolType.isPresent()) {
            builder.setProtocolType(getL2protocolProtocolType(protocolType.get()));
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
        List<Protocol> protocols = new ArrayList<>();
        for (final Protocol protocol : Protocol.values()) {
            if (names.contains(protocol.getName().toLowerCase())) {
                protocols.add(protocol);
            }
        }
        return protocols.isEmpty() ? null : protocols;
    }

}