/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.l2vsi;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;
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

public class L2VSIReader implements CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>,
        CompositeListReader.Child<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    public static final String SH_L2_VFI = "sh run | include ^l2 vfi|^ vpn id|^ bridge-domain";
    public static final Pattern L2_VFI_LINE = Pattern.compile("l2 vfi (?<vfi>\\S+) autodiscovery\\s+vpn id (?<vccid>\\S+)\\s+bridge-domain (?<bd>\\S+).*");

    private Cli cli;

    public L2VSIReader(Cli cli) {
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

        return parseL2Vfis(reader.blockingRead(SH_L2_VFI, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<NetworkInstanceKey> parseL2Vfis(String output) {
        String linePerL2Vsi = realignL2vsi(output);

        return parseFields(linePerL2Vsi, 0,
                L2_VFI_LINE::matcher,
                m -> m.group("vfi"),
                NetworkInstanceKey::new);
    }

    public static String realignL2vsi(String output) {
        String withoutNewlines = output.replaceAll(NEWLINE.pattern(), "");
        return withoutNewlines.replace("l2 vfi", "\nl2 vfi");
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier,
                                      @Nonnull NetworkInstanceBuilder networkInstanceBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        networkInstanceBuilder.setName(name);
    }
}
