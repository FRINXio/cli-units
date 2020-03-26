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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ProfileSchedulerPolicyReader
        implements CompositeListReader.Child<SchedulerPolicy, SchedulerPolicyKey, SchedulerPolicyBuilder>,
        CliConfigListReader<SchedulerPolicy, SchedulerPolicyKey, SchedulerPolicyBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"traffic-profiling\"";
    private static final Pattern ALL_IDS = Pattern.compile("traffic-profiling.*name (?<name>\\S+).*");

    private Cli cli;

    public ProfileSchedulerPolicyReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SchedulerPolicyKey> getAllIds(@Nonnull InstanceIdentifier<SchedulerPolicy> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(cli, this, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    static List<SchedulerPolicyKey> getAllIds(Cli cli, CliReader cliReader,
                                              InstanceIdentifier<?> id,
                                              ReadContext context) throws ReadFailedException {
        String output = cliReader.blockingRead(SHOW_COMMAND, cli, id, context);

        return ParsingUtils.parseFields(output, 0,
            ALL_IDS::matcher,
            matcher -> matcher.group("name"),
            SchedulerPolicyKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<SchedulerPolicy> instanceIdentifier,
                                      @Nonnull SchedulerPolicyBuilder schedulerPolicyBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        schedulerPolicyBuilder.setName(instanceIdentifier.firstKeyOf(SchedulerPolicy.class).getName());
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
