/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_INTERFACE_CFG = "configuration search string \"port %s\"";
    private static final String SH_TYPE = "configuration search string \"aggregation create agg %s\"";

    private Cli cli;

    public InterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                      @Nonnull final ConfigBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        parseInterface(blockingRead(f(SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx), builder, ifcName);
        parseType(blockingRead(f(SH_TYPE, ifcName), cli, id, ctx), builder, ifcName);
    }

    @VisibleForTesting
    void parseInterface(final String output, final ConfigBuilder builder, String name)  {
        parseEnabled(output, builder, name);
        builder.setName(name);

        setMtu(output, builder, name);
        setDescription(output, builder, name);
    }

    @VisibleForTesting
    void parseType(final String output, ConfigBuilder builder, String name) {
        builder.setType(EthernetCsmacd.class);

        Pattern agg = Pattern.compile("aggregation create agg " + name + "$");

        ParsingUtils.NEWLINE.splitAsStream(output)
            .map(String::trim)
            .map(agg::matcher)
            .filter(Matcher::matches)
            .skip(0)
            .findFirst().ifPresent(v -> builder.setType(Ieee8023adLag.class));
    }

    private void setMtu(final String output, ConfigBuilder builder, String name) {
        Pattern portMtu = Pattern.compile("port set port " + name + " .*max-frame-size (?<mtu>\\d+).*");

        ParsingUtils.parseField(output,
            portMtu::matcher,
            matcher -> Integer.valueOf(matcher.group("mtu")),
            builder::setMtu);
    }

    private void setDescription(String output, ConfigBuilder builder, String name) {
        if (output.contains("\"")) {
            Pattern portDescLong = Pattern.compile("port set port " + name + " .*description \"(?<desc>\\S+.*)\".*");
            ParsingUtils.parseField(output,
                portDescLong::matcher,
                matcher -> matcher.group("desc"),
                builder::setDescription);
        } else {
            Pattern portDescShort = Pattern.compile("port set port " + name + " .*description (?<desc>\\S+).*");
            ParsingUtils.parseField(output,
                portDescShort::matcher,
                matcher -> matcher.group("desc"),
                builder::setDescription);
        }
    }

    private void parseEnabled(final String output, final ConfigBuilder builder, String name) {
        Pattern portEnabled = Pattern.compile("port disable port " + name + "$");
        builder.setEnabled(true);
        ParsingUtils.parseField(output, 0,
            portEnabled::matcher,
            matcher -> true,
            mode -> builder.setEnabled(false));
    }
}