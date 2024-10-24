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

package io.frinx.cli.unit.saos6.system.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.CienaServerAuthenticationAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.CienaServerAuthenticationAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.CienaSystemAuthenticationExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.auth.keys.top.ntp.keys.ntp.key.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.auth.keys.top.ntp.keys.ntp.key.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpAuthenticationStateReader implements CliOperReader<State, StateBuilder> {

    private final Cli cli;

    public NtpAuthenticationStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> id,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var builder = new CienaServerAuthenticationAugBuilder();
        parseKeyState(stateBuilder, builder,
                blockingRead(f(NtpAuthenticationReader.SH_NTP_AUTH_KEY), cli, id, readContext));
        stateBuilder.addAugmentation(CienaServerAuthenticationAug.class, builder.build());
    }

    public static void parseKeyState(@NotNull StateBuilder stateBuilder,
                                      CienaServerAuthenticationAugBuilder builder,
                                      String output) {
        ParsingUtils.parseFields(output, 0,
            NtpAuthenticationReader.PARSE_KEY::matcher,
            matcher -> matcher.group("keyId"),
            v -> stateBuilder.setKeyId(Integer.valueOf(v)));

        ParsingUtils.parseFields(output, 0,
            NtpAuthenticationReader.PARSE_KEY::matcher,
            matcher -> matcher.group("keyType"),
            v -> builder.setKeyType(CienaSystemAuthenticationExtension.KeyType.valueOf(v.toUpperCase(Locale.ROOT))));
    }
}