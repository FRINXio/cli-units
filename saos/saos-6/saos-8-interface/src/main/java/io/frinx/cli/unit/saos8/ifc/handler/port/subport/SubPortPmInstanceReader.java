/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortPmInstanceReader implements CliConfigListReader<PmInstance, PmInstanceKey, PmInstanceBuilder> {

    static final String SHOW_COMMAND = "configuration search string \"pm create sub-port\"";

    private final Cli cli;

    public SubPortPmInstanceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<PmInstanceKey> getAllIds(@Nonnull InstanceIdentifier<PmInstance> instanceIdentifier,
                                         @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            Long index = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

            Optional<String> subPortName = getSubPortName(instanceIdentifier, readContext, parentPort, index);

            if (subPortName.isPresent()) {
                String output = blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext);
                return getAllIds(output, subPortName.get());
            }
        }
        return Collections.emptyList();
    }

    @VisibleForTesting
    static List<PmInstanceKey> getAllIds(String output, String subPort) {
        Pattern allIds = Pattern.compile("pm create sub-port " + subPort + " pm-instance (?<name>\\S+).*");

        return ParsingUtils.parseFields(output, 0,
            allIds::matcher,
            matcher -> matcher.group("name"),
            PmInstanceKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<PmInstance> instanceIdentifier,
                                      @Nonnull PmInstanceBuilder pmInstanceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            pmInstanceBuilder.setName(instanceIdentifier.firstKeyOf(PmInstance.class).getName());
        }
    }

    private Optional<String> getSubPortName(@Nonnull InstanceIdentifier<PmInstance> instanceIdentifier,
                                            @Nonnull ReadContext readContext,
                                            String parentPort,
                                            Long index) throws ReadFailedException {
        String subPortNameOutput = blockingRead(SubPortReader.SHOW_COMMAND, cli,
                instanceIdentifier, readContext);

        return SubPortConfigReader.getSubPortName(subPortNameOutput, parentPort, index);
    }

    private boolean isPort(InstanceIdentifier<PmInstance> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.getAllIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}