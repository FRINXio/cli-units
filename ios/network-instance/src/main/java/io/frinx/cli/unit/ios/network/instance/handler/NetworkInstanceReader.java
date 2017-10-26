/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.InitCliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NetworkInstanceReader implements InitCliListReader<NetworkInstance, NetworkInstanceKey,
        NetworkInstanceBuilder> {

    private static final String SH_IP_VRF = "sh ip vrf";
    private static final Pattern VRF_ID_LINE = Pattern.compile("(?<id>[^\\s]+[\\s]+).*");

    private Cli cli;

    public NetworkInstanceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<NetworkInstanceKey> getAllIds(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseVrfIds(blockingRead(SH_IP_VRF, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<NetworkInstanceKey> parseVrfIds(String output) {
        List<NetworkInstanceKey> networkInstanceKeys = ParsingUtils.parseFields(output, 1,
                VRF_ID_LINE::matcher,
                m -> m.group("id"),
                value -> new NetworkInstanceKey(value.trim()));

        networkInstanceKeys.add(DEFAULT_NETWORK);

        return networkInstanceKeys;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<NetworkInstance> list) {
        ((NetworkInstancesBuilder) builder).setNetworkInstance(list);
    }

    @Nonnull
    @Override
    public NetworkInstanceBuilder getBuilder(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier) {
        return new NetworkInstanceBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                      @Nonnull NetworkInstanceBuilder networkInstanceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        networkInstanceBuilder.setName(name);
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull InstanceIdentifier<NetworkInstance> id,
                                                  @Nonnull NetworkInstance readValue,
                                                  @Nonnull ReadContext ctx) {
        // Direct translation
        return Initialized.create(id, readValue);
    }
}
