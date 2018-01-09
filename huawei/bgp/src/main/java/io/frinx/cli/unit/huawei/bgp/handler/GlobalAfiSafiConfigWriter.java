/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.bgp.handler;

import static com.google.common.base.Preconditions.checkArgument;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalAfiSafiConfigWriter implements BgpWriter<Config> {

    private static final String GLOBAL_BGP_AFI_SAFI = "system-view\n" +
            "bgp %s\n" +
            "ipv4-family %s\n" +
            "return\n" +
            "commit";

    private static final String GLOBAL_BGP_AFI_SAFI_DELETE = "system-view\n" +
            "bgp %s\n" +
            "undo ipv4-family %s\n" +
            "return\n" +
            "commit";

    private static final String VRF_BGP_AFI_SAFI = "system-view\n" +
            "bgp %s\n" +
            "ipv4-family vpn-instance %s\n" +
            "return\n" +
            "commit";

    private static final String VRF_BGP_AFI_SAFI_DELETE = "system-view\n" +
            "bgp %s\n" +
            "undo ipv4-family vpn-instance %s\n" +
            "return\n" +
            "commit";

    static final String VRF_BGP_AFI_SAFI_ROUTER_ID = "system-view\n" +
            "bgp %s\n" +
            "ipv4-family vpn-instance %s\n" +
            "router-id %s\n" +
            "return\n" +
            "commit";

    private Cli cli;

    public GlobalAfiSafiConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id,
                                              Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();
        Long as = writeContext.readAfter(RWUtils.cutId(id, Global.class)).get().getConfig().getAs().getValue();

        if(vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            blockingWriteAndRead(f(GLOBAL_BGP_AFI_SAFI,
                    as, toDeviceAddressFamily(config.getAfiSafiName())),
                    cli, id, config);
        } else {
            checkArgument(writeContext.readAfter(RWUtils.cutId(id, NetworkInstance.class)).get().getConfig().getRouteDistinguisher() != null,
                    "Route distinguisher missing for VRF: %s. Cannot configure BGP afi/safi", vrfName);

            DottedQuad routerId = writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).get().getGlobal().getConfig().getRouterId();

            if(routerId == null) {
                blockingWriteAndRead(f(VRF_BGP_AFI_SAFI,
                        as, toDeviceAddressFamily(config.getAfiSafiName()), vrfName),
                        cli, id, config);
            } else {
                blockingWriteAndRead(f(VRF_BGP_AFI_SAFI_ROUTER_ID,
                        as, toDeviceAddressFamily(config.getAfiSafiName()), vrfName, routerId.getValue()),
                        cli, id, config);
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
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id,
                                               Config dataBefore,
                                               Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        // No point in updating a single command
        // FIXME Update here is dangerous, deleting and readding address-family is not an atomic operation
        // on IOS and the deletion is performed in background without freezing the delete command
        // then the subsequent "add afi command" fails. So not updating the address family here is safer for now
        // The downside is that we set router-id here under address-family if we are under a VRF. This means that
        // updates to router-id for VRFs bgp configuration does not work properly
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id,
                                               Config config,
                                               WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();
        Long as = writeContext.readBefore(RWUtils.cutId(id, Global.class)).get().getConfig().getAs().getValue();

        if(vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
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
