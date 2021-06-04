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

package io.frinx.cli.unit.iosxe.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalAfiSafiConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_AUTO_SUMMARY = "show running-config "
            + "| include ^router bgp"
            + "|^  (auto-summary|no auto-summary)"
            + "|^ *address-family"
            + "|^  redistribute (connected|static)"
            + "|^  default-information originate"
            + "|^  synchronization"
            + "|^ exit-address-family";

    private static final Pattern AUTO_SUMMARY_PATTERN = Pattern.compile("auto-summary");
    private static final Pattern REDISTRIBUTE_CON_PATTERN = Pattern.compile("redistribute connected");
    private static final Pattern REDISTRIBUTE_STAT_PATTERN = Pattern.compile("redistribute static");
    private static final Pattern DEFAULT_INFORMATION_PATTERN = Pattern.compile("default-information originate");
    private static final Pattern SYNCHRONIZATION_PATTERN = Pattern.compile("synchronization");

    private Cli cli;

    public GlobalAfiSafiConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> afiSafiName = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        String output = blockingRead(SH_AUTO_SUMMARY, cli, instanceIdentifier, readContext);

        parseConfig(output, vrfKey, afiSafiName, configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, NetworkInstanceKey vrfKey, Class<? extends AFISAFITYPE> afiSafiName,
                            ConfigBuilder configBuilder) {
        GlobalAfiSafiConfigAugBuilder augBuilder = new GlobalAfiSafiConfigAugBuilder();
        configBuilder.setAfiSafiName(afiSafiName);

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK) && afiSafiName.equals(IPV4UNICAST.class)) {
            setAutoSummary(augBuilder, output);
        } else if (afiSafiName.equals(IPV4UNICAST.class)) {
            setRedistribute(augBuilder, output, vrfKey.getName());
        }

        configBuilder.addAugmentation(GlobalAfiSafiConfigAug.class, augBuilder.build());
    }

    private static void setAutoSummary(GlobalAfiSafiConfigAugBuilder builder, String output) {
        // default value
        builder.setAutoSummary(false);

        ParsingUtils.parseField(output, 0,
            AUTO_SUMMARY_PATTERN::matcher,
            matcher -> matcher.find(),
            value -> builder.setAutoSummary(true));
    }

    static void setRedistribute(GlobalAfiSafiConfigAugBuilder builder, String output, String vrfKey) {
        Pattern pattern = Pattern.compile("address-family.*|redistribute connected|redistribute static|"
                + "default-information originate|synchronization|exit-address-family");
        Pattern startPattern = Pattern.compile("address-family ipv4 vrf " + vrfKey);
        Pattern endPattern = Pattern.compile("exit-address-family");

        // default values
        builder.setRedistributeConnected(false);
        builder.setRedistributeStatic(false);
        builder.setDefaultInformationOriginate(false);
        builder.setSynchronization(false);

        List<String> lines = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(Matcher::group)
                .collect(Collectors.toList());

        boolean run = false;
        for (String line : lines) {
            if (run) {
                if (REDISTRIBUTE_CON_PATTERN.matcher(line).matches()) {
                    builder.setRedistributeConnected(true);
                }
                if (REDISTRIBUTE_STAT_PATTERN.matcher(line).matches()) {
                    builder.setRedistributeStatic(true);
                }
                if (DEFAULT_INFORMATION_PATTERN.matcher(line).matches()) {
                    builder.setDefaultInformationOriginate(true);
                }
                if (SYNCHRONIZATION_PATTERN.matcher(line).matches()) {
                    builder.setSynchronization(true);
                }
            }
            if (startPattern.matcher(line).matches()) {
                run = true;
            }
            if (endPattern.matcher(line).matches()) {
                run = false;
            }
        }
    }
}