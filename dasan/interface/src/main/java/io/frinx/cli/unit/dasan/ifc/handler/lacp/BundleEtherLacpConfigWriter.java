/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.dasan.ifc.handler.lacp;

import com.google.common.base.Preconditions;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.BundleEtherInterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.AggregationType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class BundleEtherLacpConfigWriter implements CliWriter<Config> {

    private Cli cli;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    public BundleEtherLacpConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifName);
        if (!matcher.matches()) {
            return;
        }

        try {
            Preconditions.checkArgument(AggregationType.LACP.equals(data.getLagType()), "unsupported lag type. : %s",
                    data.getLagType());
        } catch (IllegalArgumentException e) {
            throw new WriteFailedException.CreateFailedException(id, data, e);
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifName);
        if (!matcher.matches()) {
            return;
        }

        try {
            Preconditions.checkArgument(dataBefore.getLagType().equals(dataAfter.getLagType()),
                    "Changing lag type is not permitted. Before: %s, After: %s", dataBefore.getLagType(),
                    dataAfter.getLagType());
        } catch (IllegalArgumentException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull WriteContext writeContext) throws WriteFailedException {
    }
}