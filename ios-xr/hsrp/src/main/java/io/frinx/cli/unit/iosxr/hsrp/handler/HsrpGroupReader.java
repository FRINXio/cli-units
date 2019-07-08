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

package io.frinx.cli.unit.iosxr.hsrp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.hsrp.handler.util.HsrpUtil;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814.hsrp.groups.HsrpGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.ADDRESSFAMILY;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpGroupReader implements CliConfigListReader<HsrpGroup, HsrpGroupKey, HsrpGroupBuilder> {

    public static final String SH_HSRP_INTERFACE =
            "show running-config router hsrp interface %s | include address-family";
    private static final Pattern FAMILY_LINE = Pattern.compile("\\s*address-family (?<type>[^\\n].*)");
    public static final String SH_GROUPS =
            "show running-config router hsrp interface %s address-family %s | include ^   hsrp";
    private static final Pattern GROUP_LINE = Pattern.compile("\\s*hsrp (?<groupNumber>[0-9]+).*");

    private Cli cli;

    public HsrpGroupReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<HsrpGroupKey> getAllIds(InstanceIdentifier<HsrpGroup> instanceIdentifier, ReadContext readContext)
            throws ReadFailedException {
        List<HsrpGroupKey> hsrpGroups = new ArrayList<>();
        String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        String readCommand = f(SH_HSRP_INTERFACE, interfaceName);
        String output = blockingRead(readCommand, cli, instanceIdentifier, readContext);

        List<Class<? extends ADDRESSFAMILY>> familyList = parseHsrpGroupKeys(output);

        for (Class<? extends ADDRESSFAMILY> family : familyList) {
            String familyType = HsrpUtil.getStringType(family);

            readCommand = f(SH_GROUPS, interfaceName, familyType);

            output = blockingRead(readCommand, cli, instanceIdentifier, readContext);

            ParsingUtils.parseFields(output, 0, GROUP_LINE::matcher,
                matcher -> Long.valueOf(matcher.group("groupNumber")),
                value -> hsrpGroups.add(new HsrpGroupKey(family, value)));
        }
        return hsrpGroups;
    }

    @VisibleForTesting
    public List<Class<? extends ADDRESSFAMILY>> parseHsrpGroupKeys(String output) {
        return ParsingUtils.parseFields(output, 0, FAMILY_LINE::matcher,
            matcher -> HsrpUtil.getType(matcher.group("type")), value -> value);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<HsrpGroup> instanceIdentifier,
            @Nonnull HsrpGroupBuilder hsrpGroupBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        HsrpGroupKey key = instanceIdentifier.firstKeyOf(HsrpGroup.class);
        hsrpGroupBuilder.setAddressFamily(key.getAddressFamily());
        hsrpGroupBuilder.setVirtualRouterId(key.getVirtualRouterId());
    }
}
