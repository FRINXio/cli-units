/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.qos.handler.scheduler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SchedulerPolicyConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search running-config string \"traffic-profiling\"";
    private static final String IFC_ID = "%sport %s%s name %s%s";

    private Cli cli;

    public SchedulerPolicyConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        parseSchedulerPolicyConfig(output, configBuilder, policyName);
    }

    @VisibleForTesting
    void parseSchedulerPolicyConfig(String output, ConfigBuilder builder, String policyName) {
        builder.setName(policyName);

        if (!policyName.matches("\\d+")) {
            SaosQosScPolicyIfcIdBuilder augBuilder = new SaosQosScPolicyIfcIdBuilder();
            setIfcId(output, augBuilder, policyName);
            builder.addAugmentation(SaosQosScPolicyIfcId.class, augBuilder.build());
        }
    }

    private void setIfcId(String output, SaosQosScPolicyIfcIdBuilder augBuilder, String policyName) {
        Pattern type = Pattern.compile(f(IFC_ID, ".*", "(?<id>\\d+)", ".*", policyName, ".*"));
        ParsingUtils.parseField(output,
            type::matcher,
            matcher -> matcher.group("id"),
            augBuilder::setInterfaceId);
    }
}
