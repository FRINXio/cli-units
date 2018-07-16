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

        checkLoggingConfig(ifcName, writeContext);

        List<EnabledLoggingForEvent> enabledLoggingForEventList = dataAfter.getEnabledLoggingForEvent();

        String command = enabledLoggingForEventList == null || enabledLoggingForEventList.isEmpty()
                ? NO_LOGGING_COMMAND : LOGGING_COMMAND;

        if (enabledLoggingForEventList != null && !enabledLoggingForEventList.isEmpty()) {
            Preconditions.checkArgument(LoggingInterfacesReader.LINK_UP_DOWN_EVENT_LIST
                            .containsAll(enabledLoggingForEventList),
                    "Cannot configure logging events %s for interface %s, only %s event is supported",
                    enabledLoggingForEventList, ifcName, LoggingInterfacesReader.LINK_UP_DOWN_EVENT);
        }

        blockingWriteAndRead(cli, id, dataAfter,
                f("interface %s", ifcName),
                command,
                "root");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = dataBefore.getInterfaceId().getValue();

        blockingDeleteAndRead(cli, id,
                f("interface %s", ifcName),
                NO_LOGGING_COMMAND,
                "root");
    }

    private static void checkLoggingConfig(String ifcName, WriteContext writeContext) {
        InstanceIdentifier<Interface> ifcId = IIDs.INTERFACES.child(Interface.class, new InterfaceKey(ifcName));
        final Optional<Interface> optionalIfc = writeContext.readAfter(ifcId);
        Preconditions.checkArgument(optionalIfc.isPresent(),
                "Cannot configure logging on non-existent interface %s", ifcName);
    }
}
