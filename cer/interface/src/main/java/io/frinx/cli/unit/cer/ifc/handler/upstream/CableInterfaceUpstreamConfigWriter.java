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

import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.UpstreamCablesConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.config.OfdmFrequency;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.top.upstream.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableInterfaceUpstreamConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String CABLE_WRITE_TEMPLATE = """
            configure interface {$name}
            {% if($config.cable_mac) %}cable cable-mac {$config.cable_mac}
            {% endif %}{% if($config.ingress_cancellation_interval) %}cable ingress-cancellation interval {$config.ingress_cancellation_interval}
            {% endif %}{% if($config.channel_width) %}cable channel-width {$config.channel_width}
            {% endif %}{% if($config.cable_frequency) %}cable frequency {$config.cable_frequency}
            {% endif %}{% if($config.mdd_channel_priority) %}cable mdd-channel-priority {$config.mdd_channel_priority}
            {% endif %}{% if($config.supervision) %}cable supervision {$config.supervision}
            {% endif %}{% if($config.channel_id) %}cable channel-id {$config.channel_id}
            {% endif %}{% if($pre_eq_enable) %}cable pre-eq-enable true
            {% else %}cable pre-eq-enable false
            {% endif %}
            {% if($config.modulation_profile) %}cable modulation-profile {$config.modulation_profile}
            {% endif %}{% if($docsis_mode) %}cable docsis-mode {$docsis_mode}
            {% endif %}{% if($config.spectrum_group) %}cable spectrum-group {$config.spectrum_group}
            {% endif %}{% if($enable) %}cable no shutdown
            {% else %}cable shutdown
            {% endif %}end""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String OFDM_WRITE_TEMPLATE = """
            configure interface {$name}
            {% if($config.cable_mac) %}ofdm cable-mac {$config.cable_mac}
            {% endif %}{% if($config.ofdm_frequency) %}ofdm frequency low-act-edge {$config.ofdm_frequency.low_act_edge} high-act-edge {$config.ofdm_frequency.high_act_edge}
            {% endif %}{% if($config.mdd_channel_priority) %}ofdm mdd-channel-priority {$config.mdd_channel_priority}
            {% endif %}{% if($config.supervision) %}ofdm supervision {$config.supervision}
            {% endif %}{% if($config.channel_id) %}ofdm channel-id {$config.channel_id}
            {% endif %}{% if($config.modulation_profile) %}ofdm modulation-profile {$config.modulation_profile}
            {% endif %}{% if($inc_burst_noise_immunity) %}ofdm inc-burst-noise-immunity
            {% endif %}{% if($enable) %}ofdm no shutdown
            {% else %}ofdm shutdown
            {% endif %}end""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String CABLE_UPDATE_TEMPLATE = """
            configure interface {$name}
            {% if($cable_mac) %}no cable cable-mac
            cable cable-mac {$config.cable_mac}
            {% elseif(!$config.cable_mac) %}no cable cable-mac
            {% endif %}{% if($ingress_cancellation_interval) %}cable ingress-cancellation interval {$config.ingress_cancellation_interval}
            {% elseif(!$config.ingress_cancellation_interval) %}no cable ingress-cancellation
            {% endif %}{% if($channel_width) %}cable channel-width {$config.channel_width}
            {% endif %}{% if($cable_frequency) %}cable frequency {$config.cable_frequency}
            {% endif %}{% if($mdd_channel_priority) %}cable mdd-channel-priority {$config.mdd_channel_priority}
            {% elseif(!config.mdd_channel_priority) %}no cable mdd-channel-priority
            {% endif %}{% if($supervision) %}{% if($before.supervision) %}no cable supervision {$before.supervision}
            cable supervision {$config.supervision}
            {% else %}cable supervision {$config.supervision}
            {% endif %}{% elseif(!$config.supervision && $before.supervision) %}no cable supervision {$before.supervision}
            {% endif %}{% if($channel_id) %}cable channel-id {$config.channel_id}
            {% endif %}{% if($pre_eq_enable) %}cable pre-eq-enable {$pre_eq_enable}
            {% endif %}{% if($modulation_profile) %}cable modulation-profile {$config.modulation_profile}
            {% elseif(!$config.modulation_profile) %}no cable modulation-profile
            {% endif %}{% if($docsis_mode) %}cable docsis-mode {$docsis_mode}
            {% endif %}{% if($spectrum_group) %}no cable spectrum-group
            cable spectrum-group {$config.spectrum_group}
            {% elseif(!$config.spectrum_group) %}no cable spectrum-group
            {% endif %}{% if($enable) %}cable {$enable}
            {% endif %}end""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String OFDM_UPDATE_TEMPLATE = """
            configure interface {$name}
            {% if($cable_mac) %}no ofdm cable-mac
            ofdm cable-mac {$config.cable_mac}
            {% elseif(!$config.cable_mac) %}no ofdm cable-mac
            {% endif %}{% if($ofdm_frequency) %}ofdm frequency low-act-edge {$config.ofdm_frequency.low_act_edge} high-act-edge {$config.ofdm_frequency.high_act_edge}
            {% endif %}{% if($mdd_channel_priority) %}ofdm mdd-channel-priority {$config.mdd_channel_priority}
            {% elseif(!$config.mdd_channel_priority) %}no ofdm mdd-channel-priority
            {% endif %}{% if($supervision) %}{% if($before.supervision) %}no ofdm supervision {$before.supervision}
            ofdm supervision {$config.supervision}
            {% else %}ofdm supervision {$config.supervision}
            {% endif %}{% elseif(!$config.supervision && $before.supervision) %}no ofdm supervision {$before.supervision}
            {% endif %}{% if($channel_id) %}ofdm channel-id {$config.channel_id}
            {% endif %}{% if($modulation_profile) %}ofdm modulation-profile {$config.modulation_profile}
            {% elseif(!$config.modulation_profile) %}no ofdm modulation-profile
            {% endif %}{% if($inc_burst_noise_immunity) %}ofdm inc-burst-noise-immunity
            {% else %}{% if(!$after_inc_burst_noise_immunity) %}no ofdm inc-burst-noise-immunity
            {% endif %}{% endif %}{% if($enable) %}ofdm {$enable}
            {% endif %}end""";

    private static final String CABLE_DELETE_TEMPLATE = """
            configure interface {$name}
            no cable
            end""";

    private static final String OFDM_DELETE_TEMPLATE = """
            configure interface {$name}
            no ofdm
            end""";

    private final Cli cli;

    public CableInterfaceUpstreamConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (name.contains("cable-upstream") && name.contains("scq")) {
            String docsisMode = convertDocsisModeToString(config.getDocsisMode());
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    fT(CABLE_WRITE_TEMPLATE,
                            "before", null,
                            "config", config,
                            "name", name,
                            "enable", (config.isEnable() != null && config.isEnable()) ? Chunk.TRUE : null,
                            "docsis_mode", docsisMode,
                            "pre_eq_enable", (config.isPreEqEnable() != null
                                    && config.isPreEqEnable()) ? Chunk.TRUE : null));
        } else if (name.contains("cable-upstream")) {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    fT(OFDM_WRITE_TEMPLATE,
                            "before", null,
                            "config", config,
                            "name", name,
                            "enable", (config.isEnable() != null && config.isEnable()) ? Chunk.TRUE : null,
                            "inc_burst_noise_immunity", (config.isIncBurstNoiseImmunity() != null
                                    && config.isIncBurstNoiseImmunity()) ? Chunk.TRUE : null));
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (name.contains("cable-upstream") && name.contains("scq")) {
            blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                    fT(CABLE_UPDATE_TEMPLATE,
                            "before", dataBefore,
                            "config", dataAfter,
                            "name", name,
                            "cable_mac", updateCableMac(dataBefore, dataAfter),
                            "ingress_cancellation_interval", updateIngressCancellationInterval(dataBefore, dataAfter),
                            "channel_width", updateChannelWidth(dataBefore, dataAfter),
                            "cable_frequency", updateCableFrequency(dataBefore, dataAfter),
                            "mdd_channel_priority", updateMddChannelPriority(dataBefore, dataAfter),
                            "supervision", updateSupervision(dataBefore, dataAfter),
                            "channel_id", updateChannelId(dataBefore, dataAfter),
                            "pre_eq_enable", updatePreEqEnable(dataBefore, dataAfter),
                            "modulation_profile", updateModulationProfile(dataBefore, dataAfter),
                            "docsis_mode", updateDocsisMode(dataBefore, dataAfter),
                            "spectrum_group", updateSpectrumGroup(dataBefore, dataAfter),
                            "enable", updateEnable(dataBefore, dataAfter)));
        } else if (name.contains("cable-upstream")) {
            blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                    fT(OFDM_UPDATE_TEMPLATE,
                            "before", dataBefore,
                            "config", dataAfter,
                            "name", name,
                            "cable_mac", updateCableMac(dataBefore, dataAfter),
                            "ofdm_frequency", updateOfdmFrequency(dataBefore, dataAfter),
                            "mdd_channel_priority", updateMddChannelPriority(dataBefore, dataAfter),
                            "supervision", updateSupervision(dataBefore, dataAfter),
                            "channel_id", updateChannelId(dataBefore, dataAfter),
                            "modulation_profile", updateModulationProfile(dataBefore, dataAfter),
                            "inc_burst_noise_immunity", updateIncBurstNoiseImmunity(dataBefore, dataAfter),
                            "after_inc_burst_noise_immunity", (dataAfter.isIncBurstNoiseImmunity() != null
                                    && dataAfter.isIncBurstNoiseImmunity()) ? Chunk.TRUE : null,
                            "enable", updateEnable(dataBefore, dataAfter)));
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config configBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (name.contains("cable-upstream") && name.contains("scq")) {
            blockingWriteAndRead(cli, instanceIdentifier, configBefore,
                    fT(CABLE_DELETE_TEMPLATE,
                            "config", configBefore,
                            "name", name));
        } else if (name.contains("cable-upstream")) {
            blockingWriteAndRead(cli, instanceIdentifier, configBefore,
                    fT(OFDM_DELETE_TEMPLATE,
                            "config", configBefore,
                            "name", name));
        }

    }

    private String updateCableMac(Config dataBefore, Config dataAfter) {
        String cableMacBefore = dataBefore.getCableMac();
        String cableMacAfter = dataAfter.getCableMac();
        if (!Objects.equals(cableMacAfter, cableMacBefore)) {
            return cableMacAfter;
        }
        return null;
    }

    private Integer updateIngressCancellationInterval(Config dataBefore, Config dataAfter) {
        Integer ingressCancellationIntervalBefore = dataBefore.getIngressCancellationInterval();
        Integer ingressCancellationIntervalAfter = dataAfter.getIngressCancellationInterval();
        if (!Objects.equals(ingressCancellationIntervalAfter, ingressCancellationIntervalBefore)) {
            return ingressCancellationIntervalAfter;
        }
        return null;
    }

    private String updateChannelWidth(Config dataBefore, Config dataAfter) {
        String channelWidthBefore = dataBefore.getChannelWidth();
        String channelWidthAfter = dataAfter.getChannelWidth();
        if (!Objects.equals(channelWidthAfter, channelWidthBefore)) {
            return channelWidthAfter;
        }
        return null;
    }

    private String updateCableFrequency(Config dataBefore, Config dataAfter) {
        String cableFrequencyBefore = dataBefore.getCableFrequency();
        String cableFrequencyAfter = dataAfter.getCableFrequency();
        if (!Objects.equals(cableFrequencyAfter, cableFrequencyBefore)) {
            return cableFrequencyAfter;
        }
        return null;
    }

    private Integer updateMddChannelPriority(Config dataBefore, Config dataAfter) {
        Integer mddChannelPriorityBefore = dataBefore.getMddChannelPriority();
        Integer mddChannelPriorityAfter = dataAfter.getMddChannelPriority();
        if (!Objects.equals(mddChannelPriorityAfter, mddChannelPriorityBefore)) {
            return mddChannelPriorityAfter;
        }
        return null;
    }

    private String updateSupervision(Config dataBefore, Config dataAfter) {
        String supervisionBefore = dataBefore.getSupervision();
        String supervisionAfter = dataAfter.getSupervision();
        if (!Objects.equals(supervisionAfter, supervisionBefore)) {
            return supervisionAfter;
        }
        return null;
    }

    private Integer updateChannelId(Config dataBefore, Config dataAfter) {
        Integer channelIdBefore = dataBefore.getChannelId();
        Integer channelIdAfter = dataAfter.getChannelId();
        if (!Objects.equals(channelIdAfter, channelIdBefore)) {
            return channelIdAfter;
        }
        return null;
    }

    private String updatePreEqEnable(Config dataBefore, Config dataAfter) {
        Boolean enabledBefore = dataBefore.isPreEqEnable();
        Boolean enabledAfter = dataAfter.isPreEqEnable();
        if (!Objects.equals(enabledAfter, enabledBefore)) {
            return enabledAfter ? "true" : "false";
        }
        return null;
    }

    private Integer updateModulationProfile(Config dataBefore, Config dataAfter) {
        Integer modulationProfileBefore = dataBefore.getModulationProfile();
        Integer modulationProfileAfter = dataAfter.getModulationProfile();
        if (!Objects.equals(modulationProfileAfter, modulationProfileBefore)) {
            return modulationProfileAfter;
        }
        return null;
    }

    private String updateDocsisMode(Config dataBefore, Config dataAfter) {
        String docsisModeBefore = convertDocsisModeToString(dataBefore.getDocsisMode());
        String docsisModeAfter = convertDocsisModeToString(dataAfter.getDocsisMode());
        if (!Objects.equals(docsisModeAfter, docsisModeBefore)) {
            return docsisModeAfter;
        }
        return null;
    }

    private Integer updateSpectrumGroup(Config dataBefore, Config dataAfter) {
        Integer spectrumGroupBefore = dataBefore.getSpectrumGroup();
        Integer spectrumGroupAfter = dataAfter.getSpectrumGroup();
        if (!Objects.equals(spectrumGroupAfter, spectrumGroupBefore)) {
            return spectrumGroupAfter;
        }
        return null;
    }

    private OfdmFrequency updateOfdmFrequency(Config dataBefore, Config dataAfter) {
        OfdmFrequency ofdmFrequencyBefore = dataBefore.getOfdmFrequency();
        OfdmFrequency ofdmFrequencyAfter = dataAfter.getOfdmFrequency();
        if (!Objects.equals(ofdmFrequencyAfter, ofdmFrequencyBefore)) {
            return ofdmFrequencyAfter;
        }
        return null;
    }

    private String updateIncBurstNoiseImmunity(Config dataBefore, Config dataAfter) {
        Boolean incBurstNoiseImmunityBefore = dataBefore.isIncBurstNoiseImmunity();
        Boolean incBurstNoiseImmunityAfter = dataAfter.isIncBurstNoiseImmunity();
        if (!Objects.equals(incBurstNoiseImmunityAfter, incBurstNoiseImmunityBefore)) {
            return incBurstNoiseImmunityAfter ? Chunk.TRUE : null;
        }
        return null;
    }

    private String updateEnable(Config dataBefore, Config dataAfter) {
        Boolean enabledBefore = dataBefore.isEnable();
        Boolean enabledAfter = dataAfter.isEnable();
        if (!Objects.equals(enabledAfter, enabledBefore)) {
            return enabledAfter ? "no shutdown" : "shutdown";
        }
        return null;
    }

    private String convertDocsisModeToString(UpstreamCablesConfig.DocsisMode docsisMode) {
        if (docsisMode == null) {
            return null;
        }
        return switch (docsisMode) {
            case Atdma -> "atdma";
            case Tdma -> "tdma";
            case TdmaAtdma -> "tdma-atdma";
            default -> throw new IllegalArgumentException("Unknown docsis mode " + docsisMode);
        };
    }
}