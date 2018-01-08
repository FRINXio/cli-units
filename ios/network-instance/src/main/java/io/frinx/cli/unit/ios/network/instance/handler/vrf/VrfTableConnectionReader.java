/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.TableConnectionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnection;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfTableConnectionReader implements
        L3VrfListReader.L3VrfConfigListReader<TableConnection, TableConnectionKey, TableConnectionBuilder> {

    private static final String OSPF_TO_BGP_DEFAULT_VRF =
            "sh run | include ^router ospf|^ redistribute bgp | exclude vrf";
    private static final String OSPF_TO_BGP = "sh run | include ^router ospf vrf %s|^ redistribute bgp";

    private static final String BGP_TO_OSPF_DEFAULT =
            "sh run  | include ^router bgp|^  redistribute ospf|^ address-family ipv4 | exclude vrf";
    private static final String BGP_TO_OSPF_VRF =
            "sh run  | include ^router bgp|^  redistribute ospf|^ address-family ipv4 vrf %s";

    private final Cli cli;

    public VrfTableConnectionReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<TableConnectionKey> getAllIdsForType(@Nonnull InstanceIdentifier<TableConnection> instanceIdentifier,
                                                     @Nonnull ReadContext readContext) throws ReadFailedException {
        List<TableConnectionKey> tableConnectionKeyes = Lists.newArrayList();

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ospfToBgpCommand =
                DEFAULT_NETWORK_NAME.equals(vrfName) ? OSPF_TO_BGP_DEFAULT_VRF : String.format(OSPF_TO_BGP, vrfName);
        String ospfToBgpOutput = blockingRead(ospfToBgpCommand, cli, instanceIdentifier, readContext);
        if (ParsingUtils.NEWLINE.split(ospfToBgpOutput).length == 2) {
            new TableConnectionKey(IPV4.class, OSPF.class, BGP.class);
        }

        String bgpToOspfCommand =
                DEFAULT_NETWORK_NAME.equals(vrfName) ? BGP_TO_OSPF_DEFAULT : String.format(BGP_TO_OSPF_VRF, vrfName);
        String bgpToOspfOutput = blockingRead(ospfToBgpCommand, cli, instanceIdentifier, readContext);

        if (ParsingUtils.NEWLINE.split(bgpToOspfOutput).length == 3) {
            new TableConnectionKey(IPV4.class, BGP.class, OSPF.class);
        }

        return tableConnectionKeyes;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<TableConnection> list) {
        ((TableConnectionsBuilder) builder).setTableConnection(list);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<TableConnection> instanceIdentifier,
                                             @Nonnull TableConnectionBuilder tableConnectionBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        TableConnectionKey tableConnectionKey = instanceIdentifier.firstKeyOf(TableConnection.class);

        tableConnectionBuilder
                .setAddressFamily(tableConnectionKey.getAddressFamily())
                .setDstProtocol(tableConnectionKey.getDstProtocol())
                .setSrcProtocol(tableConnectionKey.getSrcProtocol())
                .setConfig(new ConfigBuilder()
                        .setAddressFamily(tableConnectionKey.getAddressFamily())
                        .setDstProtocol(tableConnectionKey.getDstProtocol())
                        .setSrcProtocol(tableConnectionKey.getSrcProtocol())
                        .build())
                .build();
    }
}
