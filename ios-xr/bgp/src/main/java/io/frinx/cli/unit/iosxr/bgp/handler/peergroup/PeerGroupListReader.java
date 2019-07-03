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

package io.frinx.cli.unit.iosxr.bgp.handler.peergroup;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupListReader implements CliConfigListReader<PeerGroup, PeerGroupKey, PeerGroupBuilder> {

    static final String READ_NBR_GROUPS_CMD = "show running-config router bgp %s | include neighbor-group";

    private static final Pattern GROUP_LINE = Pattern.compile("neighbor\\-group (?<groupname>\\S+)");
    private Cli cli;

    public PeerGroupListReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<PeerGroupKey> getAllIds(@Nonnull InstanceIdentifier<PeerGroup> iid,
                                               @Nonnull ReadContext readContext) throws ReadFailedException {
        String networkInstanceName = iid.firstKeyOf(NetworkInstance.class).getName();
        Long as = readAsNumberFromContext(iid, readContext);
        if (as == null) {
            return Collections.EMPTY_LIST;
        }
        String output = blockingRead(f(READ_NBR_GROUPS_CMD, as), cli, iid, readContext);
        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(networkInstanceName)) {
            return ParsingUtils.parseFields(output,
                0,
                GROUP_LINE::matcher,
                matcher -> matcher.group("groupname"),
                value -> new PeerGroupKey(value));
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<PeerGroup> id, PeerGroupBuilder builder, ReadContext ctx)
            throws ReadFailedException {
        String name = id.firstKeyOf(PeerGroup.class).getPeerGroupName();
        builder.setPeerGroupName(name);
        builder.setKey(new PeerGroupKey(name));
    }

    public static Long readAsNumberFromContext(InstanceIdentifier<?> iid, WriteContext context, Boolean delete) {
        Optional<Config> gc;
        if (delete) {
            gc = context.readBefore(RWUtils.cutId(iid, Bgp.class)
                .child(Global.class)
                .child(Config.class));
        } else {
            gc = context.readAfter(RWUtils.cutId(iid, Bgp.class)
                .child(Global.class)
                .child(Config.class));
        }
        if (!gc.isPresent()) {
            return null;
        }
        return gc.get().getAs().getValue();
    }

    public static Long readAsNumberFromContext(InstanceIdentifier<?> iid, ReadContext context) {
        Optional<Config> gc = context.read(RWUtils.cutId(iid, Bgp.class)
                .child(Global.class)
                .child(Config.class));
        if (!gc.isPresent()) {
            return null;
        }
        return gc.get().getAs().getValue();
    }
}