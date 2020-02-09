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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.cp;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIPointsWriter implements CompositeWriter.Child<ConnectionPoints>, CliWriter<ConnectionPoints> {

    private Cli cli;

    public L2VSIPointsWriter(Cli cli) {
        this.cli = cli;
    }

    // TODO: fill in

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> iid,
                                                 @Nonnull ConnectionPoints data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        return false;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> iid,
                                                  @Nonnull ConnectionPoints dataBefore,
                                                  @Nonnull ConnectionPoints dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        return false;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> iid,
                                                  @Nonnull ConnectionPoints dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        return false;
    }
}
