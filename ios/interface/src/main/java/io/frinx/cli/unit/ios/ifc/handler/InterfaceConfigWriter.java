/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.handler;

import static com.google.common.base.Preconditions.checkArgument;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (data.getType() == SoftwareLoopback.class) {
            writeLoopbackInterface(id, data, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Cannot create interface of type: " + data.getType()));
        }
    }

    private static final Pattern LOOPBACK_NAME_PATTERN = Pattern.compile("Loopback(?<number>[0-9]+)");

    private void writeLoopbackInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        Matcher matcher = LOOPBACK_NAME_PATTERN.matcher(data.getName());
        try {
            checkArgument(matcher.matches(),
                    "Loopback name must be in format: Loopback45, not: %s", data.getName());
        } catch (RuntimeException e) {
            throw new WriteFailedException.CreateFailedException(id, data, e);
        }

        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("interface loopback %s", matcher.group("number")),
                f("description %s", data.getDescription()),
                data.isEnabled() ? "no shutdown" : "shutdown",
                "exit",
                "exit");
    }

    public static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES = Collections.singleton(EthernetCsmacd.class);

    public static boolean isPhysicalInterface(Config data) {
        return PHYS_IFC_TYPES.contains(data.getType());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                    "Changing interface type is not permitted. Before: %s, After: %s",
                    dataBefore.getType(), dataAfter.getType());
        } catch (RuntimeException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }

        if (isPhysicalInterface(dataAfter)) {
            updatePhysicalInterface(id, dataAfter, writeContext);
        } else if (dataAfter.getType() == SoftwareLoopback.class) {
            writeLoopbackInterface(id, dataAfter, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, dataAfter,
                    new IllegalArgumentException("Unknown interface type: " + dataAfter.getType()));
        }
    }

    private void updatePhysicalInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("interface %s", data.getName()),
                data.getDescription() == null ? "" : f("description %s", data.getDescription()),
                data.getMtu() == null ? "" : f("mtu %s", data.getMtu()),
                data.isEnabled() ? "no shutdown" : "shutdown",
                "exit",
                "exit");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(dataBefore)) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        } else if (dataBefore.getType() == SoftwareLoopback.class) {
            deleteLoopbackInterface(id, dataBefore, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, dataBefore,
                    new IllegalArgumentException("Unknown interface type: " + dataBefore.getType()));
        }
    }

    private void deleteLoopbackInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {
        Matcher matcher = LOOPBACK_NAME_PATTERN.matcher(data.getName());
        try {
            checkArgument(matcher.matches(),
                    "Loopback name must be in format: Loopback45, not: %s", data.getName());
        } catch (RuntimeException e) {
            throw new WriteFailedException.DeleteFailedException(id, e);
        }

        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("no interface loopback %s", matcher.group("number")),
                "exit");
    }
}
