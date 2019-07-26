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

package io.frinx.cli.unit.brocade.network.instance.policy.forwarding;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.brocade.rev190726.NiPfIfBrocadeAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceConfigWriter implements CliWriter<Config> {
    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$ifcName}\n"
            + "{% if ($brcd) %}"
                + "{$brcd|update(input_service_policy,rate-limit input policy-map `$brcd.input_service_policy`\n,"
                    + "no rate-limit input policy-map `$brcd.input_service_policy`\n)}"
                + "{$brcd|update(output_service_policy,rate-limit output policy-map `$brcd.output_service_policy`\n,"
                    + "no rate-limit output policy-map `$brcd.output_service_policy`\n)}"
            + "{% endif %}"
            + "end";

    private Cli cli;

    public PolicyForwardingInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataAfter,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        Preconditions.checkArgument(NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class)),
            "Policy forwarding should be configured in default network instance");

        NiPfIfBrocadeAug brcd = getBrocadeAug(dataAfter);
        if (brcd != null) {
            blockingWriteAndRead(cli, id, dataAfter, getCommand(dataAfter, brcd, false));
        }
    }

    @VisibleForTesting
    String getCommand(@Nonnull Config config, NiPfIfBrocadeAug brcd, boolean delete) {
        return fT(WRITE_TEMPLATE, "ifcName", config.getInterfaceId().getValue(),
                "brcd", brcd,
                "delete", delete ? Chunk.TRUE : null);
    }

    @Override
    public void updateCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataBefore,
        @Nonnull Config dataAfter,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataBefore,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        NiPfIfBrocadeAug brcd = getBrocadeAug(dataBefore);
        if (brcd != null) {
            blockingDeleteAndRead(cli, id, getCommand(dataBefore, brcd, true));
        }
    }

    private static NiPfIfBrocadeAug getBrocadeAug(Config config) {
        NiPfIfBrocadeAug brocadeAug = config.getAugmentation(NiPfIfBrocadeAug.class);
        if (brocadeAug == null) {
            return null;
        }
        return brocadeAug;
    }
}
