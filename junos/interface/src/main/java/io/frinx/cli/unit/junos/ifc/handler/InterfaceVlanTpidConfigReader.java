/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.junos.ifc.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceVlanTpidConfigReader implements CliConfigReader<Config1, Config1Builder> {

    public static final Pattern VLAN_TAGGING_LINE = Pattern.compile("set interfaces (?<id>\\S+) (?<tpid>vlan-tagging)");

    private Cli cli;

    public InterfaceVlanTpidConfigReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public Config1Builder getBuilder(@NotNull InstanceIdentifier<Config1> id) {
        return new Config1Builder();
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config1> id, @NotNull Config1Builder builder,
        @NotNull ReadContext ctx) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        parseTagTypes(blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, name), cli, id, ctx), builder,
            name);
    }

    private void parseTagTypes(String output, Config1Builder builder, String name) {
        ParsingUtils
            .NEWLINE
            .splitAsStream(output)
            .map(String::trim)
            .map(VLAN_TAGGING_LINE::matcher)
            .filter(Matcher::matches)
            .findFirst().map(m -> m.group("tpid"))
            .ifPresent(s -> builder.setTpid(TPID0X8100.class));
    }
}