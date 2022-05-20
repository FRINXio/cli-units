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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.cer.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rev220420.CerIfAggSubifAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rev220420.CerIfAggregateSubifExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigWriter implements CliWriter<Config>  {

    private static final String WRITE_TEMPLATE = "configure\n"
            + "interface {$ifc_name}\n"
            + "isis wide-metric {$metric_value} {$metric_level_type}\n"
            + "ip ospf cost {$ospf_cost}\n"
            + "end";

    private static final String UPDATE_TEMPLATE = "configure\n"
            + "interface {$ifc_name}\n"
            + "{% if ($after_metric_value) %}no isis wide-metric {$before_metric_level_type}\n"
            + "isis wide-metric {$after_metric_value} {$after_metric_level_type}\n{% endif %}"
            + "{% if ($after_ospf_cost) %}no ip ospf cost {$before_ospf_cost}\n"
            + "ip ospf cost {$after_ospf_cost}\n{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure\n"
            + "interface {$ifc_name}\n"
            + "no isis wide-metric {$metric_level_type}\n"
            + "no ip ospf cost {$ospf_cost}\n"
            + "end";

    private final Cli cli;

    public SubinterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String subIfcName = Util.getSubinterfaceName(id);
        blockingWriteAndRead(cli, id, config,
                fT(WRITE_TEMPLATE,
                        "ifc_name", subIfcName,
                        "metric_value", getMetricValue(config).isPresent() ? getMetricValue(config).get() : null,
                        "metric_level_type", getMetricLevelType(config).isPresent()
                                ? getMetricLevelType(config).get() : null,
                        "ospf_cost", getOspfCost(config).isPresent()
                                ? getOspfCost(config).get() : null));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        final String subIfcName = Util.getSubinterfaceName(id);
        blockingWriteAndRead(cli, id, dataAfter,
                fT(UPDATE_TEMPLATE, "ifc_name", subIfcName,
                        "before_metric_value", getMetricValue(dataBefore).isPresent()
                                ? getMetricValue(dataBefore).get() : null,
                        "after_metric_value", getMetricValue(dataAfter).isPresent()
                                ? getMetricValue(dataAfter).get() : null,
                        "before_metric_level_type", getMetricLevelType(dataBefore).isPresent()
                                ? getMetricLevelType(dataBefore).get() : null,
                        "after_metric_level_type", getMetricLevelType(dataAfter).isPresent()
                                ? getMetricLevelType(dataAfter).get() : null,
                        "before_ospf_cost", getOspfCost(dataBefore).isPresent()
                                ? getOspfCost(dataBefore).get() : null,
                        "after_ospf_cost", getOspfCost(dataAfter).isPresent()
                                ? getOspfCost(dataAfter).get() : null));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String subIfcName = Util.getSubinterfaceName(id);
        blockingDeleteAndRead(cli, id,
                fT(DELETE_TEMPLATE,
                        "ifc_name", subIfcName,
                        "metric_value", getMetricValue(config).isPresent()
                                ? getMetricValue(config).get() : null,
                        "metric_level_type", getMetricLevelType(config).isPresent()
                                ? getMetricLevelType(config).get() : null,
                        "ospf_cost", getOspfCost(config).isPresent()
                                ? getOspfCost(config).get() : null));
    }

    private Optional<Long> getMetricValue(Config config) {
        CerIfAggSubifAug cerSubIfGlobalAug = config.getAugmentation(CerIfAggSubifAug.class);
        if (cerSubIfGlobalAug == null || cerSubIfGlobalAug.getMetric() == null) {
            return Optional.empty();
        }
        return Optional.of(cerSubIfGlobalAug.getMetric());
    }

    private Optional<String> getMetricLevelType(Config config) {
        CerIfAggSubifAug cerSubIfGlobalAug = config.getAugmentation(CerIfAggSubifAug.class);
        if (cerSubIfGlobalAug == null || cerSubIfGlobalAug.getLevelType() == null) {
            return Optional.empty();
        }
        return Optional.of(convertIsisLevelToString(cerSubIfGlobalAug.getLevelType()));
    }

    private String convertIsisLevelToString(CerIfAggregateSubifExtension.LevelType levelType) {
        if (levelType == null) {
            return null;
        }
        switch (levelType) {
            case LEVEL1:
                return "level-1";
            case LEVEL2:
                return "level-2";
            case LEVEL12:
                return "level-1-2";
            default :
                throw new IllegalArgumentException("Unknown isis level " + levelType);
        }
    }

    private Optional<Integer> getOspfCost(Config config) {
        CerIfAggSubifAug cerSubIfGlobalAug = config.getAugmentation(CerIfAggSubifAug.class);
        if (cerSubIfGlobalAug == null || cerSubIfGlobalAug.getCost() == null) {
            return Optional.empty();
        }
        return Optional.of(cerSubIfGlobalAug.getCost());
    }
}
