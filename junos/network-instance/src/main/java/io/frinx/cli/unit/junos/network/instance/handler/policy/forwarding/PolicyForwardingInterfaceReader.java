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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {
    @VisibleForTesting
    static final String SH_CONFIG = "show configuration class-of-service | display set";
    static final Pattern INET_PRECEDENCE_LINE = Pattern.compile(
        "set class-of-service interfaces (?<ifname>\\S+) unit (?<unit>\\S+) "
        + "classifiers inet-precedence (?<classifier>\\S+)");

    private final Cli cli;

    public PolicyForwardingInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(
        @Nonnull InstanceIdentifier<Interface> id,
        @Nonnull ReadContext context) throws ReadFailedException {

        if (!NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class))) {
            return Collections.emptyList();
        }

        String output = blockingRead(SH_CONFIG, cli, id, context);

        return ParsingUtils.parseFields(output, 0,
            INET_PRECEDENCE_LINE::matcher,
            m -> f("%s.%s", m.group("ifname"), m.group("unit")),
            name -> new InterfaceKey(new InterfaceId(name)));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> readData) {
        ((InterfacesBuilder) builder).setInterface(readData);
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Interface> id,
        @Nonnull InterfaceBuilder builder,
        @Nonnull ReadContext ctx) throws ReadFailedException {

        builder.setInterfaceId(id.firstKeyOf(Interface.class).getInterfaceId());
    }
}
