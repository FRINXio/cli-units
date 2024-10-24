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

package io.frinx.cli.unit.saos8.ifc.handler.port.portqueuegroup;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortQueueGroupPmInstanceReader
        implements CliConfigListReader<PmInstance, PmInstanceKey, PmInstanceBuilder> {

    static final String SHOW_COMMAND = "configuration search string \"pm create port-queue-group\"";

    private final Cli cli;

    public PortQueueGroupPmInstanceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<PmInstanceKey> getAllIds(@NotNull InstanceIdentifier<PmInstance> instanceIdentifier,
                                          @NotNull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            String portQueueGroupName = parentPort + "-Default";
            String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
            return getAllIds(output, portQueueGroupName);
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    static List<PmInstanceKey> getAllIds(String output, String portQueueGroup) {
        Pattern allIds = Pattern.compile("pm create port-queue-group " + portQueueGroup
                + " pm-instance (?<name>\\S+).*");

        return ParsingUtils.parseFields(output, 0,
            allIds::matcher,
            matcher -> matcher.group("name"),
            PmInstanceKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<PmInstance> instanceIdentifier,
                                      @NotNull PmInstanceBuilder pmInstanceBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        pmInstanceBuilder.setName(instanceIdentifier.firstKeyOf(PmInstance.class).getName());
    }

    private boolean isPort(InstanceIdentifier<PmInstance> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.checkCachedIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}