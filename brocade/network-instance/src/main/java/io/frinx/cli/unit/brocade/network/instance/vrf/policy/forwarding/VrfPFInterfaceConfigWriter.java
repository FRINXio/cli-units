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

package io.frinx.cli.unit.brocade.network.instance.vrf.policy.forwarding;

import com.google.common.annotations.VisibleForTesting;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.vrf.ifc.VrfInterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.brocade.rev190726.NiPfIfBrocadeAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfPFInterfaceConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            configure terminal
            {$pfAug|update(input_service_policy,interface `$ifcName`
            rate-limit input policy-map `$pfAug.input_service_policy`
            ,interface `$ifcName`
            no rate-limit input policy-map `$before.input_service_policy`
            )}exit
            {$pfAug|update(output_service_policy,interface `$ifcName`
            rate-limit output policy-map `$pfAug.output_service_policy`
            ,interface `$ifcName`
            no rate-limit output policy-map `$before.output_service_policy`
            )}end""";

    private Cli cli;

    public VrfPFInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> id,
                                                 @NotNull Config dataAfter,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {

        if (!VrfInterfaceReader.VRF_CHECK.canProcess(id, writeContext, false)) {
            return false;
        }

        writeCurrentAttributes(id, dataAfter);

        return true;
    }

    protected void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                          @NotNull Config dataAfter) throws WriteFailedException.CreateFailedException {
        Optional<NiPfIfBrocadeAug> policyForwardAug =
                Optional.ofNullable(dataAfter.getAugmentation(NiPfIfBrocadeAug.class));
        if (policyForwardAug.isPresent()) {
            blockingWriteAndRead(cli, id, dataAfter,
                    getCommand(dataAfter, policyForwardAug.get(), null,false));
        }
    }

    @VisibleForTesting
    String getCommand(@NotNull Config config, NiPfIfBrocadeAug pfAug, NiPfIfBrocadeAug before, boolean delete) {
        return fT(WRITE_TEMPLATE, "ifcName", config.getInterfaceId().getValue(),
                "pfAug", pfAug,
                "before", before,
                "delete", delete ? Chunk.TRUE : null);
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> id,
                                                  @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {

        return writeCurrentAttributesWResult(id, dataAfter, writeContext);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> id,
                                                  @NotNull Config dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {

        if (!VrfInterfaceReader.VRF_CHECK.canProcess(id, writeContext, true)) {
            return false;
        }

        deleteCurrentAttributes(id, dataBefore);

        return true;
    }

    protected void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                           @NotNull Config dataBefore)
            throws WriteFailedException.DeleteFailedException {
        Optional<NiPfIfBrocadeAug> policyForwardAug =
                Optional.ofNullable(dataBefore.getAugmentation(NiPfIfBrocadeAug.class));
        if (policyForwardAug.isPresent()) {
            blockingDeleteAndRead(cli, id, getCommand(dataBefore, null, policyForwardAug.get(), true));
        }
    }
}