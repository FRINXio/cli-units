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

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.mpls.MplsReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.MplsRsvpSubscriptionConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.extension.rev171024.NiMplsRsvpIfSubscripAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.mpls.rsvp.subscription.subscription.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NiMplsRsvpIfSubscripAugReader implements MplsReader.MplsConfigReader<NiMplsRsvpIfSubscripAug,
        NiMplsRsvpIfSubscripAugBuilder> {

    private Cli cli;
    private static final String SH_RSVP_INT = "show running-config rsvp interface %s";
    private static final Pattern IFACE_LINE = Pattern.compile("bandwidth(?<bandwidth>.*)(K?)");
    @VisibleForTesting
    public static final String DEFAULT = "default";

    public NiMplsRsvpIfSubscripAugReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<NiMplsRsvpIfSubscripAug> instanceIdentifier,
                                             @Nonnull NiMplsRsvpIfSubscripAugBuilder niMplsRsvpIfSubscripAugBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        final InterfaceId name = instanceIdentifier.firstKeyOf(Interface.class)
                .getInterfaceId();
        parseConfig(blockingRead(String.format(SH_RSVP_INT, name.getValue()), cli, instanceIdentifier, readContext),
                niMplsRsvpIfSubscripAugBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String output, NiMplsRsvpIfSubscripAugBuilder builder) {
        Optional<String> bwOpt = ParsingUtils.parseField(output.replaceAll("\\h+", " "), 0,
                IFACE_LINE::matcher,
            matcher -> matcher.group("bandwidth"));

        if (bwOpt.isPresent()) {
            String bw = bwOpt.get()
                    .trim();
            if ("".equals(bw)) {
                // if only the word bandwidth is present, set to "default"
                builder.setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(DEFAULT));
            } else if (!"0".equals(bw)) {
                // if 0, don't set bandwidth field at all
                // if non-zero, change to bps on output
                builder.setBandwidth(new MplsRsvpSubscriptionConfig.Bandwidth(bps(Long.valueOf(bw))));
            }
        }
    }

    private static Long bps(Long kbps) {
        return kbps * 1000;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull NiMplsRsvpIfSubscripAug
            readValue) {
        ((ConfigBuilder) parentBuilder).addAugmentation(NiMplsRsvpIfSubscripAug.class, readValue);
    }
}
