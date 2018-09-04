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
package io.frinx.cli.unit.dasan.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public  class VlanInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String SH_SINGLE_INTERFACE_CFG = "show running-config interface br%s";

    private static final Pattern NO_SHUTDOWN_LINE = Pattern.compile("^\\s*no shutdown$");

    private static final Pattern MTU_LINE = Pattern.compile("^\\s*mtu (?<mtu>\\d+)$");

    private Cli cli;

    public VlanInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                      @Nonnull final ConfigBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return;
        }
        parseInterface(blockingRead(f(SH_SINGLE_INTERFACE_CFG, matcher.group("id")), cli, id, ctx), builder, name);
    }

    @VisibleForTesting
    static void parseInterface(final String output, final ConfigBuilder builder, String name) {
        // Set disabled unless proven otherwise
        builder.setName(name);
        builder.setEnabled(false);
        builder.setType(L3ipvlan.class);

        ParsingUtils.parseField(output, 0,
                NO_SHUTDOWN_LINE::matcher,
            matcher -> true,
                builder::setEnabled);

        ParsingUtils.parseField(output, 0,
                MTU_LINE::matcher,
            matcher -> Integer.valueOf(matcher.group("mtu")),
                builder::setMtu);
    }
}
