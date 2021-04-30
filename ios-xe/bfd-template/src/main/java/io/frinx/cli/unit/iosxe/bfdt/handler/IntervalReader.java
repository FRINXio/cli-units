/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.bfdt.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.Interval;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.IntervalBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IntervalReader implements CliConfigReader<Interval, IntervalBuilder> {

    private static final Pattern BFD_TEMPLATE_INTERVAL = Pattern.compile("interval min-tx (?<tx>\\S+) min-rx "
            + "(?<rx>\\S+) multiplier (?<multiplier>\\S+)");

    private Cli cli;

    public IntervalReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interval> instanceIdentifier,
                                      @Nonnull IntervalBuilder intervalBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String bfdTemplateName = instanceIdentifier.firstKeyOf(BfdTemplate.class).getName();
        final String showCommand = f(BfdTemplateConfigReader.SH_BFD_TEMPLATE, bfdTemplateName);
        final String bfdTemplateOutput = blockingRead(showCommand, cli, instanceIdentifier, readContext);
        parseInterval(bfdTemplateOutput, intervalBuilder);
    }

    @VisibleForTesting
    static void parseInterval(String output, IntervalBuilder intervalBuilder) {
        final Optional<String> bfdTemplateTx = ParsingUtils.parseField(output, 0,
            BFD_TEMPLATE_INTERVAL::matcher,
            matcher -> matcher.group("tx"));

        final Optional<String> bfdTemplateRx = ParsingUtils.parseField(output, 0,
            BFD_TEMPLATE_INTERVAL::matcher,
            matcher -> matcher.group("rx"));

        final Optional<String> bfdTemplateMultiplier = ParsingUtils.parseField(output, 0,
            BFD_TEMPLATE_INTERVAL::matcher,
            matcher -> matcher.group("multiplier"));

        if (bfdTemplateMultiplier.isPresent() && bfdTemplateRx.isPresent() && bfdTemplateTx.isPresent()) {
            intervalBuilder.setMinTx(bfdTemplateTx.get());
            intervalBuilder.setMinRx(bfdTemplateRx.get());
            intervalBuilder.setMultiplier(bfdTemplateMultiplier.get());
        }
    }
}
