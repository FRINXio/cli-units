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
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.us.conn.config.UsConn;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.us.conn.config.UsConnBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.us.conn.config.UsConnKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceRpdUsConnConfigReader implements CliConfigListReader<UsConn, UsConnKey, UsConnBuilder> {

    private static final Pattern US_CONN_ID_LINE = Pattern.compile(".*us-conn (?<usConnId>\\d+).*");

    private final Cli cli;

    public InterfaceRpdUsConnConfigReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<UsConnKey> getAllIds(@NotNull InstanceIdentifier<UsConn> instanceIdentifier,
                                     @NotNull ReadContext readContext) throws ReadFailedException {
        List<UsConnKey> ids = new ArrayList<>();
        final String rpdName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (rpdName.contains("rpd")) {
            String output = blockingRead(f(InterfaceRpdConfigReader.SH_CABLE_UP, rpdName),
                    cli, instanceIdentifier, readContext);

            ids = ParsingUtils.parseFields(output, 0,
                    US_CONN_ID_LINE::matcher,
                    matcher ->  Integer.valueOf(matcher.group("usConnId")),
                    UsConnKey::new);

        }

        return ids;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<UsConn> instanceIdentifier,
                                      @NotNull UsConnBuilder usConnBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String rpdName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Integer usConnId = instanceIdentifier.firstKeyOf(UsConn.class).getId();
        if (rpdName.contains("rpd")) {
            String output = blockingRead(f(InterfaceRpdConfigReader.SH_CABLE_UP, rpdName),
                    cli, instanceIdentifier, readContext);

            parseUsConn(output, usConnId, usConnBuilder);
        }
    }

    @VisibleForTesting
    static void parseUsConn(String connectorsOutput, Integer usConnId, UsConnBuilder usConnBuilder) {
        usConnBuilder.setId(usConnId);

        ParsingUtils.parseField(connectorsOutput,
                Pattern.compile(
                        String.format("us-conn %d base-target-power (?<baseTargetPower>.+)", usConnId))::matcher,
                matcher -> Short.valueOf(matcher.group("baseTargetPower")),
                usConnBuilder::setBaseTargetPower);
    }
}