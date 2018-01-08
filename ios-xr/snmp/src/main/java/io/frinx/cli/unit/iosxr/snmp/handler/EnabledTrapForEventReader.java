/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.snmp.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.event.types.rev171024.LINKUPDOWN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp._interface.config.EnabledTrapForEvent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp._interface.config.EnabledTrapForEventBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp._interface.config.EnabledTrapForEventKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.ConfigBuilder;


import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class EnabledTrapForEventReader implements CliListReader<EnabledTrapForEvent, EnabledTrapForEventKey, EnabledTrapForEventBuilder> {

    private Cli cli;

    public EnabledTrapForEventReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<EnabledTrapForEventKey> getAllIds(@Nonnull InstanceIdentifier<EnabledTrapForEvent> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        return Lists.newArrayList(new EnabledTrapForEventKey(LINKUPDOWN.class));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<EnabledTrapForEvent> list) {
        ((ConfigBuilder) builder).setEnabledTrapForEvent(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<EnabledTrapForEvent> instanceIdentifier, @Nonnull EnabledTrapForEventBuilder enabledTrapForEventBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final InterfaceId id = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        parseEvent(blockingRead(String.format(InterfaceReader.SHOW_SNMP_INTERFACE, id.getValue()), cli, instanceIdentifier, readContext), enabledTrapForEventBuilder);
    }

    @VisibleForTesting
    public static void parseEvent(String output, EnabledTrapForEventBuilder builder) {
        Optional<String> upDown = ParsingUtils.parseField(output.replaceAll("\\h+", " "), 0,
                InterfaceReader.INTERFACE_PATTERN::matcher,
                matcher -> matcher.group("updown"));
        if (upDown.isPresent() && "enable".equals(upDown.get())) {
            builder.setEventName(LINKUPDOWN.class);
        }
    }
}
