/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.cisco.rev171109.NiPfIfCiscoAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;
    private static final String SHOW_ALL_POLICIES = "sh run policy-map | include ^policy-map";

    public PolicyForwardingInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkArgument(DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class)),
                "Policy forwarding should be configured in default network instance");

        String ifcName = id.firstKeyOf(Interface.class).getInterfaceId().getValue();

        // check ifc existence
        InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface> ifcId =
                IIDs.INTERFACES.child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
                        new InterfaceKey(ifcName));
        Preconditions.checkArgument(writeContext.readAfter(ifcId).isPresent(),
                "Cannot configure policy forwarding on non-existent interface %s", ifcName);

        NiPfIfCiscoAug pfIfAug = dataAfter.getAugmentation(NiPfIfCiscoAug.class);
        if (pfIfAug == null) {
            return;
        }

        String policies = blockingWriteAndRead(cli, id, dataAfter, SHOW_ALL_POLICIES);
        List<String> policyList = parsePolicies(policies);
        checkPoliciesExist(pfIfAug, policyList);

        blockingWriteAndRead(cli, id, dataAfter,
                "conf t",
                f("interface %s", ifcName),
                pfIfAug.getInputServicePolicy() != null
                        ? f("service-policy input %s", pfIfAug.getInputServicePolicy())
                        : "no service-policy input",
                pfIfAug.getOutputServicePolicy() != null
                        ? f("service-policy output %s", pfIfAug.getOutputServicePolicy())
                        : "no service-policy output",
                "commit",
                "end");
    }

    private static List<String> parsePolicies(String policies) {
        return ParsingUtils.NEWLINE.splitAsStream(policies)
                .map(String::trim)
                .map(policyLine -> policyLine.split(" ")[1])
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private static void checkPoliciesExist(NiPfIfCiscoAug pfIfAug, List<String> policyList) {
        policyList.add(null);
        Preconditions.checkArgument(policyList.contains(pfIfAug.getInputServicePolicy())
                && policyList.contains(pfIfAug.getOutputServicePolicy()),
                "Cannot configure %s policy-forwarding configuration,"
                        + " specifies policies do not exist, avalaible policies %s", pfIfAug, policyList);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getInterfaceId().getValue();

        blockingDeleteAndRead(cli, id,
                "conf t",
                f("interface %s", ifcName),
                "no service-policy output",
                "no service-policy input",
                "commit",
                "end");

    }
}
