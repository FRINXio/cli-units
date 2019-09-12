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

package io.frinx.cli.unit.ios.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.MplsRsvpSubscriptionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.Percentage;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NiMplsRsvpIfSubscripConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;
    private static final String SH_RSVP_INT = "show running-config interface %s";
    private static final Pattern BW_LINE =
            Pattern.compile("(?<bandwidth>ip rsvp bandwidth ?)(?<percent>percent )?(?<bwValue>\\d*)(K?)");
    @VisibleForTesting
    public static final String DEFAULT = "default";

    public NiMplsRsvpIfSubscripConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifaceName = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        String bandwidthLine = blockingRead(String.format(SH_RSVP_INT, ifaceName), cli, id, ctx);
        parseConfig(bandwidthLine, builder);
    }

    @VisibleForTesting
    public static void parseConfig(String output, ConfigBuilder builder) {
        String polishedOutput = output.replaceAll("\\h+", " ");
        Optional<String> bwOpt = ParsingUtils.parseField(polishedOutput, 0,
            BW_LINE::matcher,
            matcher -> matcher.group("bandwidth"));
        if (!bwOpt.isPresent()) {
            return;
        }
        Optional<String> percentageOpt = ParsingUtils.parseField(polishedOutput, 0,
            BW_LINE::matcher,
            matcher -> matcher.group("percent"));
        Optional<String> bwValueOpt = ParsingUtils.parseField(polishedOutput, 0,
            BW_LINE::matcher,
            matcher -> matcher.group("bwValue"));
        if (percentageOpt.isPresent() && bwValueOpt.isPresent()) {
            builder.setSubscription(new Percentage(Short.valueOf(bwValueOpt.get())));
            return;
        }

        if (bwValueOpt.isPresent()) {
            NiMplsRsvpIfSubscripAugBuilder augBuilder = new NiMplsRsvpIfSubscripAugBuilder();
            String bw = bwValueOpt.get().trim();
            if ("".equals(bw)) {
                // if only the word bandwidth is present, set to "default"
                augBuilder.setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(DEFAULT));
            } else if (!"0".equals(bw)) {
                // if 0, don't set bandwidth field at all
                // if non-zero, change to bps on output
                augBuilder.setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(bps(Long.valueOf(bw))));
            }
            builder.addAugmentation(NiMplsRsvpIfSubscripAug.class, augBuilder.build());
        }
    }

    private static Long bps(Long kbps) {
        return kbps * 1000;
    }
}
