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

package io.frinx.cli.iosxr.hsrp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.hsrp.handler.util.HsrpUtil;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.hsrp.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.hsrp.group.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.ADDRESSFAMILY;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpGroupConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public static final String SH_GROUPS =
            "show running-config router hsrp interface %s address-family %s | include ^ *hsrp %s";
    public static final String SH_GROUP = "show running-config router hsrp interface %s address-family %s %s";
    private static final Pattern GROUP_LINE =
            Pattern.compile("\\s*hsrp (?<groupNumber>[0-9]+) version (?<version>[0-9]+)");
    private static final Pattern HSRP_LINE = Pattern.compile("\\s*hsrp (?<hsrp>[0-9]+)");
    private static final Pattern PRIORITY_LINE = Pattern.compile("\\s*priority (?<priority>[0-9]+)");
    public static final Short DEFAULT_VERSION = 1;

    public HsrpGroupConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static void parseGroupConfig(ConfigBuilder configBuilder, String output) {

        configBuilder.setVersion(DEFAULT_VERSION);

        ParsingUtils.parseField(output, GROUP_LINE::matcher, matcher -> Short.valueOf(matcher.group("version")),
                configBuilder::setVersion);

        ParsingUtils.parseField(output, PRIORITY_LINE::matcher, matcher -> Short.valueOf(matcher.group("priority")),
                configBuilder::setPriority);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
            @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        Class<? extends ADDRESSFAMILY> family = instanceIdentifier.firstKeyOf(HsrpGroup.class).getAddressFamily();
        String familyType = HsrpUtil.getStringType(family);
        Long virtualRouterId = instanceIdentifier.firstKeyOf(HsrpGroup.class).getVirtualRouterId();

        configBuilder.setAddressFamily(family);
        configBuilder.setVirtualRouterId(virtualRouterId);

        String outputGroups = blockingRead(String.format(SH_GROUPS, interfaceId, familyType, virtualRouterId), cli,
                instanceIdentifier, readContext).trim();

        Optional<String> groupNumber =
            ParsingUtils.parseField(outputGroups, 0, HSRP_LINE::matcher, matcher -> matcher.group());

        if (groupNumber.isPresent()) {
            parseGroupConfig(configBuilder,
                    blockingRead(String.format(SH_GROUP, interfaceId, familyType, groupNumber.get()), cli,
                    instanceIdentifier, readContext));
        }
    }
}
