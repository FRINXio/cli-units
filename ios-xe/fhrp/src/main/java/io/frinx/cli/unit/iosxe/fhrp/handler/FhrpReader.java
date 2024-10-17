/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.fhrp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.Fhrp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.FhrpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.fhrp.Version.Vrrp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.fhrp.VersionBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FhrpReader implements CliConfigReader<Fhrp, FhrpBuilder> {

    public static final String SH_RUN_FHRP_DEFINITION = "show running-config | include fhrp version";
    private static final Pattern DHRP_VERSION_CHUNK = Pattern.compile("fhrp version vrrp (?<version>\\S+)");

    private final Cli cli;

    public FhrpReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Fhrp> instanceIdentifier,
                                      @NotNull FhrpBuilder fhrpBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String showCommand = f(SH_RUN_FHRP_DEFINITION, instanceIdentifier);
        final String fhrpOutput = blockingRead(showCommand, cli, instanceIdentifier, readContext);
        parseFhrp(fhrpOutput, fhrpBuilder);
    }

    @VisibleForTesting
    static void parseFhrp(String output, FhrpBuilder fhrpBuilder) {
        ParsingUtils.parseField(output, 0,
            DHRP_VERSION_CHUNK::matcher,
            matcher -> matcher.group("version"),
            s -> fhrpBuilder.setVersion(new VersionBuilder().setVrrp(getFhrpVrrp(s)).build()));
    }

    private static Vrrp getFhrpVrrp(final String name) {
        if (name.equalsIgnoreCase(Vrrp.V3.getName())) {
            return Vrrp.V3;
        } else {
            return Vrrp.V2;
        }
    }
}