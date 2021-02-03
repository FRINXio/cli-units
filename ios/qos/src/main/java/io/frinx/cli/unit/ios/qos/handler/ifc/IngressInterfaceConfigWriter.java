/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.qos.handler.ifc;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIngressInterfaceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos._interface.input.top.input.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressInterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$interface_id}\n"
            + "{% if ($policy_map) %}service-policy input {$policy_map}\n{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "interface {$interface_id}\n"
            + "{% if ($policy_map) %}no service-policy input {$policy_map}\n{% endif %}"
            + "end";

    private final Cli cli;

    public IngressInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        final String policyMapName = getServicePolicy(config);

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                        "interface_id", interfaceId,
                        "policy_map", policyMapName));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        final String policyMapName = getServicePolicy(config);

        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(DELETE_TEMPLATE,
                        "interface_id", interfaceId,
                        "policy_map", policyMapName));
    }

    private String getServicePolicy(final Config config) {
        final QosIngressInterfaceAug aug = config.getAugmentation(QosIngressInterfaceAug.class);
        return aug != null ? aug.getServicePolicy() : null;
    }

}
