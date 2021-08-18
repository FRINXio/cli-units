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

package io.frinx.cli.unit.saos.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.AcceptableFrameType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.IngressToEgressQmap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.VlanEthertypePolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_SINGLE_INTERFACE_CFG = "configuration search string \"port %s\"";
    private static final String SH_TYPE = "configuration search string \"aggregation create agg %s\"";

    private final Cli cli;

    public InterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                      @Nonnull final ConfigBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        parseType(blockingRead(f(SH_TYPE, ifcName), cli, id, ctx), builder, ifcName);
        IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        parseInterface(blockingRead(f(SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx),
                builder, ifSaosAugBuilder, ifcName);
        parseLogicalInterface(blockingRead(f(InterfaceReader.LOGICAL_INTERFACE, ifcName), cli, id, ctx),
                builder, ifSaosAugBuilder, ifcName);
        builder.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
    }

    @VisibleForTesting
    void parseLogicalInterface(final String output, ConfigBuilder builder,
                               IfSaosAugBuilder ifSaosAugBuilder, String name) {
        builder.setName(name);
        if (name.equals("remote")) {
            setRemoteDescription(output, builder);
        }
        setIpvAddressAndMask(output, name, ipv4 -> !ipv4.contains(":"),
            ipv4 -> ifSaosAugBuilder.setIpv4Prefix(new Ipv4Prefix(ipv4)));
        setIpvAddressAndMask(output, name, ipv6 -> ipv6.contains(":"),
            ipv6 -> ifSaosAugBuilder.setIpv6Prefix(new Ipv6Prefix(ipv6)));
    }

    @VisibleForTesting
    void parseInterface(final String output, final ConfigBuilder builder,
                        IfSaosAugBuilder ifSaosAugBuilder, String name)  {
        parseEnabled(output, builder, name);
        builder.setName(name);

        setMtu(output, builder, name);
        setDescription(output, builder, name);
        setMode(output, ifSaosAugBuilder, name);
        setAcceptableFrameType(output, ifSaosAugBuilder, name);
        setIngressVSFilter(output, ifSaosAugBuilder, name);
        setVlanEthertypePolicy(output, ifSaosAugBuilder, name);
        setIngressToEgressQmap(output, ifSaosAugBuilder, name);
        setAccessControlAttributes(output, ifSaosAugBuilder, name);
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

    private void setDescription(final String output, ConfigBuilder builder, String name) {
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

    private void setMode(final String output, IfSaosAugBuilder ifSaosAugBuilder, String name) {
        Pattern portMode = Pattern.compile("port set port " + name + " .*mode (?<mode>\\S+).*");
        Optional<String> mode = ParsingUtils.parseField(output, 0,
            portMode::matcher,
            matcher -> matcher.group("mode"));

        if (mode.isPresent()) {
            PhysicalType physicalType;
            switch (mode.get()) {
                case "default":
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

    private void setAcceptableFrameType(final String output, IfSaosAugBuilder ifSaosAugBuilder, String name) {
        Pattern portAft = Pattern.compile("port set port " + name + " .*acceptable-frame-type (?<aft>\\S+).*");
        Optional<String> aft = ParsingUtils.parseField(output, 0,
            portAft::matcher,
            matcher -> matcher.group("aft"));

        if (aft.isPresent()) {
            AcceptableFrameType acceptableFrameType;
            switch (aft.get()) {
                case "all":
                    acceptableFrameType = AcceptableFrameType.All;
                    break;
                case "tagged-only":
                    acceptableFrameType = AcceptableFrameType.TaggedOnly;
                    break;
                case "untagged-only":
                    acceptableFrameType = AcceptableFrameType.UntaggedOnly;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse Acceptable Frame Type value: " + aft.get());
            }
            ifSaosAugBuilder.setAcceptableFrameType(acceptableFrameType);
        }
    }

    private void setIngressVSFilter(final String output, IfSaosAugBuilder ifSaosAugBuilder, String name) {
        Pattern portVif = Pattern.compile("port set port " + name + " .*vs-ingress-filter (?<vif>\\S+).*");
        Optional<String> ingressFilter = ParsingUtils.parseField(output, 0,
            portVif::matcher,
            matcher -> matcher.group("vif"));

        if (ingressFilter.isPresent()) {
            boolean value;
            switch (ingressFilter.get()) {
                case "on":
                    value = true;
                    break;
                case "off":
                    value = false;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse Ingress VS Filter value: " + ingressFilter.get());
            }
            ifSaosAugBuilder.setVsIngressFilter(value);
        }
    }

    private void setVlanEthertypePolicy(final String output, IfSaosAugBuilder ifSaosAugBuilder, String name) {
        Pattern vcVep = Pattern.compile("virtual-circuit ethernet set port "
                + name + " vlan-ethertype-policy (?<vep>\\S+).*");
        Optional<String> vep = ParsingUtils.parseField(output, 0,
            vcVep::matcher,
            matcher -> matcher.group("vep"));

        if (vep.isPresent()) {
            VlanEthertypePolicy vlanEthertypePolicy;
            switch (vep.get()) {
                case "all":
                    vlanEthertypePolicy = VlanEthertypePolicy.All;
                    break;
                case "vlan-tpid":
                    vlanEthertypePolicy = VlanEthertypePolicy.VlanTpid;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse Vlan Ethertype Policy value: " + vep.get());
            }
            ifSaosAugBuilder.setVlanEthertypePolicy(vlanEthertypePolicy);
        }
    }

    private void setIngressToEgressQmap(final String output, IfSaosAugBuilder ifSaosAugBuilder, String name) {
        Pattern portIteq = Pattern.compile("port set port " + name + " .*ingress-to-egress-qmap NNI-NNI.*");
        ParsingUtils.parseField(output,
            portIteq::matcher,
            matcher -> true,
            iteq -> ifSaosAugBuilder.setIngressToEgressQmap(IngressToEgressQmap.NNINNI));
    }

    private void setAccessControlAttributes(final String output, IfSaosAugBuilder ifSaosAugBuilder, String name) {
        Pattern maxMacs = Pattern.compile("flow access-control set port " + name + " max-dynamic-macs (?<macs>\\d+).*");
        Pattern unlearned = Pattern.compile("flow access-control set port " + name + " .*forward-unlearned.*");

        ParsingUtils.parseFields(output, 0,
            maxMacs::matcher,
            m -> m.group("macs"),
            s -> ifSaosAugBuilder.setMaxDynamicMacs(Integer.parseInt(s)));

        ParsingUtils.parseFields(output, 0,
            unlearned::matcher,
            matcher -> true,
            s -> ifSaosAugBuilder.setForwardUnlearned(false));
    }

    protected void parseEnabled(final String output, final ConfigBuilder builder, String name) {
        Pattern portEnabled = Pattern.compile("port disable port " + name + "$");
        builder.setEnabled(true);
        ParsingUtils.parseField(output, 0,
            portEnabled::matcher,
            matcher -> true,
            mode -> builder.setEnabled(false));
    }

    private void setRemoteDescription(final String output, ConfigBuilder builder) {
        Pattern description = Pattern.compile("\\| remote\\s+\\| (?<description>.*)\\s+\\|.*\\|");
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(description::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("description"))
                .findFirst()
                .map(desc -> desc.replaceAll("\\s+", ""))
                .ifPresent(builder::setDescription);
    }

    private void setIpvAddressAndMask(final String output, String name, Predicate<String> predicate,
                                      Consumer<String> consumer) {
        Pattern ipv4Prefix = Pattern.compile("\\|\\s(?<id>" + name + ")\\s+\\|.*\\|\\s(?<ipvType>\\S+).*");
        ParsingUtils.NEWLINE.splitAsStream(output)
                .map(ipv4Prefix::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("ipvType"))
                .filter(predicate)
                .findFirst()
                .ifPresent(consumer);
    }
}