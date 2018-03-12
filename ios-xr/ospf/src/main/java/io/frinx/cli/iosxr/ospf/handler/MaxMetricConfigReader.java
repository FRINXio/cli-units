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
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.MaxMetricBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaxMetricConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    private static final Pattern MAX_METRIC_LINE = Pattern.compile(".*max-metric router-lsa.*");
    private static final Pattern STARTUP_LINE = Pattern.compile(".*on-startup (?<timeout>\\d+).*");
    private static final Pattern STUB_LINE = Pattern.compile(".*include-stub.*");
    private static final Pattern SUMMARY_LINE = Pattern.compile(".*summary-lsa.*");
    private static final Pattern EXTERNAL_LINE = Pattern.compile(".*external-lsa.*");

    private static final String SH_RUN_OSPF_MAX_METRIC = "do show running-config router ospf %s | include ^ max-metric";

    private Cli cli;

    public MaxMetricConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        String ospfId = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        parseTimers(blockingRead(String.format(SH_RUN_OSPF_MAX_METRIC, ospfId), cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    public static void parseTimers(String output, ConfigBuilder builder) {

        Optional<Boolean> maybeMax = ParsingUtils.parseField(output, 0, MAX_METRIC_LINE::matcher, Matcher::matches);

        if (maybeMax.isPresent() && maybeMax.get()) {
            builder.setSet(true);
            ParsingUtils.parseField(output,
                    STARTUP_LINE::matcher,
                    matcher -> matcher.group("timeout"),
                    value -> builder.setTimeout(new BigInteger(value)));

            List<Class<? extends MAXMETRICINCLUDE>> includes = new ArrayList<>();
            ParsingUtils.parseField(output,
                    STUB_LINE::matcher,
                    Matcher::matches,
                    value -> includes.add(MAXMETRICINCLUDESTUB.class));

            ParsingUtils.parseField(output,
                    SUMMARY_LINE::matcher,
                    Matcher::matches,
                    value -> includes.add(MAXMETRICSUMMARYLSA.class));

            ParsingUtils.parseField(output,
                    EXTERNAL_LINE::matcher,
                    Matcher::matches,
                    value -> includes.add(MAXMETRICINCLUDETYPE2EXTERNAL.class));
            builder.setInclude(includes);
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((MaxMetricBuilder) builder).setConfig(config);
    }
}
