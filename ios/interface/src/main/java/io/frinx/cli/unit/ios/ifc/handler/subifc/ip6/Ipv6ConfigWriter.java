/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip6;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;

public class Ipv6ConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public Ipv6ConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = "configure terminal\n" +
            "interface %s\n" +
            "%s\n" +
            "end";
    private static final Pattern LINK_LOCAL = Pattern.compile("[Ff][Ee][89AaBb].*");

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId != ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }

        blockingWriteAndRead(cli, instanceIdentifier, config,
                f(WRITE_TEMPLATE,
                        getIfcName(instanceIdentifier),
                        createAddressCommand(config)));
    }

    private static String createAddressCommand(Config config) {
        String ip = config.getIp().getValue();
        if (isLinkLocal(ip)) {
            return "ipv6 address " + ip + " link-local";
        }
        else {
            return "ipv6 address " + ip + "/" + config.getPrefixLength().toString();
        }
    }

    /*
     * Check if address belongs to subnet FE80::/10
     */
    private static boolean isLinkLocal(String ip) {
        return LINK_LOCAL.matcher(ip).find();
    }


    private static String getIfcName(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subIfcIndex = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();
        checkArgument(subIfcIndex == ZERO_SUBINTERFACE_ID, "Only subinterface " + ZERO_SUBINTERFACE_ID + "  can have IP");
        return ifcName;
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config before,
                                        @Nonnull Config after,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            deleteCurrentAttributes(instanceIdentifier, before, writeContext);
            writeCurrentAttributes(instanceIdentifier, after, writeContext);
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(instanceIdentifier, before, after, e);
        }
    }

    private static final String DELETE_TEMPLATE = "configure terminal\n" +
            "interface %s\n" +
            "no %s\n" +
            "end";

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId == ZERO_SUBINTERFACE_ID) {

            try {
                blockingWriteAndRead(cli, instanceIdentifier, config,
                        f(DELETE_TEMPLATE,
                                getIfcName(instanceIdentifier),
                                createAddressCommand(config)));
            } catch (WriteFailedException.CreateFailedException e) {
                throw new WriteFailedException.DeleteFailedException(instanceIdentifier, e);
            }
        } else {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }
    }
}
