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

package io.frinx.cli.unit.cer.cable.handler.cablemac;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.CableMac;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.CableMacBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.cer.rev230125.cable.mac.extension.cable.macs.CableMacKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableMacReader implements CliOperListReader<CableMac, CableMacKey, CableMacBuilder> {

    private Cli cli;

    public CableMacReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_CABLE_MODEM = "show cable modem";

    static final Pattern PARSE_CABLE_MODEM = Pattern.compile(
            "(?<interfaceName>\\d+/[a-z0-9]+/\\d+-\\d+/[a-z0-9]+/\\d+) *(?<mac>\\d+) *(?<bonded>[0-9x-]+) *"
                    + "(?<state>\\D+) *(?<docSis>[0-9.]+) *(?<qos>[0-9A-Z/-]+) *(?<cpe>\\d+) *"
                    + "(?<macAddress>[0-9a-z.]+) *(?<ipAddress>[0-9.-]+)");

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<CableMac> instanceIdentifier,
                                      @NotNull CableMacBuilder cableMacBuilder,
                                      @NotNull ReadContext readContext) {
        cableMacBuilder.setName(instanceIdentifier.firstKeyOf(CableMac.class).getId());
        cableMacBuilder.setId(instanceIdentifier.firstKeyOf(CableMac.class).getId());
    }

    @NotNull
    @Override
    public List<CableMacKey> getAllIds(@NotNull InstanceIdentifier<CableMac> id,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        return parseAllMacKeys(blockingRead(SH_CABLE_MODEM, cli, id, readContext));
    }

    @VisibleForTesting
    static List<CableMacKey> parseAllMacKeys(String output) {
        return ParsingUtils.parseFields(output.substring(532), 0,
                PARSE_CABLE_MODEM::matcher,
                matcher -> matcher.group("mac"),
                CableMacKey::new);
    }
}