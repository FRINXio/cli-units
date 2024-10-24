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
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.modem.extension.cable.modems.CableModem;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.modem.extension.cable.modems.CableModemBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.modem.extension.cable.modems.CableModemKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableModemReader implements CliOperListReader<CableModem, CableModemKey, CableModemBuilder> {

    private Cli cli;

    public CableModemReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_CABLE_MODEM = "show cable modem";

    static final Pattern PARSE_CABLE_MODEM = Pattern.compile(
            "(?<interfaceName>\\d+/[a-z0-9]+/\\d+-\\d+/[a-z0-9]+/\\d+) *(?<mac>\\d+) *(?<bonded>[0-9x-]+) *"
                    + "(?<state>\\D+) *(?<docSis>[0-9.]+) *(?<qos>[0-9A-Z/-]+) *(?<cpe>\\d+) *"
                    + "(?<macAddress>[0-9a-z.]+) *(?<ipAddress>[0-9.-]+)");

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<CableModem> instanceIdentifier,
                                      @NotNull CableModemBuilder cableModemBuilder,
                                      @NotNull ReadContext readContext) {
        cableModemBuilder.setName(instanceIdentifier.firstKeyOf(CableModem.class).getId());
        cableModemBuilder.setId(instanceIdentifier.firstKeyOf(CableModem.class).getId());
    }

    @NotNull
    @Override
    public List<CableModemKey> getAllIds(@NotNull InstanceIdentifier<CableModem> id,
                                       @NotNull ReadContext readContext) throws ReadFailedException {
        return parseAllMacAddresses(blockingRead(SH_CABLE_MODEM, cli, id, readContext));
    }

    @VisibleForTesting
    static List<CableModemKey> parseAllMacAddresses(String output) {
        return ParsingUtils.parseFields(output.substring(532), 0,
                PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("macAddress"),
                CableModemKey::new);
    }
}
