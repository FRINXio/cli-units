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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.L2protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.L2protocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.l2protocols.service.instance.l2protocol.L2protocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2protocolReader implements CliConfigListReader<L2protocol, L2protocolKey, L2protocolBuilder> {

    private final Cli cli;

    private static final Pattern SERVICE_INSTANCE_L2PROTOCOL_LINE =
            Pattern.compile("l2protocol (?<operation>\\S+) (?<protocol>.+)");


    public L2protocolReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<L2protocolKey> getAllIds(@Nonnull InstanceIdentifier<L2protocol> instanceIdentifier,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long serviceInstanceId = instanceIdentifier.firstKeyOf(ServiceInstance.class).getId();
        final String showCommand = f(ServiceInstanceConfigReader.SH_SERVICE_INSTANCE, ifcName, serviceInstanceId);
        final String serviceInstanceOutput = blockingRead(showCommand, cli, instanceIdentifier, readContext);
        return parseL2protocolNames(serviceInstanceOutput);
    }

    @VisibleForTesting
    static List<L2protocolKey> parseL2protocolNames(@Nonnull String aclNameConfiguration) {
        return ParsingUtils.parseFields(aclNameConfiguration, 0,
            SERVICE_INSTANCE_L2PROTOCOL_LINE::matcher,
            matcher -> matcher.group("operation"),
            L2protocolKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<L2protocol> instanceIdentifier,
                                      @Nonnull L2protocolBuilder l2protocolBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        l2protocolBuilder.setName(instanceIdentifier.firstKeyOf(L2protocol.class).getName());
    }
}
