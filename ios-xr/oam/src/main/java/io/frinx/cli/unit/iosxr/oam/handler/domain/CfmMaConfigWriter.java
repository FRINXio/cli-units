/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.oam.handler.domain;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.ma.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmMaConfigWriter implements CliWriter<Config> {
    private Cli cli;

    @SuppressWarnings("checkstyle:linelength")
    private static final String CREATE_TEMPLATE = """
            ethernet cfm
            domain {$domain.domain_name} level {$domain.level.value}
            service {$config.ma_name} down-meps
            {% if ($config.continuity_check_interval) %}continuity-check interval {$config.continuity_check_interval.name} loss-threshold {$config.continuity_check_loss_threshold}
            {% else %}no continuity-check interval
            {% endif %}{% loop in $removed_crosscheck as $mep_id %}no mep crosscheck mep-id {$mep_id}
            {% onEmpty %}{% endloop %}{% loop in $added_crosscheck as $mep_id %}mep crosscheck mep-id {$mep_id}
            {% onEmpty %}{% endloop %}{% if $is_efd == TRUE %}{% else %}no {% endif %}efd
            root""";

    private static final String DELETE_TEMPLATE = """
            ethernet cfm
            domain {$domain.domain_name} level {$domain.level.value}
            no service {$config.ma_name}
            root""";

    public CfmMaConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config config,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        writeOrUpdateCurrentAttributes(id, null, config, true, writeContext);
    }

    @Override
    public void updateCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataBefore,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        writeOrUpdateCurrentAttributes(id, dataBefore, dataAfter, false, writeContext);
    }

    private void writeOrUpdateCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config dataBefore,
        Config dataAfter,
        boolean isCreate,
        WriteContext writeContext) throws WriteFailedException  {

        Preconditions.checkArgument(
            (dataAfter.getContinuityCheckInterval() == null && dataAfter.getContinuityCheckLossThreshold() == null)
            || (dataAfter.getContinuityCheckInterval() != null && dataAfter.getContinuityCheckLossThreshold() != null),
            "Continuity check interval and continuity check threshold must be set as a pair");

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.Config
            domain = readAfterDomainConfig(id, writeContext);

        List<Integer> removedCrosscheck;
        List<Integer> addedCrosscheck;

        if (isCreate) {
            removedCrosscheck = Collections.emptyList();
            addedCrosscheck = dataAfter.getMepCrosscheck();
        } else {
            removedCrosscheck = dataBefore.getMepCrosscheck().stream()
                .filter(v -> !Iterables.contains(dataAfter.getMepCrosscheck(), v))
                .collect(Collectors.toList());

            addedCrosscheck = dataAfter.getMepCrosscheck().stream()
                .filter(v -> !Iterables.contains(dataBefore.getMepCrosscheck(), v))
                .collect(Collectors.toList());
        }

        blockingWriteAndRead(cli, id, dataAfter,
                fT(CREATE_TEMPLATE,
                    "domain", domain,
                    "config", dataAfter,
                    "is_efd", dataAfter.isEfd(),     // $config.is_efd doesn't work in chunk template.
                    "removed_crosscheck", removedCrosscheck,
                    "added_crosscheck", addedCrosscheck));
    }

    @Override
    public void deleteCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config config,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.Config
            domain = readBeforeDomainConfig(id, writeContext);

        blockingDeleteAndRead(cli, id,
            fT(DELETE_TEMPLATE,
                "domain", domain,
                "config", config));
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains
        .domain.Config readBeforeDomainConfig(InstanceIdentifier<Config> id, WriteContext writeContext) {

        InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm
            .domains.domain.Config> configId = RWUtils.cutId(id, Domain.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains
                    .domain.Config.class);

        return writeContext.readBefore(configId).orElse(null);
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains
        .domain.Config readAfterDomainConfig(InstanceIdentifier<Config> id, WriteContext writeContext) {

        InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm
            .domains.domain.Config> configId = RWUtils.cutId(id, Domain.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains
                    .domain.Config.class);

        return writeContext.readAfter(configId).orElse(null);
    }
}