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

package io.frinx.cli.unit.dasan.ifc.handler.l3ipvlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.L3ipvlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.l3ipvlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.l3ipvlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3ipvlanConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_INTERFACE_CFG = "show running-config interface %s";
    private static final Pattern NO_IP_REDIRECTS_LINE = Pattern.compile("^\\s*no ip redirects$");
    private Cli cli;

    public L3ipvlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> builder, @Nonnull final Config value) {
        ((L3ipvlanBuilder) builder).setConfig(value);
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
            @Nonnull final ConfigBuilder builder, @Nonnull final ReadContext ctx) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        parseInterface(blockingRead(f(SH_SINGLE_INTERFACE_CFG, name.replace("Vlan", "br")), cli, id, ctx), builder);
    }

    @VisibleForTesting
    static void parseInterface(final String output, final ConfigBuilder builder) {
        ParsingUtils.parseField(output, 0, NO_IP_REDIRECTS_LINE::matcher, matcher -> false, builder::setIpRedirects);
    }
}
