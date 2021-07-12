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

package io.frinx.cli.unit.iosxe.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigReader;
import io.frinx.cli.unit.iosxe.ifc.Util;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class InterfaceConfigReader extends AbstractInterfaceConfigReader {

    public static final String SH_SINGLE_INTERFACE_CFG = "show running-config interface %s";

    public static final Pattern SHUTDOWN_LINE = Pattern.compile("shutdown");
    public static final Pattern DESCRIPTION_LINE = Pattern.compile("description (?<desc>.+)");
    private static final Pattern MTU_LINE = Pattern.compile("mtu (?<mtu>.+)");
    private static final Pattern STORM_CONTROL_LINE =
            Pattern.compile("storm-control (?<address>\\S+) level (?<level>(\\d|\\.)+).*");
    public static final Pattern NO_SNMP_TRAP_LINE = Pattern.compile("\\s*no snmp trap link-status");
    private static final Pattern MEDIA_TYPE_LINE = Pattern.compile("media-type (?<mediaType>.+)");
    private static final Pattern LLDP_TRANSMIT_LINE = Pattern.compile("no lldp transmit");
    private static final Pattern LLDP_RECEIVE_LINE = Pattern.compile("no lldp receive");
    private static final Pattern FHRP_MINIMUM_DELAY_LINE = Pattern.compile("fhrp delay minimum (?<seconds>.+)");
    private static final Pattern FHRP_RELOAD_DELAY_LINE = Pattern.compile("fhrp delay reload (?<seconds>.+)");
    private static final Pattern NO_IP_PROXY_ARP_LINE = Pattern.compile("\\s*no ip proxy-arp");
    private static final Pattern NO_IP_REDIRECTS_LINE = Pattern.compile("\\s*no ip redirects");
    private static final Pattern IPV6_ND_RA_LINE = Pattern.compile("\\s*ipv6 nd ra suppress (?<number>.+)");

    public InterfaceConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    public void parseInterface(String output, ConfigBuilder builder, String name) {
        super.parseInterface(output, builder, name);

        final IfCiscoExtAugBuilder ifCiscoExtAugBuilder = new IfCiscoExtAugBuilder();
        setStormControl(output, ifCiscoExtAugBuilder);
        setSnmpTrap(output, ifCiscoExtAugBuilder);
        setLldpTransmit(output, ifCiscoExtAugBuilder);
        setFhrpMinimumDelay(output, ifCiscoExtAugBuilder);
        setFhrpReloadDelay(output, ifCiscoExtAugBuilder);
        setIpRedirects(output, ifCiscoExtAugBuilder);
        setIpProxyArp(output, ifCiscoExtAugBuilder);
        setIpv6NdRa(output, ifCiscoExtAugBuilder);
        if (Util.isPhysicalInterface(builder.getType())) {
            setLldpReceive(output, ifCiscoExtAugBuilder);
        }
        if (isCiscoExtAugNotEmpty(ifCiscoExtAugBuilder)) {
            builder.addAugmentation(IfCiscoExtAug.class, ifCiscoExtAugBuilder.build());
        }

        final IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        setMode(output, ifSaosAugBuilder);
        if (isSaosAugNotEmpty(ifSaosAugBuilder)) {
            builder.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
        }
    }

    private void setSnmpTrap(String output, IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (NO_SNMP_TRAP_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setSnmpTrapLinkStatus(false);
        }
    }

    private void setLldpReceive(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (LLDP_RECEIVE_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setLldpReceive(false);
        }
    }

    private void setLldpTransmit(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (LLDP_TRANSMIT_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setLldpTransmit(false);
        }
    }

    private boolean isCiscoExtAugNotEmpty(final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        return !ifCiscoExtAugBuilder.build().equals((new IfCiscoExtAugBuilder()).build());
    }

    private Boolean isSaosAugNotEmpty(final IfSaosAugBuilder ifSaosAugBuilder) {
        return !ifSaosAugBuilder.build().equals((new IfSaosAugBuilder()).build());
    }

    private void setMode(final String output, final IfSaosAugBuilder ifSaosAugBuilder) {
        final Optional<String> mode = ParsingUtils.parseField(output, 0,
            MEDIA_TYPE_LINE::matcher,
            matcher -> matcher.group("mediaType"));

        if (mode.isPresent()) {
            final PhysicalType physicalType;
            switch (mode.get()) {
                case "auto-select":
                    physicalType = PhysicalType.Default;
                    break;
                case "rj45":
                    physicalType = PhysicalType.Rj45;
                    break;
                case "sfp":
                    physicalType = PhysicalType.Sfp;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse Mode value: " + mode.get());
            }
            ifSaosAugBuilder.setPhysicalType(physicalType);
        }
    }

    private void setStormControl(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        final List<StormControl> stormControls = new ArrayList<>();
        ParsingUtils.parseFields(output, 0,
            STORM_CONTROL_LINE::matcher,
            Matcher::group,
            m -> stormControls.add(parseStormControlLine(m)));

        if (!stormControls.isEmpty()) {
            ifCiscoExtAugBuilder.setStormControl(stormControls);
        }
    }

    private StormControl parseStormControlLine(final String line) {
        final Optional<String> address = ParsingUtils.parseField(line, 0,
            STORM_CONTROL_LINE::matcher,
            matcher -> matcher.group("address"));
        final Optional<String> level = ParsingUtils.parseField(line, 0,
            STORM_CONTROL_LINE::matcher,
            matcher -> matcher.group("level"));

        if (address.isPresent() && level.isPresent()) {
            final StormControlBuilder builder = new StormControlBuilder();
            builder.setAddress(Util.getStormControlAddress(address.get()));
            builder.setKey(new StormControlKey(builder.getAddress()));
            builder.setLevel(new BigDecimal(level.get()));
            return builder.build();
        }

        return null;
    }

    private void setFhrpMinimumDelay(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        final Optional<String> delay = ParsingUtils.parseField(output, 0,
            FHRP_MINIMUM_DELAY_LINE::matcher,
            matcher -> matcher.group("seconds"));
        delay.ifPresent(s -> ifCiscoExtAugBuilder.setFhrpMinimumDelay(Integer.parseInt(s)));
    }

    private void setFhrpReloadDelay(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        final Optional<String> delay = ParsingUtils.parseField(output, 0,
            FHRP_RELOAD_DELAY_LINE::matcher,
            matcher -> matcher.group("seconds"));
        delay.ifPresent(s -> ifCiscoExtAugBuilder.setFhrpReloadDelay(Integer.parseInt(s)));
    }

    private void setIpRedirects(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (NO_IP_REDIRECTS_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setIpRedirects(false);
        }
    }

    private void setIpProxyArp(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        if (NO_IP_PROXY_ARP_LINE.matcher(output).find()) {
            ifCiscoExtAugBuilder.setIpProxyArp(false);
        }
    }

    private void setIpv6NdRa(final String output, final IfCiscoExtAugBuilder ifCiscoExtAugBuilder) {
        ParsingUtils.parseField(output, 0,
            IPV6_ND_RA_LINE::matcher,
            matcher -> matcher.group("number"),
            ifCiscoExtAugBuilder::setIpv6NdRaSuppress);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_SINGLE_INTERFACE_CFG, ifcName);
    }

    @Override
    protected Pattern getShutdownLine() {
        return SHUTDOWN_LINE;
    }

    @Override
    protected Pattern getMtuLine() {
        return MTU_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESCRIPTION_LINE;
    }

    @Override
    public Class<? extends InterfaceType> parseType(String name) {
        return Util.parseType(name);
    }

}
