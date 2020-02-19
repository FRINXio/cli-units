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
import java.math.BigInteger;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OneRateTwoColorConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search running-config string \"traffic-profiling\"";
    private static final String CIR = "%sname %s cir %s%s";

    private Cli cli;

    public OneRateTwoColorConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
        String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
        parseOneRateTwoColorConfig(output, configBuilder, policyName);
    }

    @VisibleForTesting
    void parseOneRateTwoColorConfig(String output, ConfigBuilder builder, String policyName) {
        setCir(output, builder, policyName);
    }

    private void setCir(String output, ConfigBuilder builder, String policyName) {
        Pattern cir = Pattern.compile(f(CIR, ".*", policyName, "(?<cir>\\d+)", ".*"));
        ParsingUtils.parseField(output,
            cir::matcher,
            matcher -> matcher.group("cir"),
            value -> builder.setCir(new BigInteger(value)));
    }
}
