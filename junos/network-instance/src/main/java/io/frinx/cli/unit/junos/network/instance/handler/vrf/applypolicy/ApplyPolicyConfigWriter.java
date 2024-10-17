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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.applypolicy;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ApplyPolicyConfigWriter implements CliWriter<Config> {
    private static final String WRITE_INSTANCE_IMPORT_TEMPLATE =
            "set routing-instances %s routing-options instance-import %s";
    private static final String DELETE_INSTANCE_IMPORT_TEMPLATE =
            "delete routing-instances %s routing-options instance-import %s";

    private final Cli cli;

    public ApplyPolicyConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String vrfName = id.firstKeyOf(NetworkInstance.class).getName();
        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            return;
        }

        writeImportPolicy(id, vrfName, dataAfter);
    }

    @Override
    public void updateCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataBefore,
            @NotNull Config dataAfter,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String vrfName = id.firstKeyOf(NetworkInstance.class).getName();
        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            return;
        }

        // This is correct procedure here, if we register multiple values
        // in import-policy (this type is leaf-list), Junos also remembers their registering order.
        // So, when we update import-policy, we should delete all previous policies
        // and regiester all following policies rather than just addding or deleting differences.
        if (!isEqualPolicy(dataBefore.getImportPolicy(), dataAfter.getImportPolicy())) {
            deleteImportPolicy(id, vrfName, dataBefore);
            writeImportPolicy(id, vrfName, dataAfter);
        }
    }

    @Override
    public void deleteCurrentAttributes(
            @NotNull InstanceIdentifier<Config> id,
            @NotNull Config dataBefore,
            @NotNull WriteContext writeContext) throws WriteFailedException {

        String vrfName = id.firstKeyOf(NetworkInstance.class).getName();
        deleteImportPolicy(id, vrfName, dataBefore);
    }

    private void writeImportPolicy(
        InstanceIdentifier<Config> id,
        String vrfName,
        Config data) throws WriteFailedException {

        if (data.getImportPolicy() == null) {
            return;
        }

        for (String policy : data.getImportPolicy()) {
            blockingWriteAndRead(cli, id, data, f(WRITE_INSTANCE_IMPORT_TEMPLATE, vrfName, policy));
        }
    }

    private void deleteImportPolicy(
        InstanceIdentifier<Config> id,
        String vrfName,
        Config data) throws WriteFailedException {

        if (data.getImportPolicy() == null) {
            return;
        }
        for (String policy : data.getImportPolicy()) {
            blockingDeleteAndRead(cli, id, f(DELETE_INSTANCE_IMPORT_TEMPLATE, vrfName, policy));
        }
    }

    private static boolean isEqualPolicy(List<String> beforePolicy, List<String> afterPolicy) {
        if (beforePolicy == null) {
            beforePolicy = Collections.emptyList();
        }
        if (afterPolicy == null) {
            afterPolicy = Collections.emptyList();
        }

        return beforePolicy.equals(afterPolicy);
    }
}