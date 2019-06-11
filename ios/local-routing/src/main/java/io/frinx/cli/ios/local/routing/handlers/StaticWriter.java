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

package io.frinx.cli.ios.local.routing.handlers;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHops;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticWriter implements CliListWriter<Static, StaticKey> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "ip route {$network} {$netmask} {$nexthop}\n"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no ip route {$network} {$netmask} {$nexthop}\n"
            + "end";

    private Cli cli;

    public StaticWriter(Cli cli) {
        this.cli = cli;
    }

    private static SubnetUtils.SubnetInfo getSubnetInfo(@Nonnull IpPrefix config) {
        return new SubnetUtils(new String(config.getValue())).getInfo();
    }

    private boolean checkPreconditions(NextHops nextHops) {
        return !(nextHops == null || nextHops.getNextHop() == null || nextHops.getNextHop().isEmpty());
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Static> id, Static config,
                                              WriteContext writeContext) throws WriteFailedException {
        NextHops nextHops = config.getNextHops();
        if (checkPreconditions(nextHops)) {
            writeStaticRoutes(id, config, nextHops.getNextHop());
        }
    }

    private void writeStaticRoutes(InstanceIdentifier<Static> id, Static config, Collection<NextHop> hops)
            throws WriteFailedException.CreateFailedException {
        for (NextHop nextHop : hops) {
            SubnetUtils.SubnetInfo info = getSubnetInfo(config.getPrefix());

            String insert = fT(WRITE_TEMPLATE,
                    "network", info.getAddress(),
                    "netmask", info.getNetmask(),
                    "nexthop", nextHop.getIndex());
            blockingWriteAndRead(cli, id, config, insert);
        }
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Static> id, Static dataBefore, Static dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        Set beforeHops = Sets.newHashSet(dataBefore.getNextHops().getNextHop());
        Set afterHops = Sets.newHashSet(dataAfter.getNextHops().getNextHop());
        Set removed = Sets.difference(beforeHops, afterHops);
        Set added = Sets.difference(afterHops, beforeHops);

        deleteStaticRoutes(id, dataBefore, removed);
        writeStaticRoutes(id, dataAfter, added);
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Static> id, Static config,
                                               WriteContext writeContext) throws WriteFailedException {
        NextHops nextHops = config.getNextHops();
        if (checkPreconditions(nextHops)) {
            deleteStaticRoutes(id, config, nextHops.getNextHop());
        }
    }

    private void deleteStaticRoutes(InstanceIdentifier<Static> id, Static config, Collection<NextHop> hops)
            throws WriteFailedException.DeleteFailedException {
        for (NextHop nextHop : hops) {

            SubnetUtils.SubnetInfo info = getSubnetInfo(config.getPrefix());

            String delete = fT(DELETE_TEMPLATE,
                    "network", info.getAddress(),
                    "netmask", info.getNetmask(),
                    "nexthop", nextHop.getIndex());
            blockingDeleteAndRead(cli, id, delete);
        }
    }
}

