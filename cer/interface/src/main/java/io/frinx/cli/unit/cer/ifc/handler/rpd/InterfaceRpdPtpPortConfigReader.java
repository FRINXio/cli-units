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

package io.frinx.cli.unit.cer.ifc.handler.rpd;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ptp.port.config.PtpPort;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ptp.port.config.PtpPortBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ptp.port.config.PtpPortKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceRpdPtpPortConfigReader implements CliConfigListReader<PtpPort, PtpPortKey, PtpPortBuilder> {

    private static final Pattern PTP_PORT_ID_LINE = Pattern.compile(".*ptp port (?<portId>\\d+)");
    private static final Pattern PTP_PORT_ROLE_LINE = Pattern.compile(".*role (?<role>\\D+)");
    private static final Pattern PTP_PORT_LOCAL_PRIORITY_LINE =
            Pattern.compile(".*local-priority (?<localPriority>\\d+)");
    private static final Pattern PTP_PORT_MASTER_CLOCK_LINE =
            Pattern.compile(".*master-clock address (?<masterClockAddress>.+)");
    private static final Pattern PTP_PORT_ENABLE_LINE = Pattern.compile(".*no shutdown");

    private final Cli cli;

    public InterfaceRpdPtpPortConfigReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<PtpPortKey> getAllIds(@NotNull InstanceIdentifier<PtpPort> instanceIdentifier,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        List<PtpPortKey> ids = new ArrayList<>();
        final String rpdName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (rpdName.contains("rpd")) {
            String output = blockingRead(f(InterfaceRpdConfigReader.SH_CABLE_UP, rpdName),
                    cli, instanceIdentifier, readContext);

            ids = ParsingUtils.parseNonDistinctFields(output, 0,
                    PTP_PORT_ID_LINE::matcher,
                    matcher ->  Integer.valueOf(matcher.group("portId")),
                    PtpPortKey::new);
        }

        return ids;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<PtpPort> instanceIdentifier,
                                      @NotNull PtpPortBuilder ptpPortBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String rpdName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Integer ptpPortId = instanceIdentifier.firstKeyOf(PtpPort.class).getId();
        if (rpdName.contains("rpd")) {
            String output = blockingRead(f(InterfaceRpdConfigReader.SH_CABLE_UP, rpdName),
                    cli, instanceIdentifier, readContext);
            if (output.contains("ptp port")) {
                String[] lines = output.split("(?<=\\n)");
                int line = 0;
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i].contains("ptp port")) {
                        line = i;
                        break;
                    }
                }
                String ptpOutput = Arrays.stream(lines).skip(line).collect(Collectors.joining(""));
                parsePtpPorts(ptpOutput, ptpPortId, ptpPortBuilder);
            }
        }
    }

    @VisibleForTesting
    static void parsePtpPorts(String ptpPortOutput, Integer ptpPortId, PtpPortBuilder ptpPortBuilder) {
        String[] lines = ptpPortOutput.split("(?<=\\n)");
        int firstLine = 0;
        int endLine = 0;
        List<Integer> indexesOfPtpPort = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("ptp port " + ptpPortId)) {
                firstLine = i;
                continue;
            }
            if (lines[i].contains("exit")) {
                endLine = i;
                indexesOfPtpPort.add(firstLine);
                indexesOfPtpPort.add(endLine);
                break;
            }
        }

        ptpPortBuilder.setId(ptpPortId);
        String partOfPtpPort = Arrays.toString(
                Arrays.copyOfRange(lines, indexesOfPtpPort.get(0), indexesOfPtpPort.get(1)));
        ParsingUtils.parseField(partOfPtpPort,
                PTP_PORT_ROLE_LINE::matcher,
                matcher ->  matcher.group("role"),
                ptpPortBuilder::setRole);

        ParsingUtils.parseField(partOfPtpPort,
                PTP_PORT_LOCAL_PRIORITY_LINE::matcher,
                matcher ->  Integer.valueOf(matcher.group("localPriority")),
                ptpPortBuilder::setLocalPriority);

        ParsingUtils.parseField(partOfPtpPort,
                PTP_PORT_MASTER_CLOCK_LINE::matcher,
                matcher ->  matcher.group("masterClockAddress"),
                ptpPortBuilder::setMasterClockAddress);

        ptpPortBuilder.setEnable(false);
        ParsingUtils.parseField(partOfPtpPort,
                PTP_PORT_ENABLE_LINE::matcher,
                matcher ->  true,
                ptpPortBuilder::setEnable);
    }
}