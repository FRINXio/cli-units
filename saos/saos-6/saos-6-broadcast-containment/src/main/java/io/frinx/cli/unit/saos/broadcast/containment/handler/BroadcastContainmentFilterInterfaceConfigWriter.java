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

package io.frinx.cli.unit.saos.broadcast.containment.handler;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.Filter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class BroadcastContainmentFilterInterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "broadcast-containment add filter {$filter_name} port {$port}\n"
            + "configuration save";
    private static final String DELETE_TEMPLATE = "broadcast-containment remove filter {$filter_name} port {$port}\n"
            + "configuration save";

    private Cli cli;

    public BroadcastContainmentFilterInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String filterName = instanceIdentifier.firstKeyOf(Filter.class).getName();
        String port = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Preconditions.checkArgument(Objects.equal(config.getName(), port),
                "port names should be the same");
        blockingWriteAndRead(cli, instanceIdentifier, config, getTemplate(true, filterName, port));
    }

    private String getTemplate(Boolean write, String filterName, String port) {
        if (write) {
            return fT(WRITE_TEMPLATE, "filter_name", filterName, "port", port);
        }
        else {
            return fT(DELETE_TEMPLATE, "filter_name", filterName, "port", port);
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String filterName = id.firstKeyOf(Filter.class).getName();
        String port = id.firstKeyOf(Interface.class).getName();
        Preconditions.checkArgument(Objects.equal(dataAfter.getName(), port),
                "port names should be the same");
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String filterName = instanceIdentifier.firstKeyOf(Filter.class).getName();
        String port = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(cli, instanceIdentifier, getTemplate(false, filterName, port));
    }
}
