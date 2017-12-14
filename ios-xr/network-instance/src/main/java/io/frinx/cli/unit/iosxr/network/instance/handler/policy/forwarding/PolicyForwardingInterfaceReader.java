/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.network.instance.handler.policy.forwarding;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;
import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK;
import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PolicyForwardingInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private final Cli cli;
    private static final String SH_SERVICE_POLICY_INT = "sh run int | utility egrep \"^interface|^ service-policy input|^ service-policy output\"";

    public PolicyForwardingInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> id, @Nonnull ReadContext context)
            throws ReadFailedException {

        if (!DEFAULT_NETWORK.equals(id.firstKeyOf(NetworkInstance.class))) {
            return Collections.emptyList();
        }

        String output = blockingRead(SH_SERVICE_POLICY_INT, cli, id, context);
        String realignedOutput = realignPFInterfacesOutput(output);
        return NEWLINE.splitAsStream(realignedOutput)
                .map(String::trim)
                .filter(s -> s.contains("service-policy"))
                .map(s -> s.split(" ")[0])
                .map(String::trim)
                .map(InterfaceId::new)
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> readData) {
        ((InterfacesBuilder) builder).setInterface(readData);
    }

    @Nonnull
    @Override
    public InterfaceBuilder getBuilder(@Nonnull InstanceIdentifier<Interface> id) {
        return new InterfaceBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> id, @Nonnull InterfaceBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        builder.setInterfaceId(id.firstKeyOf(Interface.class).getInterfaceId());
    }

    private static String realignPFInterfacesOutput(String output) {
        String withoutNewlines = output.replaceAll(NEWLINE.pattern(), "");
        return withoutNewlines.replace("interface ", "\n");
    }
}
