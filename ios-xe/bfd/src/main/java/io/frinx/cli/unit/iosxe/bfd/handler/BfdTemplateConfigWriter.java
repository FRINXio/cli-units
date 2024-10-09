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

package io.frinx.cli.unit.iosxe.bfd.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.BfdTempAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.BfdTemplateConfig.Type;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.BfdTemplates;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.Interval;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BfdTemplateConfigWriter implements CliWriter<BfdTempAug> {

    private static final String TEMPLATE = "{% if ($bfdTemplateConfig) %}"
            + "configure terminal\n"
            + "{$bfdTemplateConfig}"
            + "end"
            + "{% endif %}";

    private final Cli cli;

    public BfdTemplateConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<BfdTempAug> instanceIdentifier,
                                       @NotNull BfdTempAug bfdTempAug,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, bfdTempAug,
                fT(TEMPLATE,
                        "bfdTemplateConfig", getCommands(null, bfdTempAug)));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<BfdTempAug> instanceIdentifier,
                                        @NotNull BfdTempAug dataBefore,
                                        @NotNull BfdTempAug dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(TEMPLATE,
                        "bfdTemplateConfig", getCommands(dataBefore, dataAfter)));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<BfdTempAug> instanceIdentifier,
                                        @NotNull BfdTempAug bfdTempAug,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, bfdTempAug,
                fT(TEMPLATE,
                        "bfdTemplateConfig", getCommands(bfdTempAug, null)));
    }

    private String getCommands(final BfdTempAug before, BfdTempAug after) {
        final StringBuilder currentInstances = new StringBuilder();

        if (before != null && after != null) {
            final BfdTemplates bfdTemplatesBefore = before.getBfdTemplates();
            final BfdTemplates bfdTemplatesAfter = after.getBfdTemplates();
            if (bfdTemplatesBefore != null && bfdTemplatesAfter != null) {
                final List<BfdTemplate> bfdTemplateBeforeList = bfdTemplatesBefore.getBfdTemplate();
                final List<BfdTemplate> bfdTemplateAfterList = bfdTemplatesAfter.getBfdTemplate();
                // Comparing list of changed bfd-templates with the list of original ones
                if (bfdTemplateBeforeList != null && bfdTemplateAfterList != null) {
                    for (BfdTemplate bfdtAfter : bfdTemplateAfterList) {
                        for (BfdTemplate bfdtBefore : bfdTemplateBeforeList) {
                            if (bfdtBefore.getConfig().getName().equals(bfdtAfter.getConfig().getName())) {
                                if (!bfdtBefore.getConfig().getType().equals(bfdtAfter.getConfig().getType())
                                    || bfdtBefore.getInterval() != bfdtAfter.getInterval()) {
                                    currentInstances.append(getCommandsWhenDifferent(bfdtBefore, bfdtAfter));
                                }
                            }
                        }
                    }
                    currentInstances.append("exit\n");
                }
            }
        }
        else if (before != null) {
            final BfdTemplates bfdTemplates = before.getBfdTemplates();
            if (bfdTemplates != null) {
                final List<BfdTemplate> bfdTemplateList = bfdTemplates.getBfdTemplate();
                if (bfdTemplateList != null) {
                    for (final BfdTemplate bfdTemplate: bfdTemplateList) {
                        currentInstances.append(getCreationCommands(bfdTemplate.getConfig(), true));
                    }
                }
            }
        }

        else if (after != null) {
            final BfdTemplates bfdTemplates = after.getBfdTemplates();
            if (bfdTemplates != null) {
                final List<BfdTemplate> bfdTemplateList = bfdTemplates.getBfdTemplate();
                if (bfdTemplateList != null) {
                    for (final BfdTemplate bfdTemplate : bfdTemplateList) {
                        currentInstances.append(getCreationCommands(bfdTemplate.getConfig(), false));
                        currentInstances.append(getIntervalCommands(bfdTemplate.getInterval()));
                        currentInstances.append("exit\n");
                    }
                }
            }
        }
        return currentInstances.toString();
    }

    private String getCreationCommands(final Config config, boolean delete) {
        final StringBuilder creationCommands = new StringBuilder();
        final String name = config != null ? config.getName() : null;
        final Type type = config != null ? config.getType() : null;

        if (delete) {
            creationCommands.append("no ");
        }

        creationCommands.append("bfd-template ");
        if (type != null) {
            creationCommands.append(type.getName()).append(" ");
        }
        if (name != null) {
            creationCommands.append(name);
        }
        creationCommands.append("\n");

        return creationCommands.toString();
    }

    private String getIntervalCommands(final Interval interval) {
        final StringBuilder configCommands = new StringBuilder();
        if (interval != null) {
            final String minTx = interval.getMinTx();
            final String minRx = interval.getMinRx();
            final String multiplier = interval.getMultiplier();
            configCommands.append("interval");
            if (minTx != null && minRx != null) {
                configCommands.append(" min-tx ").append(minTx);
                configCommands.append(" min-rx ").append(minRx);

                if (multiplier != null) {
                    configCommands.append(" multiplier ").append(multiplier);
                }
            }
            configCommands.append("\n");
        }
        return configCommands.toString();
    }

    private String getCommandsWhenDifferent(BfdTemplate bfdTemplateBefore, BfdTemplate bfdTemplateAfter) {
        final StringBuilder configCommands = new StringBuilder();

        final Config bfdTemplateConfigBefore = bfdTemplateBefore.getConfig();
        final Config bfdTemplateConfigAfter = bfdTemplateAfter.getConfig();

        final Interval bfdTemplateIntervalAfter = bfdTemplateAfter.getInterval();
        final Interval bfdTemplateIntervalBefore = bfdTemplateBefore.getInterval();

        if (bfdTemplateConfigBefore != null && bfdTemplateConfigAfter != null) {
            if (!bfdTemplateConfigBefore.getType().equals(bfdTemplateConfigAfter.getType())) {
                configCommands.append(getCreationCommands(bfdTemplateConfigBefore, true));
                configCommands.append(getCreationCommands(bfdTemplateConfigAfter, false));
                configCommands.append(getIntervalCommands(bfdTemplateIntervalAfter));
            }
            else if (!bfdTemplateIntervalAfter.equals(bfdTemplateIntervalBefore)) {
                configCommands.append(getCreationCommands(bfdTemplateConfigAfter, false));
                if (bfdTemplateIntervalBefore != null) {
                    configCommands.append("no ").append(getIntervalCommands(bfdTemplateIntervalBefore));
                }
                configCommands.append(getIntervalCommands(bfdTemplateIntervalAfter));
            }
        }
        return configCommands.toString();
    }
}