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

package io.frinx.cli.unit.saos8.ifc.handler.port;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final String SH_SINGLE_INTERFACE_CFG = "configuration search string \"port %s\"";
    private static final String SH_TYPE = "configuration search string \"aggregation create agg %s\"";

    private Cli cli;

    public PortConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                      @NotNull final ConfigBuilder builder,
                                      @NotNull final ReadContext ctx) throws ReadFailedException {
        if (isPort(id, ctx)) {
            String ifcName = id.firstKeyOf(Interface.class).getName();
            parseInterface(blockingRead(f(SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx), builder, ifcName);
            parseType(blockingRead(f(SH_TYPE, ifcName), cli, id, ctx), builder, ifcName);
        }
    }

    @VisibleForTesting
    void parseInterface(final String output, final ConfigBuilder builder, String name)  {
        parseEnabled(output, builder, name);
        builder.setName(name);

        setMtu(output, builder, name);
        setDescription(output, builder, name);
        IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        setNegotiationAuto(output, ifSaosAugBuilder, name);
        setSpeedType(output, ifSaosAugBuilder, name);
        builder.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
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

    private void setSpeedType(final String output, final IfSaosAugBuilder ifSaosAugBuilder, String name) {
        Pattern speedType = Pattern.compile("port set port " + name + " speed (?<speedType>\\S+).*");
        Optional<String> speedTypeValue = ParsingUtils.parseField(output, 0,
            speedType::matcher,
            matcher -> matcher.group("speedType"));

        if (speedTypeValue.isPresent()) {
            SaosIfExtensionConfig.SpeedType speed;
            switch (speedTypeValue.get()) {
                case "auto":
                    speed = SaosIfExtensionConfig.SpeedType.Auto;
                    break;
                case "ten":
                    speed = SaosIfExtensionConfig.SpeedType.Ten;
                    break;
                case "hundred":
                    speed = SaosIfExtensionConfig.SpeedType.Hundred;
                    break;
                case "gig":
                    speed = SaosIfExtensionConfig.SpeedType.Gigabit;
                    break;
                case "gigabit":
                    speed = SaosIfExtensionConfig.SpeedType.Gigabit;
                    break;
                case "ten-gig":
                    speed = SaosIfExtensionConfig.SpeedType.TenGig;
                    break;
                case "forty-gig":
                    speed = SaosIfExtensionConfig.SpeedType.FortyGig;
                    break;
                case "hundred-gig":
                    speed = SaosIfExtensionConfig.SpeedType.HundredGig;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse speed-type value: " + speedTypeValue.get());
            }
            ifSaosAugBuilder.setSpeedType(speed);
        }
    }

    private void setNegotiationAuto(final String output, final IfSaosAugBuilder ifSaosAugBuilder, String name) {
        Pattern autoNegOn = Pattern.compile("port set port " + name + " .*auto-neg on.*");
        ifSaosAugBuilder.setNegotiationAuto(false);
        ParsingUtils.parseField(output, 0,
            autoNegOn::matcher,
            matcher -> true,
            autoNeg -> ifSaosAugBuilder.setNegotiationAuto(true));
    }

    private boolean isPort(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.checkCachedIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}