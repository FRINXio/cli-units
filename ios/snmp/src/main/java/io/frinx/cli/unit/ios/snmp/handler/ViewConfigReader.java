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

package io.frinx.cli.unit.ios.snmp.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.snmp.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.config.Mib;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.config.MibBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.config.MibKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.View;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.view.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.view.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ViewConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public ViewConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(View.class).getName();
        final String output = blockingRead(f(ViewReader.SHOW_SNMP_VIEW, name), cli, instanceIdentifier, readContext);
        configBuilder.setName(name);
        configBuilder.setMib(getMibs(output));
    }

    public static List<Mib> getMibs(final String output) {
        final List<Mib> mibs = new ArrayList<>();
        final String[] lines = output.split(String.valueOf(ParsingUtils.NEWLINE));

        for (String line : lines) {
            final MibBuilder mibBuilder = new MibBuilder();
            final Optional<String> mibName = getMibName(line);
            final Optional<String> inclusion = getInclusionName(line);
            if (mibName.isPresent() && inclusion.isPresent()) {
                mibBuilder.setKey(new MibKey(mibName.get()));
                mibBuilder.setName(mibName.get());
                mibBuilder.setInclusion(Util.getInclusionType(inclusion.get()));
                mibs.add(mibBuilder.build());
            }
        }

        return mibs;
    }

    private static Optional<String> getMibName(final String line) {
        return ParsingUtils.parseField(line, 0, ViewReader.VIEW_LINE::matcher,
            m -> m.group("mib"));
    }

    private static Optional<String> getInclusionName(final String line) {
        return ParsingUtils.parseField(line, 0, ViewReader.VIEW_LINE::matcher,
            m -> m.group("inclusion"));
    }
}