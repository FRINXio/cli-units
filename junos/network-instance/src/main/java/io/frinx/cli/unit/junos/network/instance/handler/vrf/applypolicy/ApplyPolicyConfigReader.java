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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.applypolicy;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ApplyPolicyConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final String SH_IMPORT_POLICY_TEMPLATE =
        "show configuration routing-instances %s routing-options instance-import | display set";
    private static final Pattern IMPORT_POLICY_LINE_PATTERN =
        Pattern.compile("set routing-instances (?<vrfid>\\S+) routing-options instance-import (?<policyname>\\S+)");

    private Cli cli;

    public ApplyPolicyConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config data) {
        ((ApplyPolicyBuilder) builder).setConfig(data);
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull ConfigBuilder configBuilder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String vrfName = id.firstKeyOf(NetworkInstance.class).getName();
        if (vrfName.equals(NetworInstance.DEFAULT_NETWORK_NAME)) {
            return;
        }

        String output = blockingRead(f(SH_IMPORT_POLICY_TEMPLATE, vrfName), cli, id, readContext);

        List<String> policyList = ParsingUtils.parseFields(output, 0,
            IMPORT_POLICY_LINE_PATTERN::matcher,
            m -> m.group("policyname"),
            v -> v);

        if (policyList != null && !policyList.isEmpty()) {
            configBuilder.setImportPolicy(policyList);
        }
    }
}
