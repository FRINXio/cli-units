/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.brocade.network.instance.l2p2p;

import static io.frinx.cli.unit.utils.ParsingUtils.parseFields;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PReader implements CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>,
        CompositeListReader.Child<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    public static final String SH_VLL = "sh run | include vll";
    public static final Pattern VLL_ID_LINE = Pattern.compile("vll (?<network>\\S+)\\s+(?<vccid>\\S+).*");

    public static final String SH_VLL_LOCAL = "sh run | include vll-local";
    public static final Pattern VLL_LOCAL_ID_LINE = Pattern.compile("vll-local (?<network>\\S+).*");

    private Cli cli;

    public L2P2PReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<NetworkInstanceKey> getAllIds(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(instanceIdentifier, readContext, this.cli, this);
    }

    static List<NetworkInstanceKey> getAllIds(@Nonnull InstanceIdentifier<?> instanceIdentifier,
                                              @Nonnull ReadContext readContext,
                                              @Nonnull Cli cli,
                                              @Nonnull CliReader reader) throws ReadFailedException {
        if (!instanceIdentifier.getTargetType().equals(NetworkInstance.class)) {
            instanceIdentifier = RWUtils.cutId(instanceIdentifier, NetworkInstance.class);
        }

        // Parse vll based local-remote l2p2p
        List<NetworkInstanceKey> l2Ids = parseVllIds(reader.blockingRead(SH_VLL, cli, instanceIdentifier, readContext));
        // Parse vll-local based local-local l2p2p
        l2Ids.addAll(parseLocalConnectIds(reader.blockingRead(SH_VLL_LOCAL, cli, instanceIdentifier, readContext)));

        return l2Ids;
    }

    @VisibleForTesting
    static List<NetworkInstanceKey> parseVllIds(String output) {
        return parseFields(output, 0,
                VLL_ID_LINE::matcher,
                matcher -> matcher.group("network"),
                NetworkInstanceKey::new);
    }

    @VisibleForTesting
    static List<NetworkInstanceKey> parseLocalConnectIds(String output) {
        return parseFields(output, 0,
                VLL_LOCAL_ID_LINE::matcher,
                matcher -> matcher.group("network"),
                NetworkInstanceKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                      @Nonnull NetworkInstanceBuilder networkInstanceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        networkInstanceBuilder.setName(name);
    }
}
