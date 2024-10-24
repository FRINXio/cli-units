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

package io.frinx.cli.unit.huawei.bgp.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalAfiSafiConfigWriter implements CliWriter<Config> {

    private static final String GLOBAL_BGP_AFI_SAFI = """
            system-view
            bgp %s
            ipv4-family %s
            commit
            return""";

    private static final String GLOBAL_BGP_AFI_SAFI_DELETE = """
            system-view
            bgp %s
            undo ipv4-family %s
            commit
            return""";

    private static final String VRF_BGP_AFI_SAFI = """
            system-view
            bgp %s
            ipv4-family vpn-instance %s
            commit
            return""";


    private static final String VRF_BGP_AFI_SAFI_DELETE = """
            system-view
            bgp %s
            undo ipv4-family vpn-instance %s
            commit
            return""";

    static final String VRF_BGP_AFI_SAFI_ROUTER_ID = """
            system-view
            bgp %s
            ipv4-family vpn-instance %s
            router-id %s
            commit
            return""";

    private Cli cli;

    public GlobalAfiSafiConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
            @NotNull Config config, @NotNull WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();
        Long as = writeContext.readAfter(RWUtils.cutId(id, Global.class)).get().getConfig().getAs().getValue();

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            blockingWriteAndRead(f(GLOBAL_BGP_AFI_SAFI, as, toDeviceAddressFamily(config.getAfiSafiName())), cli, id,
                    config);
        } else {
            Preconditions.checkArgument(writeContext.readAfter(RWUtils.cutId(id, NetworkInstance.class))
                    .get().getConfig().getRouteDistinguisher()
                    != null, "Route distinguisher missing for VRF: %s. Cannot configure BGP afi/safi", vrfName);

            DottedQuad routerId = writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).get().getGlobal().getConfig()
                    .getRouterId();

            if (routerId
                    == null) {
                blockingWriteAndRead(f(VRF_BGP_AFI_SAFI, as, toDeviceAddressFamily(config.getAfiSafiName()), vrfName),
                        cli, id, config);
            } else {
                blockingWriteAndRead(f(VRF_BGP_AFI_SAFI_ROUTER_ID, as, toDeviceAddressFamily(config.getAfiSafiName()),
                        vrfName, routerId.getValue()), cli, id, config);
            }
        }
    }

    public static String toDeviceAddressFamily(Class<? extends AFISAFITYPE> afiSafiName) {
        if (afiSafiName.equals(IPV4UNICAST.class)) {
            return "unicast";
        } else if (afiSafiName.equals(L3VPNIPV4UNICAST.class)) {
            return "vpnv4";
        } else {
            throw new IllegalArgumentException("Unsupported afi safi type: " + afiSafiName);
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // No point in updating a single command
        // FIXME Update here is dangerous, deleting and readding address-family is not an atomic operation
        // on IOS and the deletion is performed in background without freezing the delete command
        // then the subsequent "add afi command" fails. So not updating the address family here is safer for now
        // The downside is that we set router-id here under address-family if we are under a VRF. This means that
        // updates to router-id for VRFs bgp configuration does not work properly
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();
        Long as = writeContext.readBefore(RWUtils.cutId(id, Global.class)).get().getConfig().getAs().getValue();

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            blockingWriteAndRead(f(GLOBAL_BGP_AFI_SAFI_DELETE,
                    as, toDeviceAddressFamily(config.getAfiSafiName())),
                    cli, id, config);
        } else {
            blockingWriteAndRead(f(VRF_BGP_AFI_SAFI_DELETE,
                    as, toDeviceAddressFamily(config.getAfiSafiName()), vrfName),
                    cli, id, config);
        }
    }
}