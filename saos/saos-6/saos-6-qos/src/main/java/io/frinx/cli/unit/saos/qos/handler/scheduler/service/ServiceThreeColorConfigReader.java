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

package io.frinx.cli.unit.saos.qos.handler.scheduler.service;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQos2r3cAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQos2r3cAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceThreeColorConfigReader implements CompositeReader.Child<Config, ConfigBuilder>,
        CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"traffic\"";
    private static final String PIR = "%squeue %s port %s eir %s%s";
    private static final String BE = "%squeue %s port %s%sebs %s%s";
    private static final String WEIGHT = "%squeue %s port %s%sscheduler-weight %s%s";
    private static final String CON_AVOID = "%squeue %s port %s%scongestion-avoidance-profile %s%s";

    private Cli cli;

    public ServiceThreeColorConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        if (isService(instanceIdentifier, readContext)) {
            String policyName = instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName();
            String sequenceId = instanceIdentifier.firstKeyOf(Scheduler.class).getSequence().toString();
            String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
            parseThreeColorConfig(output, configBuilder, sequenceId, policyName);
        }
    }

    @VisibleForTesting
    void parseThreeColorConfig(String output, ConfigBuilder builder, String sequenceId, String policyName) {
        SaosQos2r3cAugBuilder augBuilder = new SaosQos2r3cAugBuilder();
        setPir(output, builder, sequenceId, policyName);
        setBe(output, builder, sequenceId, policyName);
        setWeight(output, augBuilder, sequenceId, policyName);
        setConAvoid(output, augBuilder, sequenceId, policyName);

        builder.addAugmentation(SaosQos2r3cAug.class, augBuilder.build());
    }

    private void setPir(String output, ConfigBuilder builder, String sequenceId, String policyName) {
        Pattern pir = Pattern.compile(f(PIR, ".*", sequenceId, policyName, "(?<eir>\\d+)", ".*"));
        ParsingUtils.parseField(output,
            pir::matcher,
            matcher -> matcher.group("eir"),
            value -> builder.setPir(new BigInteger(value)));
    }

    private void setBe(String output, ConfigBuilder builder, String sequenceId, String policyName) {
        Pattern be = Pattern.compile(f(BE, ".*", sequenceId, policyName, ".*", "(?<ebs>\\d+)", ".*"));
        ParsingUtils.parseField(output,
            be::matcher,
            matcher -> matcher.group("ebs"),
            value -> builder.setBe(Long.parseLong(value)));
    }

    private void setWeight(String output, SaosQos2r3cAugBuilder builder, String sequenceId, String policyName) {
        Pattern weight = Pattern.compile(f(WEIGHT , ".*", sequenceId, policyName, ".*", "(?<weight>\\d+)", ".*"));
        ParsingUtils.parseField(output,
            weight::matcher,
            matcher -> matcher.group("weight"),
            value -> builder.setWeight(Long.parseLong(value)));
    }

    private void setConAvoid(String output, SaosQos2r3cAugBuilder builder, String sequenceId, String policyName) {
        Pattern conAvoid = Pattern.compile(
                f(CON_AVOID , ".*", sequenceId, policyName, ".*", "(?<con>\\S+)", ".*"));
        ParsingUtils.parseField(output,
            conAvoid::matcher,
            matcher -> matcher.group("con"),
            builder::setCongestionAvoidance);
    }

    private boolean isService(InstanceIdentifier<Config> id, ReadContext context) throws ReadFailedException {
        return ServiceSchedulerPolicyReader.getAllIds(cli, this, id, context)
                .contains(id.firstKeyOf(SchedulerPolicy.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}