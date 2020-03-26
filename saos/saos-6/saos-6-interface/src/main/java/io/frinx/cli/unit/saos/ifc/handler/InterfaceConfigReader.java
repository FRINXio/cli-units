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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_INTERFACE_CFG = "configuration search string \"port %s \"";

    private static final String PORT_DISABLE = "port disable port %s";
    private static final Pattern PORT_MTU = Pattern.compile("port set port.*max-frame-size (?<mtu>\\d+).*");
    private static final Pattern PORT_DESCRIPTION_LONG =
            Pattern.compile("port set port.*description \"(?<desc>\\S+.*)\".*");
    private static final Pattern PORT_DESCRIPTION_SHORT =
            Pattern.compile("port set port.*description (?<desc>\\S+).*");
    private static final Pattern PORT_MODE = Pattern.compile("port set port.*mode (?<mode>\\S+).*");
    private static final Pattern PORT_AFT = Pattern.compile("port set port.*acceptable-frame-type (?<aft>\\S+).*");
    private static final Pattern PORT_VIF = Pattern.compile("port set port.*vs-ingress-filter (?<vif>\\S+).*");
    private static final Pattern PORT_VLAN = Pattern.compile("vlan add vlan (?<vlanID>\\S+).*");
    private static final Pattern VC_VEP =
            Pattern.compile("virtual-circuit ethernet.*vlan-ethertype-policy (?<vep>\\S+).*");
    private static final Pattern PORT_ITEQ = Pattern.compile("port set port.*ingress-to-egress-qmap NNI-NNI.*");
    private static final Pattern MAX_MACS = Pattern.compile(".*max-dynamic-macs (?<macs>\\d+).*");
    private static final Pattern FORWARD_UNLEARNED = Pattern.compile(".*forward-unlearned (?<learning>\\S+)");

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
    }

    @VisibleForTesting
    void parseInterface(final String output, final ConfigBuilder builder, String name)  {
        parseEnabled(output, builder, name);
        builder.setName(name);
        builder.setType(EthernetCsmacd.class);

        ParsingUtils.parseField(output,
            PORT_MTU::matcher,
            matcher -> Integer.valueOf(matcher.group("mtu")),
            builder::setMtu);

        setDescription(output, builder);

        IfSaosAugBuilder ifSaosAugBuilder = new IfSaosAugBuilder();
        setMode(output, ifSaosAugBuilder);
        setAcceptableFrameType(output, ifSaosAugBuilder);
        setIngressVSFilter(output, ifSaosAugBuilder);
        setVlanEthertypePolicy(output, ifSaosAugBuilder);
        setVlanIds(output, ifSaosAugBuilder);
        setIngressToEgressQmap(output, ifSaosAugBuilder);

        setAccessControlAttributes(output, ifSaosAugBuilder);

        builder.addAugmentation(IfSaosAug.class, ifSaosAugBuilder.build());
    }

    @VisibleForTesting
    static IfSaosAug setAccessControlAttributes(String output, IfSaosAugBuilder ifSaosAugBuilder) {
        if (output.contains("flow access-control")) {
            ParsingUtils.parseFields(output, 0,
                MAX_MACS::matcher,
                m -> m.group("macs"),
                s -> ifSaosAugBuilder.setMaxDynamicMacs(Integer.parseInt(s)));

            // when group learning is not present, forward_unlearning si on
            ifSaosAugBuilder.setForwardUnlearned(true);

            ParsingUtils.parseFields(output, 0,
                FORWARD_UNLEARNED::matcher,
                m -> m.group("learning"),
                s -> ifSaosAugBuilder.setForwardUnlearned(false));
        }
        return ifSaosAugBuilder.build();
    }

    private void setIngressToEgressQmap(String output, IfSaosAugBuilder ifSaosAugBuilder) {
        ParsingUtils.parseField(output, PORT_ITEQ::matcher, matcher -> true,
            iteq -> ifSaosAugBuilder.setIngressToEgressQmap(IngressToEgressQmap.NNINNI));
    }

    private void setDescription(String output, ConfigBuilder builder) {
        if (output.contains("\"")) {
            ParsingUtils.parseField(output,
                PORT_DESCRIPTION_LONG::matcher,
                matcher -> matcher.group("desc"),
                builder::setDescription);
        } else {
            ParsingUtils.parseField(output,
                PORT_DESCRIPTION_SHORT::matcher,
                matcher -> matcher.group("desc"),
                builder::setDescription);
        }
    }

    private void setVlanIds(String output, IfSaosAugBuilder ifSaosAugBuilder) {
        List<String> vlanIdFromRead = ParsingUtils.parseFields(output, 0,
            PORT_VLAN::matcher,
            matcher -> matcher.group("vlanID"),
            value -> value);

        if (!vlanIdFromRead.isEmpty()) {
            List<String> vlanId = new ArrayList<>();
            vlanIdFromRead.forEach(vlanIdRow -> vlanId.addAll(Arrays.asList(vlanIdRow.split(","))));
            ifSaosAugBuilder.setVlanIds(vlanId);
        }
    }

    private void setVlanEthertypePolicy(String output, IfSaosAugBuilder ifSaosAugBuilder) {
        Optional<String> vep = ParsingUtils.parseField(output, 0,
            VC_VEP::matcher,
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

    private void setIngressVSFilter(String output, IfSaosAugBuilder ifSaosAugBuilder) {
        Optional<String> ingressFilter = ParsingUtils.parseField(output, 0,
            PORT_VIF::matcher,
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

    private void setAcceptableFrameType(String output, IfSaosAugBuilder ifSaosAugBuilder) {
        Optional<String> aft = ParsingUtils.parseField(output, 0,
            PORT_AFT::matcher,
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

    private void setMode(String output, IfSaosAugBuilder ifSaosAugBuilder) {
        Optional<String> mode = ParsingUtils.parseField(output, 0,
            PORT_MODE::matcher,
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

    protected void parseEnabled(final String output, final ConfigBuilder builder, String name) {
        // Set enabled unless proven otherwise
        builder.setEnabled(true);

        if (output.contains(f(PORT_DISABLE, name))) {
            builder.setEnabled(false);
        }
    }
}