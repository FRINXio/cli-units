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

package io.frinx.cli.unit.brocade.network.instance.policy.forwarding;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SH_PF_CONFIG = "show running-config interface | include ethernet | rate-limit";
    private static final Pattern ETHERNET_LINE =
            Pattern.compile("interface (?<id>.+\\d+)(?<policymap> rate-limit (in|out)put policy-map \\S+)?");
    private static final Pattern NEWLINE = Pattern.compile("\n ");

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

        String output = blockingRead(SH_PF_CONFIG, cli, id, context);

        return ParsingUtils.parseFields(NEWLINE.matcher(output).replaceAll(" "), 0,
            ETHERNET_LINE::matcher,
            matcher -> matcher.group("policymap") == null ? null : matcher.group("id"),
            ifcId -> new InterfaceKey(new InterfaceId(ifcId)));
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Interface> id,
        @Nonnull InterfaceBuilder builder,
        @Nonnull ReadContext ctx) throws ReadFailedException {

        builder.setInterfaceId(id.firstKeyOf(Interface.class).getInterfaceId());
    }
}
