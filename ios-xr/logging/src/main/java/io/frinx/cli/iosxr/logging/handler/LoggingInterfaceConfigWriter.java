/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.logging.handler;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging._interface.config.EnabledLoggingForEvent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LoggingInterfaceConfigWriter implements CliWriter<Config> {

    private final Cli cli;
    private static final String NO_LOGGING_COMMAND = "no logging events link-status";
    private static final String LOGGING_COMMAND = "logging events link-status";

    public LoggingInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = dataAfter.getInterfaceId().getValue();

        checkLoggingConfig(ifcName, writeContext, true);

        List<EnabledLoggingForEvent> enabledLoggingForEventList = dataAfter.getEnabledLoggingForEvent();

        String command = enabledLoggingForEventList == null || enabledLoggingForEventList.isEmpty()
                ? NO_LOGGING_COMMAND : LOGGING_COMMAND;

        if (enabledLoggingForEventList != null && !enabledLoggingForEventList.isEmpty()) {
            Preconditions.checkArgument(LoggingInterfacesReader.LINK_UP_DOWN_EVENT_LIST.equals(enabledLoggingForEventList),
                    "Cannot configure logging events %s for interface %s, only %s event is supported",
                    enabledLoggingForEventList, ifcName, LoggingInterfacesReader.LINK_UP_DOWN_EVENT);
        }

        blockingWriteAndRead(cli, id, dataAfter,
                "conf t",
                f("interface %s", ifcName),
                command,
                "commit",
                "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = dataBefore.getInterfaceId().getValue();

        // TODO this check is possibly not needed for delete
        checkLoggingConfig(ifcName, writeContext, false);

        blockingDeleteAndRead(cli, id,
                "conf t",
                f("interface %s", ifcName),
                NO_LOGGING_COMMAND,
                "commit",
                "end");
    }

    private static void checkLoggingConfig(String ifcName, WriteContext wContext, boolean isWrite) {
        InstanceIdentifier<Interface> ifcId = IIDs.INTERFACES.child(Interface.class, new InterfaceKey(ifcName));
        final Optional<Interface> optionalIfc;
        if (isWrite) {
            optionalIfc = wContext.readAfter(ifcId);
        } else {
            optionalIfc = wContext.readBefore(ifcId);
        }

        Preconditions.checkArgument(optionalIfc.isPresent(),
                "Cannot configure logging on non-existent interface %s", ifcName);

        Class<? extends InterfaceType> type = optionalIfc.get().getConfig().getType();
        
        Preconditions.checkArgument(Ieee8023adLag.class.equals(type) || SoftwareLoopback.class.equals(type),
                "Cannot configure logging on ethernet interface %s of type %s, "
                        + "logging is supported only on Ethernet and LAG interfaces", ifcName, type);
    }
}
