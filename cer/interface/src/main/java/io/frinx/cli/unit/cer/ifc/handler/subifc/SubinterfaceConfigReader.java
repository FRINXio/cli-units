/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.cer.ifc.Util;
import io.frinx.cli.unit.cer.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rev220420.CerIfAggSubifAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rev220420.CerIfAggSubifAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rev220420.CerIfAggregateSubifExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern ISIS_WIDE_METRIC_LEVEL_TYPE_LINE =
            Pattern.compile("\\s*isis wide-metric (?<value>.+) (?<levelType>.+)");
    private static final Pattern IP_OSPF_COST_LINE = Pattern.compile("\\s*ip ospf (?<ospfId>.+) cost (?<cost>.+)");

    private final Cli cli;

    public SubinterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        final var subKey = id.firstKeyOf(Subinterface.class);

        if (subKey == null) {
            return;
        }

        final String subIfcName = Util.getSubinterfaceName(id);
        parseSubinterfaceConfig(blockingRead(String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, subIfcName),
                cli, id, ctx), builder, subKey.getIndex(), subIfcName);
    }

    @VisibleForTesting
    static void parseSubinterfaceConfig(String ifcOutput, ConfigBuilder builder, Long subKey, String name) {
        builder.setIndex(subKey);
        builder.setName(name);
        // Set enabled unless proven otherwise
        builder.setEnabled(true);
        // Actually check if disabled
        ParsingUtils.parseField(ifcOutput,
            InterfaceConfigReader.SHUTDOWN_LINE::matcher,
            matcher -> false,
            builder::setEnabled);

        ParsingUtils.parseField(ifcOutput,
            InterfaceConfigReader.DESCRIPTION_LINE::matcher,
            matcher -> matcher.group("desc"),
            builder::setDescription);

        CerIfAggSubifAugBuilder augBuilder = new CerIfAggSubifAugBuilder();
        setIsisWideMetricLevelType(ifcOutput, augBuilder);
        setOspfCost(ifcOutput, augBuilder);

        builder.addAugmentation(CerIfAggSubifAug.class, augBuilder.build());
    }

    private static void setIsisWideMetricLevelType(final String ifcOutput,
                                                   final CerIfAggSubifAugBuilder builder) {
        var value = ParsingUtils.parseField(ifcOutput, 0,
            ISIS_WIDE_METRIC_LEVEL_TYPE_LINE::matcher,
            matcher -> Long.valueOf(Integer.parseInt(matcher.group("value"))));

        var levelType = ParsingUtils.parseField(ifcOutput, 0,
            ISIS_WIDE_METRIC_LEVEL_TYPE_LINE::matcher,
            matcher -> convertIsisLevelFromString(matcher.group("levelType")));

        if (value.isPresent() && levelType.isPresent()) {
            builder.setMetric(value.get());
            builder.setLevelType(levelType.get());
        }
    }

    private static CerIfAggregateSubifExtension.LevelType convertIsisLevelFromString(String level) {
        if (level == null) {
            return null;
        }
        switch (level) {
            case "level-1":
                return CerIfAggregateSubifExtension.LevelType.LEVEL1;
            case "level-2":
                return CerIfAggregateSubifExtension.LevelType.LEVEL2;
            case "level-1-2":
                return CerIfAggregateSubifExtension.LevelType.LEVEL12;
            default :
                throw new IllegalArgumentException("Unknown isis level " + level);
        }
    }

    private static void setOspfCost(final String ifcOutput, final CerIfAggSubifAugBuilder builder) {
        ParsingUtils.parseField(ifcOutput,
            IP_OSPF_COST_LINE::matcher,
            matcher -> Integer.valueOf(matcher.group("cost")),
            builder::setCost);
    }
}
