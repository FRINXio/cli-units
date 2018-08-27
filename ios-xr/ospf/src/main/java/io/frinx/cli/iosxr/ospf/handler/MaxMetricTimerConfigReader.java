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

package io.frinx.cli.iosxr.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICALWAYS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICONSWITCHOVER;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.MaxMetricTimer;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.MaxMetricTimerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.MaxMetricTimerKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.max.metric.timer.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.max.metric.timer.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICONSYSTEMBOOT;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MaxMetricTimerConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public MaxMetricTimerConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((MaxMetricTimerBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        String ospfId = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        MaxMetricTimerKey timerKey = instanceIdentifier.firstKeyOf(MaxMetricTimer.class);
        configBuilder.setTrigger(timerKey.getTrigger());
        final String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);
        //indent is 1 when reading default config, otherwise it is 2.
        final String indent = nwInsName.isEmpty() ? " " : "  ";

        parseTimers(blockingRead(String.format(MaxMetricTimerReader.SH_RUN_OSPF_MAX_METRIC, ospfId, nwInsName, indent),
                cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    public static void parseTimers(String output, ConfigBuilder builder) {

        Set<Class<? extends MAXMETRICINCLUDE>> includes = new HashSet<>();
        for (String line : output.split(ParsingUtils.NEWLINE.pattern())) {
            Matcher matcher = MaxMetricTimerReader.MAX_METRIC_LINE.matcher(line);
            if (!matcher.find()) {
                continue;
            }
            if ((builder.getTrigger()
                    .equals(MAXMETRICALWAYS.class) && matcher.group("trigger") != null)
                    ||
                    (builder.getTrigger()
                            .equals(MAXMETRICONSWITCHOVER.class) && matcher.group("onSwitchover") == null)
                    ||
                    builder.getTrigger()
                            .equals(MAXMETRICONSYSTEMBOOT.class) && matcher.group("onStartup") == null) {
                continue;
            }
            if (matcher.group("externalLsa") != null) {
                includes.add(MAXMETRICINCLUDETYPE2EXTERNAL.class);
            }
            if (matcher.group("summaryLsa") != null) {
                includes.add(MAXMETRICSUMMARYLSA.class);
            }
            if (matcher.group("includeStub") != null) {
                includes.add(MAXMETRICINCLUDESTUB.class);
            }
            if (matcher.group("timeout") != null) {
                builder.setTimeout(new BigInteger(matcher.group("timeout")
                        .trim()));
            }
        }
        if (includes.size() > 0) {
            builder.setInclude(Lists.newArrayList(includes));
        }
    }
}
