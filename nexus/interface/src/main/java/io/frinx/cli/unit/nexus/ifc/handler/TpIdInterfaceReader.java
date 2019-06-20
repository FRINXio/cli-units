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

package io.frinx.cli.unit.nexus.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8A88;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPIDTYPES;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TpIdInterfaceReader implements CliConfigReader<Config1, Config1Builder> {

    private static final Pattern SWITCHPORT_LINE = Pattern.compile("\\s*ethertype (?<tpid>.+)");

    private Cli cli;

    public TpIdInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id, @Nonnull Config1Builder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        parseTpId(blockingRead(String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx),
                builder);

    }

    @VisibleForTesting
    static void parseTpId(String output, Config1Builder builder) {

        Matcher matcher = SWITCHPORT_LINE.matcher(output);

        if (matcher.find()) {
            builder.setTpid(parseId(matcher.group("tpid")));
        }

    }

    @VisibleForTesting
    private static Class<? extends TPIDTYPES> parseId(String string) {

        // 88a8 on device == 8A88
        if (string.toLowerCase()
                .equals("0x88a8")) {

            return TPID0X8A88.class;
        } else {
            // Other tags are unsupported
            // This also applies to some Ironware devices where tag-type command has different syntax
            return null;
        }

    }
}
