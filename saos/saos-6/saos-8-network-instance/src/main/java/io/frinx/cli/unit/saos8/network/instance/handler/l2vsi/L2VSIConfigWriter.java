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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
            """
                    virtual-switch create vs {$data.name}
                    {% if ($data.description) %}virtual-switch set vs {$data.name} description "{$data.description}"
                    {% endif %}""";

    private static final String UPDATE_TEMPLATE =
            "{$data|update(description,virtual-switch set vs `$data.name` "
            + "description \"`$data.description`\"\n,)}";

    private Cli cli;

    public L2VSIConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid,
                                                 @NotNull Config data,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkNotNull(data.getType(), "Network instance type is not set");

        if (!data.getType().equals(L2VSI.class)) {
            return false;
        }

        blockingWriteAndRead(fT(WRITE_TEMPLATE, "data", data), cli, iid, data);

        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid,
                                                  @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkNotNull(dataAfter.getType(), "Network instance type is not set");

        if (!dataAfter.getType().equals(L2VSI.class)) {
            return false;
        }

        blockingWriteAndRead(fT(UPDATE_TEMPLATE, "data", dataAfter, "before", dataBefore),
                cli, iid, dataAfter);

        return true;
    }


    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid,
                                                  @NotNull Config dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!dataBefore.getType().equals(L2VSI.class)) {
            return false;
        }

        blockingDeleteAndRead(f("virtual-switch delete vs %s",
                dataBefore.getName()), cli, iid);

        return true;
    }
}