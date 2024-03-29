/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ospf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICALWAYS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICONSWITCHOVER;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.MaxMetricTimer;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.MaxMetricTimerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.MaxMetricTimerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICONSYSTEMBOOT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICTRIGGER;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MaxMetricTimerReader implements CliConfigListReader<MaxMetricTimer, MaxMetricTimerKey,
        MaxMetricTimerBuilder> {

    public static final Pattern MAX_METRIC_LINE = Pattern.compile("max-metric router-lsa(?<trigger>"
            + "(?<onStartup> on-startup)|(?<onSwitchover> on-switchover))*"
            + "(?<timeout> \\d+)*(?<includeStub> include-stub)*"
            + "(?<summaryLsa> summary-lsa)*"
            + "(?<externalLsa> external-lsa)*");

    public static final String SH_RUN_OSPF_MAX_METRIC = "show running-config router ospf %s %s | include ^%smax-metric";

    private Cli cli;

    public MaxMetricTimerReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<MaxMetricTimerKey> getAllIds(@NotNull InstanceIdentifier<MaxMetricTimer> instanceIdentifier,
                                                    @NotNull ReadContext readContext) throws ReadFailedException {
        String ospfId = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        final String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);
        //indent is 1 when reading default config, otherwise it is 2.
        final String indent = nwInsName.isEmpty() ? " " : "  ";

        return parseTimerKeys(blockingRead(String.format(SH_RUN_OSPF_MAX_METRIC, ospfId, nwInsName, indent),
                cli, instanceIdentifier, readContext));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<MaxMetricTimer> instanceIdentifier, @NotNull
            MaxMetricTimerBuilder builder, @NotNull ReadContext readContext) throws ReadFailedException {
        MaxMetricTimerKey timerKey = instanceIdentifier.firstKeyOf(MaxMetricTimer.class);
        builder.setKey(timerKey);
    }

    private List<MaxMetricTimerKey> parseTimerKeys(String output) {

        List<MaxMetricTimerKey> triggers = new ArrayList<>();
        for (String line : output.split(ParsingUtils.NEWLINE.pattern())) {
            Matcher matcher = MAX_METRIC_LINE.matcher(line);
            if (!matcher.find()) {
                continue;
            }
            triggers.add(new MaxMetricTimerKey(translateTrigger(matcher.group("trigger"))));
        }
        return triggers;
    }

    public static Class<? extends MAXMETRICTRIGGER> translateTrigger(@Nullable String trigger) {
        if (trigger == null) {
            return MAXMETRICALWAYS.class;
        }
        if (trigger.trim()
                .equals("on-startup")) {
            return MAXMETRICONSYSTEMBOOT.class;
        }
        if (trigger.trim()
                .equals("on-switchover")) {
            return MAXMETRICONSWITCHOVER.class;
        }
        LOG.warn("Unknown trigger type {}", trigger);
        return null;
    }
}