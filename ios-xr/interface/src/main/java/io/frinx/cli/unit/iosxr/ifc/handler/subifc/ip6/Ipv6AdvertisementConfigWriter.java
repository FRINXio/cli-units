/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6AdvertisementConfigWriter implements CliWriter<Config> {

    private static final String IPV6_ND_SUPPRESS = "ipv6 nd suppress";
    private static final String NO_IPV6_ND_SUPPRESS = "no ipv6 nd suppress";
    private final Cli cli;

    public Ipv6AdvertisementConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataAfter,
                                       @Nonnull final WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        Long subIfcIndex = id.firstKeyOf(Subinterface.class).getIndex();

        Ipv6CheckUtil.checkParentInterfaceTypeWithExeption(ifcName, EthernetCsmacd.class, Ieee8023adLag.class);
        Ipv6CheckUtil.checkSubInterfaceIdWithExeption(subIfcIndex, SubinterfaceReader.ZERO_SUBINTERFACE_ID);

        String dampConfCommand = getAdvertisementCommand(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
            f("interface %s", ifcName),
            dampConfCommand,
            "exit");
    }

    private String getAdvertisementCommand(final Config dataAfter) {
        if (dataAfter.isSuppress() != null && dataAfter.isSuppress()) {
            return IPV6_ND_SUPPRESS;
        } else {
            return NO_IPV6_ND_SUPPRESS;
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataBefore,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        Long subIfcIndex = id.firstKeyOf(Subinterface.class).getIndex();

        Ipv6CheckUtil.checkParentInterfaceTypeWithExeption(ifcName, EthernetCsmacd.class, Ieee8023adLag.class);
        Ipv6CheckUtil.checkSubInterfaceIdWithExeption(subIfcIndex, SubinterfaceReader.ZERO_SUBINTERFACE_ID);

        blockingDeleteAndRead(cli, id,
            f("interface %s", ifcName),
            NO_IPV6_ND_SUPPRESS,
            "exit");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter, @Nonnull final WriteContext writeContext)
        throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }
}
