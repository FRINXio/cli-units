/*
 * Copyright © 2024 Frinx and others.
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

package io.frinx.cli.unit.cer.cable.handler.cablemodem;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.modem.extension.cable.modems.CableModem;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.modem.extension.cable.modems.cable.modem.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.modem.extension.cable.modems.cable.modem.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableModemStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_CABLE_MAC = "show cable modem %s";

    private final Cli cli;

    public CableModemStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var name = id.firstKeyOf(CableModem.class).getId();
        String output = blockingRead(f(SH_CABLE_MAC, name), cli, id, readContext);
        parseCableModem(output, stateBuilder);
    }

    @VisibleForTesting
    static void parseCableModem(String output, StateBuilder stateBuilder) {
        ParsingUtils.parseField(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("interfaceName"),
                stateBuilder::setInterfaceName);

        ParsingUtils.parseNonDistinctFields(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("mac"),
                stateBuilder::setMac);

        ParsingUtils.parseNonDistinctFields(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("bonded"),
                stateBuilder::setBonded);

        ParsingUtils.parseNonDistinctFields(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("state"),
                stateBuilder::setState);

        ParsingUtils.parseNonDistinctFields(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("docSis"),
                stateBuilder::setDocSis);

        ParsingUtils.parseNonDistinctFields(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("qos"),
                stateBuilder::setQos);

        ParsingUtils.parseNonDistinctFields(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("cpe"),
                stateBuilder::setCpe);

        ParsingUtils.parseNonDistinctFields(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("macAddress"),
                stateBuilder::setMacAddress);

        ParsingUtils.parseNonDistinctFields(output, 0,
                CableModemReader.PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("ipAddress"),
                stateBuilder::setIpAddress);
    }
}
