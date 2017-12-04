/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4;

import static com.google.common.base.Preconditions.checkArgument;
import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4ConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public Ipv4ConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = "configure terminal\n" +
            "interface %s\n" +
            "ip address %s %s\n" +
            "commit\n" +
            "end";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId != ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }

        SubnetUtils.SubnetInfo info = getSubnetInfo(config);

        blockingWriteAndRead(cli, instanceIdentifier, config,
                f(WRITE_TEMPLATE,
                        getIfcName(instanceIdentifier),
                        config.getIp().getValue(),
                        info.getNetmask()));
    }

    private static SubnetUtils.SubnetInfo getSubnetInfo(@Nonnull Config config) {
        return new SubnetUtils(config.getIp().getValue() + "/" + config.getPrefixLength()).getInfo();
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
            "no ip address %s %s\n" +
            "commit\n" +
            "end";

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId == ZERO_SUBINTERFACE_ID) {
            SubnetUtils.SubnetInfo info = getSubnetInfo(config);

            try {
                blockingWriteAndRead(cli, instanceIdentifier, config,
                        f(DELETE_TEMPLATE,
                                getIfcName(instanceIdentifier),
                                config.getIp().getValue(),
                                info.getNetmask()));
            } catch (WriteFailedException.CreateFailedException e) {
                throw new WriteFailedException.DeleteFailedException(instanceIdentifier, e);
            }
        } else {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }
    }
}
