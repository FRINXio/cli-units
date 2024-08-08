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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.iuc.config.Iuc;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.iuc.config.Iuc.Modulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.iuc.config.IucBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rphy.extension.rev230123.upstream.ofdm.iuc.config.IucKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableInterfaceUpstreamIucConfigReader implements CliConfigListReader<Iuc, IucKey, IucBuilder> {

    private static final String SH_CABLE_UP = "show running-config interface %s";
    private static final Pattern IUC_LINE =
            Pattern.compile("ofdm iuc (?<code>\\d+) low-freq-edge (?<lowFreqEdge>\\d+) "
                    + "high-freq-edge (?<highFreqEdge>\\d+) modulation (?<modulation>.+) "
                    + "pilot-pattern (?<pilotPattern>\\d+)");

    private final Cli cli;

    public CableInterfaceUpstreamIucConfigReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<IucKey> getAllIds(@NotNull InstanceIdentifier<Iuc> instanceIdentifier,
                                  @NotNull ReadContext readContext) throws ReadFailedException {
        List<IucKey> iucKeys = new ArrayList<>();
        final String upstreamCableName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (upstreamCableName.contains("cable-upstream")) {
            String output = blockingRead(f(SH_CABLE_UP, upstreamCableName), cli, instanceIdentifier, readContext);
            AtomicInteger index = new AtomicInteger();
            iucKeys = ParsingUtils.parseNonDistinctFields(output, 0, IUC_LINE::matcher,
                    matcher ->  Integer.valueOf(matcher.group("code")),
                    code -> new IucKey(index.getAndIncrement()));
        }
        return iucKeys;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Iuc> instanceIdentifier,
                                      @NotNull IucBuilder iucBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String upstreamCableName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Integer iucId = instanceIdentifier.firstKeyOf(Iuc.class).getId();
        if (upstreamCableName.contains("cable-upstream")) {
            String output = blockingRead(f(SH_CABLE_UP, upstreamCableName), cli, instanceIdentifier, readContext);
            parseIuc(output, iucId, iucBuilder);
        }
    }

    @VisibleForTesting
    static void parseIuc(String output, int index, IucBuilder iucBuilder) {
        iucBuilder.setId(index);
        List<Integer> codes = ParsingUtils.parseNonDistinctFields(output, 0, IUC_LINE::matcher,
                matcher ->  Integer.valueOf(matcher.group("code")), Function.identity());

        List<String> lowFreqEdges = ParsingUtils.parseNonDistinctFields(output, 0, IUC_LINE::matcher,
                matcher ->  matcher.group("lowFreqEdge"), Function.identity());

        List<String> highFreqEdges = ParsingUtils.parseNonDistinctFields(output, 0, IUC_LINE::matcher,
                matcher ->  matcher.group("highFreqEdge"), Function.identity());

        List<String> modulations = ParsingUtils.parseNonDistinctFields(output, 0, IUC_LINE::matcher,
                matcher ->  matcher.group("modulation"), Function.identity());

        List<Integer> pilotPatterns = ParsingUtils.parseNonDistinctFields(output, 0, IUC_LINE::matcher,
                matcher ->  Integer.valueOf(matcher.group("pilotPattern")), Function.identity());

        iucBuilder.setCode(codes.get(index))
            .setLowFreqEdge(lowFreqEdges.get(index))
            .setHighFreqEdge(highFreqEdges.get(index))
            .setModulation(convertStringToModulation(modulations.get(index)))
            .setPilotPattern(pilotPatterns.get(index));
    }

    private static Modulation convertStringToModulation(String modulationValue) {
        if (modulationValue == null) {
            return null;
        }
        return switch (modulationValue) {
            case "zeroval" -> Modulation.Zeroval;
            case "qpsk" -> Modulation.Qpsk;
            case "8qam" -> Modulation._8qam;
            case "16qam" -> Modulation._16qam;
            case "32qam" -> Modulation._32qam;
            case "64qam" -> Modulation._64qam;
            case "128qam" -> Modulation._128qam;
            case "256qam" -> Modulation._256qam;
            case "512qam" -> Modulation._512qam;
            case "1024qam" -> Modulation._1024qam;
            default -> throw new IllegalArgumentException("Unknown modulation value" + modulationValue);
        };
    }
}
