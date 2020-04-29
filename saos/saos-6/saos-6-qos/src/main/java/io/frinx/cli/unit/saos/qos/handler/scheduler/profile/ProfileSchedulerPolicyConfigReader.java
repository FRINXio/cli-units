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

package io.frinx.cli.unit.saos.qos.handler.scheduler.profile;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProfileSchedulerPolicyConfigReader implements CompositeReader.Child<Config, ConfigBuilder>,
        CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"traffic-profiling\"";

    private Cli cli;

    public ProfileSchedulerPolicyConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isProfile(instanceIdentifier, readContext)) {
            String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
            String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
            parseSchedulerPolicyConfig(output, configBuilder, policyName);
        }
    }

    @VisibleForTesting
    void parseSchedulerPolicyConfig(String output, ConfigBuilder builder, String policyName) {
        SaosQosScPolicyIfcIdBuilder augBuilder = new SaosQosScPolicyIfcIdBuilder();
        builder.setName(policyName);
        Pattern namePattern = Pattern.compile("traffic-profiling.*port (?<port>\\S+)"
                + " .*" + " name " + policyName + ".*");

        ParsingUtils.parseField(output,
            namePattern::matcher,
            matcher -> matcher.group("port"),
            augBuilder::setInterfaceId);

        builder.addAugmentation(SaosQosScPolicyIfcId.class, augBuilder.build());
    }

    private boolean isProfile(InstanceIdentifier<Config> id, ReadContext context) throws ReadFailedException {
        return ProfileSchedulerPolicyReader.getAllIds(cli, this, id, context)
                .contains(id.firstKeyOf(SchedulerPolicy.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
