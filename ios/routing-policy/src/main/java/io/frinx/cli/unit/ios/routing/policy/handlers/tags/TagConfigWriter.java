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

package io.frinx.cli.unit.ios.routing.policy.handlers.tags;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.policy.extension.rev210525.cisco.tag.top.tags.tag.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.definitions.top.policy.definitions.PolicyDefinition;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.Statement;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TagConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "route-map {$routeMapId} {$routeMapSequence}\n"
            + "match tag {$tag}\n"
            + "exit\n"
            + "exit";

    private static final String UPDATE_TEMPLATE = "configure terminal\n"
            + "route-map {$routeMapId} {$routeMapSequence}\n"
            + "no match tag {$oldTag}\n"
            + "match tag {$newTag}\n"
            + "exit\n"
            + "exit";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "route-map {$routeMapId} {$routeMapSequence}\n"
            + "no match tag {$tag}\n"
            + "exit\n"
            + "exit";

    private final Cli cli;

    public TagConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String routeMapId = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        String sequence = instanceIdentifier.firstKeyOf(Statement.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, routeMapId, sequence));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String routeMapId = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        String sequence = instanceIdentifier.firstKeyOf(Statement.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter, updateTemplate(dataBefore, dataAfter,
                routeMapId, sequence));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String routeMapId = instanceIdentifier.firstKeyOf(PolicyDefinition.class).getName();
        String sequence = instanceIdentifier.firstKeyOf(Statement.class).getName();
        blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(config, routeMapId, sequence));
    }

    @VisibleForTesting
    String writeTemplate(Config config, String routeMapId, String sequence) {
        return fT(WRITE_TEMPLATE, "routeMapId", routeMapId, "routeMapSequence", sequence,
                "tag", config.getName());
    }

    @VisibleForTesting
    String updateTemplate(Config before, Config after, String routeMapId, String sequence) {
        return fT(UPDATE_TEMPLATE, "routeMapId", routeMapId, "routeMapSequence", sequence,
                "oldTag", before.getName(), "newTag", after.getName());
    }

    @VisibleForTesting
    String deleteTemplate(Config config, String routeMapId, String sequence) {
        return fT(DELETE_TEMPLATE, "routeMapId", routeMapId, "routeMapSequence", sequence,
                "tag", config.getName());
    }
}
