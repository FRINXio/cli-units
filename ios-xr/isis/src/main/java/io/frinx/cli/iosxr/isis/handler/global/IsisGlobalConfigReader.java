/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.iosxr.isis.handler.global;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisGlobalConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisGlobalConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisInternalLevel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.global.base.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisGlobalConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final String SH_RUN_ROUTER_ISIS = "show running-config router isis %s";
    private static final Pattern ROUTER_ISIS_LINE = Pattern.compile("max-link-metric($| level (?<level>[12]))");

    private Cli cli;

    public IsisGlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> instanceIdentifier,
        @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String instanceName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        String output = blockingRead(f(SH_RUN_ROUTER_ISIS, instanceName), cli, instanceIdentifier, readContext);
        List<IsisInternalLevel> maxLinkMetrics = parseMaxLinkMetric(output);

        if (!maxLinkMetrics.isEmpty()) {
            IsisGlobalConfAugBuilder augmentBuilder = new IsisGlobalConfAugBuilder();
            augmentBuilder.setMaxLinkMetric(maxLinkMetrics);
            builder.addAugmentation(IsisGlobalConfAug.class, augmentBuilder.build());
        }
    }

    private static List<IsisInternalLevel> parseMaxLinkMetric(String output) {
        return ParsingUtils.parseFields(output, 0,
            ROUTER_ISIS_LINE::matcher,
            m -> convertIsisInternalLevel(m.group("level")),
            v -> v);
    }

    private static IsisInternalLevel convertIsisInternalLevel(String text) {
        if ("1".equals(text)) {
            return IsisInternalLevel.LEVEL1;
        }
        if ("2".equals(text)) {
            return IsisInternalLevel.LEVEL2;
        }
        return IsisInternalLevel.NOTSET;
    }
}
