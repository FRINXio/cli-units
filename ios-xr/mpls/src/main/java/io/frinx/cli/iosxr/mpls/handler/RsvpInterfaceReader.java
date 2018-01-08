/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.mpls.MplsListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceStateReader;
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

    private static final String SH_RSVP_INT = "show rsvp interface";
    static final Pattern IFACE_LINE = Pattern.compile("(?<name>[^\\s]+) (?<bandwidth>[0-9]+)(K?) (?<flow>[^\\s]+)(K?) (?<allocated>[0-9]+) \\( (?<bps>[0-9]+)%\\) (?<maxsub>.*)");

    public RsvpInterfaceReader(Cli cli) {
        this.cli = cli;
    }


    @Override
    public List<InterfaceKey> getAllIdsForType(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                               @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_RSVP_INT, cli, instanceIdentifier, readContext);

        // Interfaces' names can be abbreviated in the output of
        // 'show rsvp interface' command. We have to map them to
        // their non-abbreviated names..
        List<InterfaceKey> shortKeys = getShortInterfaceKeys(output);

        List<InterfaceKey> longKeys = Lists.newArrayList();
        for (InterfaceKey shortKey : shortKeys) {
            String shIfcOutput = blockingRead(
                    String.format(InterfaceStateReader.SH_SINGLE_INTERFACE, shortKey.getInterfaceId().getValue()),
                    cli, instanceIdentifier, readContext);

            longKeys.add(getLongKey(shIfcOutput));
        }

        return longKeys;
    }

    @VisibleForTesting
    static List<InterfaceKey> getShortInterfaceKeys(String output) {
        return ParsingUtils.parseFields(output.replaceAll("\\h+", " "), 0,
            IFACE_LINE::matcher,
            matcher -> matcher.group("name"),
            v -> new InterfaceKey(new InterfaceId(v)));
    }

    @VisibleForTesting
    static InterfaceKey getLongKey(String output) {
        return ParsingUtils.parseFields(output, 0,
                InterfaceStateReader.STATUS_LINE::matcher,
                matcher -> matcher.group("id"),
                longIfcName -> new InterfaceKey(new InterfaceId(longIfcName)))
                // TODO we should check if the stream is actually non-empty
                .stream().findFirst().get();
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
