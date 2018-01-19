/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.mpls.MplsListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te.InterfaceAttributesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class RsvpInterfaceReader implements MplsListReader.MplsConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private Cli cli;

    private static final String SH_RSVP_INT = "show run rsvp";
    static final Pattern IFACE_LINE = Pattern.compile("interface (?<name>.*)");

    public RsvpInterfaceReader(Cli cli) {
        this.cli = cli;
    }


    @Override
    public List<InterfaceKey> getAllIdsForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                               @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_RSVP_INT, cli, instanceIdentifier, readContext);
        return getInterfaceKeys(output);
    }

    @VisibleForTesting
    static List<InterfaceKey> getInterfaceKeys(String output) {
        return ParsingUtils.parseFields(output.replaceAll("\\h+", " "), 0,
            IFACE_LINE::matcher,
            matcher -> matcher.group("name"),
            v -> new InterfaceKey(new InterfaceId(v)));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> readData) {
        ((InterfaceAttributesBuilder) builder).setInterface(readData);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier, @Nonnull InterfaceBuilder interfaceBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        InterfaceKey key = instanceIdentifier.firstKeyOf(Interface.class);
        interfaceBuilder.setInterfaceId(key.getInterfaceId());
    }

}
