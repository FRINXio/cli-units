/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.RpdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.RpdKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableRpdReader implements CliConfigListReader<Rpd, RpdKey, RpdBuilder> {
    static final String SH_CABLE_RPDS = "show running-config | include ^cable rpd";
    private static final Pattern CABLE_RPD_LINE = Pattern.compile("cable rpd (?<name>\\S+).*");

    private final Cli cli;

    public CableRpdReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<RpdKey> getAllIds(@Nonnull InstanceIdentifier<Rpd> instanceIdentifier,
                                  @Nonnull ReadContext readContext) throws ReadFailedException {
        final String output = blockingRead(SH_CABLE_RPDS, cli, instanceIdentifier, readContext);
        return getCableRpds(output);
    }

    @VisibleForTesting
    static List<RpdKey> getCableRpds(String output) {
        return ParsingUtils.parseFields(output, 0, CABLE_RPD_LINE::matcher,
            matcher -> matcher.group("name"), RpdKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Rpd> instanceIdentifier,
                                      @Nonnull RpdBuilder rpdBuilder,
                                      @Nonnull ReadContext readContext) {
        final String rpdName = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        rpdBuilder.setId(rpdName);
        rpdBuilder.setConfig(new ConfigBuilder().setId(rpdName).build());
    }
}
