/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.io.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDE;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.MaxMetricBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaxMetricConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    private static final Pattern MAX_METRIC_LINE = Pattern.compile("Originating router-LSAs with maximum metric");
    private static final Pattern STARTUP_LINE = Pattern.compile("Condition: on start-up for (?<timeout>[\\d]+) seconds, State: (?<state>.+)");
    private static final Pattern STUB_LINE = Pattern.compile("Advertise stub links with maximum metric in router-LSAs");
    private static final Pattern SUMMARY_LINE = Pattern.compile("Advertise summary-LSAs.*");
    private static final Pattern EXTERNAL_LINE = Pattern.compile("Advertise external-LSAs.*");

    private Cli cli;

    public MaxMetricConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        String ospfId = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        parseTimers(blockingRead(String.format(GlobalConfigReader.SH_OSPF, ospfId), cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    public static void parseTimers(String output, ConfigBuilder builder) {

        ParsingUtils.findMatch(output, MAX_METRIC_LINE, builder::setSet);

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

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((MaxMetricBuilder) builder).setConfig(config);
    }
}
