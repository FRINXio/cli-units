/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.rpd;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.top.rpd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rpd.extension.rev230123.rpd.top.rpd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceRpdConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_CABLE_UP = "show running-config interface %s";
    private static final Pattern RPD_INDEX_LINE = Pattern.compile(".*rpd-index (?<rpdIndex>\\d+)");
    private static final Pattern UCAM_DCAM_LINE = Pattern.compile(".*ucam (?<ucam>\\d+) dcam (?<dcam>\\d+)");
    private static final Pattern MAC_ADDRESS_LINE = Pattern.compile(".*mac-address (?<macAddress>.+)");
    private static final Pattern ADP_ENABLE_LINE = Pattern.compile(".*adp enable");
    private static final Pattern ENABLE_LINE = Pattern.compile(".*no shutdown");
    private static final Pattern SSD_ENABLE_LINE = Pattern.compile(".*ssd-enable");

    private final Cli cli;

    public InterfaceRpdConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String rpdName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        if (rpdName.contains("rpd")) {
            String output = blockingRead(f(SH_CABLE_UP, rpdName), cli, instanceIdentifier, readContext);
            parseConfig(output, configBuilder);
        }
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, RPD_INDEX_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("rpdIndex")),
                configBuilder::setRpdIndex);

        ParsingUtils.parseField(output, UCAM_DCAM_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("ucam")),
                configBuilder::setUcam);

        ParsingUtils.parseField(output, UCAM_DCAM_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("dcam")),
                configBuilder::setDcam);

        ParsingUtils.parseField(output, MAC_ADDRESS_LINE::matcher,
                matcher -> matcher.group("macAddress"),
                configBuilder::setMacAddress);

        configBuilder.setAdpEnable(false);
        ParsingUtils.parseField(output, ADP_ENABLE_LINE::matcher,
                matcher -> true,
                configBuilder::setAdpEnable);

        configBuilder.setEnable(false);
        ParsingUtils.parseField(output, ENABLE_LINE::matcher,
                matcher -> true,
                configBuilder::setEnable);

        configBuilder.setSsdEnable(false);
        ParsingUtils.parseField(output, SSD_ENABLE_LINE::matcher,
                matcher -> true,
                configBuilder::setSsdEnable);
    }
}