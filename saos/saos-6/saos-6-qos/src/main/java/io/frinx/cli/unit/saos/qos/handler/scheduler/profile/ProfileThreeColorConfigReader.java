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
import java.math.BigInteger;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProfileThreeColorConfigReader implements CompositeReader.Child<Config, ConfigBuilder>,
        CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"traffic-profiling\"";

    private Cli cli;

    public ProfileThreeColorConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        if (isProfile(instanceIdentifier, readContext)) {
            String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
            String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
            parseThreeColorConfig(output, configBuilder, policyName);
        }
    }

    @VisibleForTesting
    void parseThreeColorConfig(String output, ConfigBuilder builder, String policyName) {
        setCir(output, builder, policyName);
    }

    private void setCir(String output, ConfigBuilder builder, String policyName) {
        Pattern cir = Pattern.compile("traffic-profiling standard-profile.* name "
                + policyName + " cir (?<cir>\\S+) .*");
        ParsingUtils.parseField(output,
            cir::matcher,
            matcher -> matcher.group("cir"),
            value -> builder.setCir(new BigInteger(value)));
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