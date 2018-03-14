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

package io.frinx.cli.iosxr.bgp.handler.neighbor;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeighborTransportConfigWriter  implements BgpWriter<Config> {

    private Cli cli;
    private Pattern LOOPBACK_PATTERN = Pattern.compile("[Ll]oopback(?<index>\\d+)");

    static final String MOD_CURR_ATTR = "router bgp {$as.config.as.value} {$instName}\n" +
            "neighbor {$neighbAddr}\n" +
            "{% if ($isLoopback == TRUE) %}update-source loopback {$match:}\n{%else%}no update-source\n{%endif}" +
            "exit\n" +
            "exit";

    public NeighborTransportConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id, Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get().getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        if (config.getLocalAddress() == null) {
            return;
        }
        Matcher matcher = LOOPBACK_PATTERN.matcher(config.getLocalAddress().getString());
        if (!matcher.matches()) {
            return;
        }
        blockingWriteAndRead(cli, id, config, fT(MOD_CURR_ATTR,
                "as", g,
                "instName", instName,
                "neighbAddr", new String(id.firstKeyOf(Neighbor.class).getNeighborAddress().getValue()),
                "isLoopback", true,
                "match", matcher.group("index")));
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get().getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        boolean isLoopback = false;
        Matcher matcher = null;
        if (dataAfter.getLocalAddress() != null) {
            matcher = LOOPBACK_PATTERN.matcher(dataAfter.getLocalAddress().getString());
            if (matcher.matches()) {
                isLoopback = true;
            }
        }
        blockingWriteAndRead(cli, id, dataAfter, fT(MOD_CURR_ATTR,
                "as", g,
                "instName", instName,
                "neighbAddr", new String(id.firstKeyOf(Neighbor.class).getNeighborAddress().getValue()),
                "isLoopback", isLoopback,
                "match", matcher.group("index")));
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id, Config config, WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        if (!bgpOptional.isPresent()) {
            return;
        }
        final Global g = bgpOptional.get().getGlobal();
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        blockingDeleteAndRead(cli, id, fT(MOD_CURR_ATTR,
                "as", g,
                "instName", instName,
                "neighbAddr", new String(id.firstKeyOf(Neighbor.class).getNeighborAddress().getValue())));
    }
}
