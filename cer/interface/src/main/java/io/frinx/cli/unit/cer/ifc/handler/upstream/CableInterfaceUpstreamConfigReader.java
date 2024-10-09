/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.upstream;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.UpstreamCablesConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.UpstreamCablesConfig.DocsisMode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.config.OfdmFrequencyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.top.upstream.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.top.upstream.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableInterfaceUpstreamConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_CABLE_UP = "show running-config interface %s";
    private static final Pattern CABLE_MAC_LINE = Pattern.compile(".*cable-mac (?<cableMac>\\d+)");
    private static final Pattern INGRESS_CANCELLATION_LINE =
            Pattern.compile(".*ingress-cancellation interval (?<ingressCancellationInterval>\\d+)");
    private static final Pattern CHANNEL_WIDTH_LINE = Pattern.compile(".*channel-width (?<channelWidth>\\d+)");
    private static final Pattern FREQUENCY_LINE = Pattern.compile(".*frequency (?<frequency>\\d+)");
    private static final Pattern MDD_CHANNEL_PRIORITY_LINE =
            Pattern.compile(".*mdd-channel-priority (?<mddChannelPriority>\\d+)");
    private static final Pattern SUPERVISION_LINE = Pattern.compile(".*supervision (?<supervision>.+)");
    private static final Pattern CHANNEL_ID_LINE = Pattern.compile(".*channel-id (?<channelId>\\d+)");
    private static final Pattern PRE_EQ_ENABLE_LINE = Pattern.compile(".*pre-eq-enable (?<preEqEnable>\\D+)");
    private static final Pattern MODULATION_PROFILE_LINE =
            Pattern.compile(".*modulation-profile (?<modulationProfile>\\d+)");
    private static final Pattern DOCSIS_MODE_LINE = Pattern.compile(".*docsis-mode (?<docsisMode>\\D+)");
    private static final Pattern SPECTRUM_GROUP_LINE = Pattern.compile(".*spectrum-group (?<spectrumGroupId>\\d+)");
    private static final Pattern NO_SHUTDOWN_LINE = Pattern.compile(".* no shutdown");

    private static final Pattern OFDM_FREQUENCY_LINE =
            Pattern.compile("ofdm frequency low-act-edge (?<lowActEdge>\\d+) high-act-edge (?<highActEdge>\\d+)");
    private static final Pattern INC_BURST_NOISE_IMMUNITY_LINE = Pattern.compile("ofdm inc-burst-noise-immunity");

    private final Cli cli;

    public CableInterfaceUpstreamConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String upstreamCableName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (upstreamCableName.contains("cable-upstream")) {
            parseConfig(blockingRead(f(SH_CABLE_UP, upstreamCableName), cli, instanceIdentifier, readContext),
                    configBuilder, upstreamCableName);
        }
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder, String upstreamCableName) {
        ParsingUtils.parseField(output, CABLE_MAC_LINE::matcher,
                matcher -> matcher.group("cableMac"),
                configBuilder::setCableMac);

        ParsingUtils.parseField(output, MDD_CHANNEL_PRIORITY_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("mddChannelPriority")),
                configBuilder::setMddChannelPriority);

        ParsingUtils.parseField(output, SUPERVISION_LINE::matcher,
                matcher -> matcher.group("supervision"),
                configBuilder::setSupervision);

        ParsingUtils.parseField(output, CHANNEL_ID_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("channelId")),
                configBuilder::setChannelId);

        ParsingUtils.parseField(output, MODULATION_PROFILE_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("modulationProfile")),
                configBuilder::setModulationProfile);

        configBuilder.setEnable(true);
        ParsingUtils.parseField(output, NO_SHUTDOWN_LINE::matcher,
                matcher -> true,
                shutdown -> configBuilder.setEnable(false));

        if (upstreamCableName.contains("scq")) {
            ParsingUtils.parseField(output, INGRESS_CANCELLATION_LINE::matcher,
                    matcher -> Integer.valueOf(matcher.group("ingressCancellationInterval")),
                    configBuilder::setIngressCancellationInterval);

            ParsingUtils.parseField(output, FREQUENCY_LINE::matcher,
                    matcher -> matcher.group("frequency"),
                    configBuilder::setCableFrequency);

            ParsingUtils.parseField(output, CHANNEL_WIDTH_LINE::matcher,
                    matcher -> matcher.group("channelWidth"),
                    configBuilder::setChannelWidth);

            configBuilder.setPreEqEnable(false);
            ParsingUtils.parseField(output, PRE_EQ_ENABLE_LINE::matcher,
                    matcher -> matcher.group("preEqEnable"),
                    preEqEnable -> configBuilder.setPreEqEnable(preEqEnable.equals("true")));

            configBuilder.setDocsisMode(UpstreamCablesConfig.DocsisMode.Tdma);
            var docsisModeValue = ParsingUtils.parseField(output, 0,
                    DOCSIS_MODE_LINE::matcher,
                    matcher -> matcher.group("docsisMode"));
            docsisModeValue.ifPresent(s -> configBuilder.setDocsisMode(convertStringToDocsisMode(s)));

            ParsingUtils.parseField(output, SPECTRUM_GROUP_LINE::matcher,
                    matcher -> Integer.valueOf(matcher.group("spectrumGroupId")),
                    configBuilder::setSpectrumGroup);
        }

        if (!upstreamCableName.contains("scq")) {
            OfdmFrequencyBuilder frequencyBuilder = new OfdmFrequencyBuilder();
            ParsingUtils.parseField(output, OFDM_FREQUENCY_LINE::matcher,
                    matcher -> matcher.group("lowActEdge"),
                    frequencyBuilder::setLowActEdge);

            ParsingUtils.parseField(output, OFDM_FREQUENCY_LINE::matcher,
                    matcher -> matcher.group("highActEdge"),
                    frequencyBuilder::setHighActEdge);
            configBuilder.setOfdmFrequency(frequencyBuilder.build());

            ParsingUtils.parseField(output, OFDM_FREQUENCY_LINE::matcher,
                    matcher -> matcher.group("highActEdge"),
                    frequencyBuilder::setHighActEdge);
            configBuilder.setOfdmFrequency(frequencyBuilder.build());

            configBuilder.setIncBurstNoiseImmunity(false);
            ParsingUtils.parseField(output, INC_BURST_NOISE_IMMUNITY_LINE::matcher,
                    matcher -> true,
                    configBuilder::setIncBurstNoiseImmunity);
        }
    }

    private static DocsisMode convertStringToDocsisMode(String docsisModeValue) {
        if (docsisModeValue == null) {
            return null;
        }
        return switch (docsisModeValue) {
            case "atdma" -> DocsisMode.Atdma;
            case "tdma" -> DocsisMode.Tdma;
            case "tdma-atdma" -> DocsisMode.TdmaAtdma;
            default -> throw new IllegalArgumentException("Unknown docsis mode value" + docsisModeValue);
        };
    }
}