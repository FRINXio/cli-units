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

package io.frinx.cli.unit.saos8.system.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.auth.keys.top.ntp.keys.NtpKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.auth.keys.top.ntp.keys.NtpKeyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.auth.keys.top.ntp.keys.NtpKeyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpAuthenticationReader implements CliOperListReader<NtpKey, NtpKeyKey, NtpKeyBuilder> {

    private final Cli cli;

    public static final String SH_NTP_AUTH_KEY = "ntp client show keys";
    public static final Pattern PARSE_KEY = Pattern.compile("\\| (?<keyId>\\d+) *\\| (?<keyType>\\S+) *\\|");

    public NtpAuthenticationReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<NtpKey> id,
                                      @NotNull NtpKeyBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        builder.setKeyId(id.firstKeyOf(NtpKey.class).getKeyId());
    }

    @NotNull
    @Override
    public List<NtpKeyKey> getAllIds(@NotNull InstanceIdentifier<NtpKey> id,
                                     @NotNull ReadContext context) throws ReadFailedException {
        var keyIds = parseAllKeyIds(blockingRead(SH_NTP_AUTH_KEY, cli, id, context));
        return keyIds;
    }

    public static List<NtpKeyKey> parseAllKeyIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_KEY::matcher,
            matcher -> matcher.group("keyId"),
            v -> new NtpKeyKey(Integer.parseInt(v)));
    }
}