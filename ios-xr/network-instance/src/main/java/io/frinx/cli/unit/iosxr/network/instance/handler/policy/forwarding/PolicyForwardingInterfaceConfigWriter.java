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
package io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Optional;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.cisco.rev171109.NiPfIfCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;
    private static final String UPDATE_TEMPLATE = """
            interface {$ifcName}
            {$pfAug|update(input_service_policy,service-policy input `$pfAug.input_service_policy`
            ,no service-policy input `$before.input_service_policy`
            , true)}{$pfAug|update(output_service_policy,service-policy output `$pfAug.output_service_policy`
            ,no service-policy output `$before.output_service_policy`
            , true)}root""";

    public PolicyForwardingInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkArgument(NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class)),
                "Policy forwarding should be configured in default network instance");

        String ifcName = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();

        // check ifc/sub-ifc existence
        Preconditions.checkArgument(existsInterface(ifcName, writeContext),
                "Cannot configure policy forwarding on non-existent interface %s", ifcName);

        Optional<NiPfIfCiscoAug> pfIfAug = Optional.ofNullable(dataAfter.getAugmentation(NiPfIfCiscoAug.class));
        if (!pfIfAug.isPresent()) {
            return;
        }

        blockingWriteAndRead(cli, id, dataAfter,
                f("interface %s", ifcName),
                pfIfAug.get().getInputServicePolicy() != null
                        ? f("service-policy input %s", pfIfAug.get().getInputServicePolicy())
                        : "no service-policy input",
                pfIfAug.get().getOutputServicePolicy() != null
                        ? f("service-policy output %s", pfIfAug.get().getOutputServicePolicy())
                        : "no service-policy output",
                "root");
    }

    private String getCommand(String ifcName, NiPfIfCiscoAug after, NiPfIfCiscoAug before) {
        return fT(UPDATE_TEMPLATE, "ifcName", ifcName, "pfAug", after, "before", before);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore, @NotNull
            Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkArgument(NetworInstance.DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class)),
                "Policy forwarding should be configured in default network instance");

        Optional<NiPfIfCiscoAug> pfIfAugAfter = Optional.ofNullable(dataAfter.getAugmentation(NiPfIfCiscoAug.class));
        if (!pfIfAugAfter.isPresent()) {
            return;
        }
        Optional<NiPfIfCiscoAug> pfIfAugBefore = Optional.ofNullable(dataBefore.getAugmentation(NiPfIfCiscoAug.class));

        String ifcName = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();

        blockingWriteAndRead(cli, id, dataAfter, getCommand(ifcName, pfIfAugAfter.get(), pfIfAugBefore.get()));
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getInterfaceId()
                .getValue();

        blockingDeleteAndRead(cli, id,
                f("interface %s", ifcName),
                "no service-policy output",
                "no service-policy input",
                "root");
    }

    private static boolean existsInterface(String ifcName, WriteContext writeContext) {
        if (SubinterfaceReader.isSubinterface(ifcName)) {
            Matcher matcher = SubinterfaceReader.SUBINTERFACE_NAME.matcher(ifcName);
            matcher.matches();

            InstanceIdentifier<Subinterface> subifcId = IIDs.INTERFACES
                .child(
                    org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                            .rev161222.interfaces.top.interfaces.Interface.class,
                    new InterfaceKey(matcher.group("ifcId")))
                .child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(Long.valueOf(matcher.group("subifcIndex"))));
            return writeContext.readAfter(subifcId).isPresent();
        }

        InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                .rev161222.interfaces.top.interfaces.Interface> ifcId = IIDs.INTERFACES
                        .child(
                            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222
                                    .interfaces.top.interfaces.Interface.class,
                            new InterfaceKey(ifcName));
        return writeContext.readAfter(ifcId).isPresent();
    }
}