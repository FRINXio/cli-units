/*
 * Copyright © 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6ConfigWriter implements CliWriter<Config> {

    private static final String NO_IPV6_ADDRESS_CMD = "no ipv6 address";
    private final Cli cli;
    static final String MISSING_IP_ADDRESS_MSG = "Missing IP address";
    static final String MISSING_PREFIX_LENGTH_MSG = "Missing prefix length";

    public Ipv6ConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataAfter,
                                       @Nonnull final WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();

        validateConfig(id, dataAfter, ifcName);

        // ipv6 <address> <IPv6 Prefix/length>
        String dampConfCommand = f("ipv6 address %s/%s", dataAfter.getIp().getValue(), dataAfter.getPrefixLength());

        blockingWriteAndRead(cli, id, dataAfter,
            f("interface %s", ifcName),
            dampConfCommand,
            "exit");
    }

    private void validateConfig(final InstanceIdentifier<Config> id,
                                final Config dataAfter, final String ifcName) {
        Long subIfcIndex = id.firstKeyOf(Subinterface.class).getIndex();

        Preconditions.checkArgument(dataAfter.getIp() != null, MISSING_IP_ADDRESS_MSG);
        Preconditions.checkArgument(dataAfter.getPrefixLength() != null, MISSING_PREFIX_LENGTH_MSG);
        Ipv6CheckUtil.checkParentInterfaceTypeWithExeption(ifcName, EthernetCsmacd.class, Ieee8023adLag.class,
            SoftwareLoopback.class);
        Ipv6CheckUtil.checkSubInterfaceIdWithExeption(subIfcIndex, SubinterfaceReader.ZERO_SUBINTERFACE_ID);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter, @Nonnull final WriteContext writeContext)
        throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id, @Nonnull final Config dataBefore,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        Long subifcIndex = id.firstKeyOf(Subinterface.class).getIndex();

        blockingDeleteAndRead(cli, id,
            f("interface %s", ifcName, subifcIndex),
            NO_IPV6_ADDRESS_CMD,
            "exit");
    }
}
