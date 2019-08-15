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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupListReader implements CliConfigListReader<PeerGroup, PeerGroupKey, PeerGroupBuilder> {

    static final String READ_NBR_GROUPS_CMD = "show running-config router bgp %s %s %s | include neighbor-group";

    private static final Pattern GROUP_LINE = Pattern.compile("neighbor\\-group (?<groupname>\\S+)");
    private Cli cli;

    public PeerGroupListReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<PeerGroupKey> getAllIds(@Nonnull InstanceIdentifier<PeerGroup> iid, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        Long as = readAsNumberFromContext(iid, readContext);
        if (as == null) {
            return Collections.emptyList();
        }
        final String protName = iid.firstKeyOf(Protocol.class).getName();
        final String instance = BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(protName)
                ? "" : String.format("instance %s", protName);
        String nwInsName = GlobalConfigWriter.resolveVrfWithName(iid);
        return parseAllIds(blockingRead(f(READ_NBR_GROUPS_CMD, as, instance, nwInsName), cli, iid, readContext));
    }

    @VisibleForTesting
    public List<PeerGroupKey> parseAllIds(String output) {
        return ParsingUtils.parseFields(output,
            0,
            GROUP_LINE::matcher,
            matcher -> matcher.group("groupname"),
            PeerGroupKey::new);
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<PeerGroup> id, PeerGroupBuilder builder,
                                      @Nonnull ReadContext ctx) {
        String name = id.firstKeyOf(PeerGroup.class).getPeerGroupName();
        builder.setPeerGroupName(name);
        builder.setKey(new PeerGroupKey(name));
    }

    static Long readAsNumberFromContext(InstanceIdentifier<?> iid, WriteContext context, Boolean delete) {
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

    static Long readAsNumberFromContext(InstanceIdentifier<?> iid, ReadContext context) {
        Optional<Config> gc = context.read(RWUtils.cutId(iid, Bgp.class)
                .child(Global.class)
                .child(Config.class));
        if (!gc.isPresent()) {
            return null;
        }
        return gc.get().getAs().getValue();
    }
}