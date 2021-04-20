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

package io.frinx.cli.unit.ios.routing.policy.handlers.prefix;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.DENY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PERMIT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.PrefixConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.cisco.rev210422.SETOPERATION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.set.top.prefix.sets.PrefixSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.prefix.top.prefixes.prefix.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixConfigWriter implements CliWriter<Config> {

    public static final String WRITE_TEMPLATE = "configure terminal\n"
            + "ip prefix-list {$name} seq {$sequenceId} {$operation} {$network}\n"
            + "end\n";

    public static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no ip prefix-list {$name} seq {$sequenceId} {$operation} {$network}\n"
            + "end\n";

    private final Cli cli;

    public PrefixConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        PrefixSetKey prefixSetKey = id.firstKeyOf(PrefixSet.class);
        blockingWriteAndRead(cli, id, config, fT(WRITE_TEMPLATE,
                "name", prefixSetKey.getName(),
                "sequenceId", config.getAugmentation(PrefixConfigAug.class).getSequenceId(),
                "operation", getOperationName(config),
                "network", config.getIpPrefix().getIpv4Prefix().getValue()));
    }

    public static String getOperationName(@Nonnull Config config) {
        Class<? extends SETOPERATION> action = config.getAugmentation(PrefixConfigAug.class).getOperation();
        if (action.equals(PERMIT.class)) {
            return  "permit";
        } else if (action.equals(DENY.class)) {
            return "deny";
        }
        throw new IllegalStateException("No action found for entry %s" + action);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        PrefixSetKey prefixSetKey = id.firstKeyOf(PrefixSet.class);
        blockingWriteAndRead(cli, id, config, fT(DELETE_TEMPLATE,
                "name", prefixSetKey.getName(),
                "sequenceId", config.getAugmentation(PrefixConfigAug.class).getSequenceId(),
                "operation", getOperationName(config),
                "network", config.getIpPrefix().getIpv4Prefix().getValue()));
    }
}
