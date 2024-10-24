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
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ds.conn.config.DsConn;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ds.conn.config.DsConnBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.ds.conn.config.DsConnKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceRpdDsConnConfigReader implements CliConfigListReader<DsConn, DsConnKey, DsConnBuilder> {

    private static final Pattern DS_CONN_ID_LINE = Pattern.compile(".*ds-conn (?<dsConnId>\\d+).*");

    private final Cli cli;

    public InterfaceRpdDsConnConfigReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<DsConnKey> getAllIds(@NotNull InstanceIdentifier<DsConn> instanceIdentifier,
                                     @NotNull ReadContext readContext) throws ReadFailedException {
        List<DsConnKey> ids = new ArrayList<>();
        final String rpdName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (rpdName.contains("rpd")) {
            String output = blockingRead(f(InterfaceRpdConfigReader.SH_CABLE_UP, rpdName),
                    cli, instanceIdentifier, readContext);

            ids = ParsingUtils.parseFields(output, 0,
                    DS_CONN_ID_LINE::matcher,
                    matcher ->  Integer.valueOf(matcher.group("dsConnId")),
                    DsConnKey::new);

        }

        return ids;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<DsConn> instanceIdentifier,
                                      @NotNull DsConnBuilder dsConnBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String rpdName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Integer dsConnId = instanceIdentifier.firstKeyOf(DsConn.class).getId();
        if (rpdName.contains("rpd")) {
            String output = blockingRead(f(InterfaceRpdConfigReader.SH_CABLE_UP, rpdName),
                    cli, instanceIdentifier, readContext);

            parseDsConn(output, dsConnId, dsConnBuilder);
        }
    }

    @VisibleForTesting
    static void parseDsConn(String connectorsOutput, Integer dsConnId, DsConnBuilder dsConnBuilder) {
        dsConnBuilder.setId(dsConnId);

        ParsingUtils.parseField(connectorsOutput,
                Pattern.compile(String.format("ds-conn %d power-level (?<powerLevel>\\d+)", dsConnId))::matcher,
                matcher -> Integer.valueOf(matcher.group("powerLevel")),
                dsConnBuilder::setPowerLevel);

        List<String> dsGroups = ParsingUtils.parseNonDistinctFields(connectorsOutput, 0,
                Pattern.compile(String.format("ds-conn %d ds-group (?<dsGroup>.+)", dsConnId))::matcher,
                matcher -> matcher.group("dsGroup"),
                Function.identity());
        dsConnBuilder.setDsGroup(dsGroups);
    }
}