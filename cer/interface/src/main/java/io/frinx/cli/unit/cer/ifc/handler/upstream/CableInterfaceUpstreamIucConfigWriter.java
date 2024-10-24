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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.iuc.top.upstream.iuc.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.iuc.config.Iuc;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableInterfaceUpstreamIucConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            configure interface {$name}
            {% if($difference) %}
            {% loop in $before.iuc as $before_iuc counter=$i %}
            {% loop in $before_modulations as $before_modulation counter=$j %}
            {% if ($i == $j) %} no ofdm iuc {$before_iuc.code} low-freq-edge {$before_iuc.low_freq_edge} high-freq-edge {$before_iuc.high_freq_edge} modulation {$before_modulation} pilot-pattern {$before_iuc.pilot_pattern}
            {% endif %}
            {% onEmpty %}{% endloop %}
            {% onEmpty %}{% endloop %}
            {% loop in $config.iuc as $after_iuc counter=$i %}
            {% loop in $after_modulations as $after_modulation counter=$j %}
            {% if ($i == $j) %} ofdm iuc {$after_iuc.code} low-freq-edge {$after_iuc.low_freq_edge} high-freq-edge {$after_iuc.high_freq_edge} modulation {$after_modulation} pilot-pattern {$after_iuc.pilot_pattern}
            {% endif %}
            {% onEmpty %}{% endloop %}
            {% onEmpty %}{% endloop %}
            {% endif %}
            end""";

    @SuppressWarnings("checkstyle:linelength")
    private static final String DELETE_TEMPLATE = """
            configure interface {$name}
            {% loop in $before.iuc as $before_iuc counter=$i %}
            {% loop in $before_modulations as $before_modulation counter=$j %}
            {% if ($i == $j) %} no ofdm iuc {$before_iuc.code} low-freq-edge {$before_iuc.low_freq_edge} high-freq-edge {$before_iuc.high_freq_edge} modulation {$before_modulation} pilot-pattern {$before_iuc.pilot_pattern}
            {% endif %}
            {% onEmpty %}{% endloop %}
            {% onEmpty %}{% endloop %}
            end""";

    private final Cli cli;

    public CableInterfaceUpstreamIucConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "before", null,
                        "config", config,
                        "name", name,
                        "difference", Chunk.TRUE,
                        "after_modulations", convertModulationsToString(config)));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String difference = isThereDifferenceBetweenIucs(dataBefore, dataAfter) ? Chunk.TRUE : null;
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(WRITE_TEMPLATE,
                        "before", dataBefore,
                        "config", dataAfter,
                        "name", name,
                        "difference", difference,
                        "before_modulations", convertModulationsToString(dataBefore),
                        "after_modulations", convertModulationsToString(dataAfter)));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataBefore,
                fT(DELETE_TEMPLATE,
                        "before", dataBefore,
                        "name", name,
                        "before_modulations", convertModulationsToString(dataBefore)));
    }

    private boolean isThereDifferenceBetweenIucs(Config dataBefore, Config dataAfter) {
        List<Iuc> iucBefore = dataBefore != null ? dataBefore.getIuc() : null;
        List<Iuc> iucAfter = dataAfter != null ? dataAfter.getIuc() : null;
        return !Objects.equals(iucAfter, iucBefore);
    }

    private List<String> convertModulationsToString(Config data) {
        if (data.getIuc() == null) {
            return null;
        }
        List<String> convertedModulations = new ArrayList<>();
        for (Iuc iuc: data.getIuc()) {
            convertedModulations.add(
                switch (iuc.getModulation()) {
                    case Zeroval -> "zeroval";
                    case Qpsk -> "qpsk";
                    case _8qam -> "8qam";
                    case _16qam -> "16qam";
                    case _32qam -> "32qam";
                    case _64qam -> "64qam";
                    case _128qam -> "128qam";
                    case _256qam -> "256qam";
                    case _512qam -> "512qam";
                    case _1024qam -> "1024qam";
                    default -> throw new IllegalArgumentException("Unknown modulation" + iuc.getModulation());
                });
        }
        return convertedModulations;
    }
}