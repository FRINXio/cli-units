/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.brocade.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.SubinterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceReader implements CliConfigListReader<Subinterface, SubinterfaceKey, SubinterfaceBuilder> {

    public static final long ZERO_SUBINTERFACE_ID = 0L;

    private Cli cli;

    public SubinterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<SubinterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier,
                                           @Nonnull ReadContext readContext) throws ReadFailedException {
        // Subinterface with ID 0 is reserved for IP addresses of the interface
        InstanceIdentifier<Subinterface> zeroSubIfaceIid = RWUtils.replaceLastInId(instanceIdentifier,
                new InstanceIdentifier.IdentifiableItem<>(Subinterface.class, new SubinterfaceKey(ZERO_SUBINTERFACE_ID)));
        boolean hasIpv4Address = readContext.read(zeroSubIfaceIid.augmentation(Subinterface1.class)).isPresent();
        boolean hasIpv6Address = readContext.read(zeroSubIfaceIid.augmentation(Subinterface2.class)).isPresent();
        if (hasIpv4Address || hasIpv6Address) {
            return Collections.singletonList(new SubinterfaceKey(ZERO_SUBINTERFACE_ID));
        }

        return Collections.emptyList();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Subinterface> list) {
        ((SubinterfacesBuilder) builder).setSubinterface(list);
    }

    @Nonnull
    @Override
    public SubinterfaceBuilder getBuilder(@Nonnull InstanceIdentifier<Subinterface> instanceIdentifier) {
        return new SubinterfaceBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Subinterface> id,
                                      @Nonnull SubinterfaceBuilder builder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        builder.setIndex(id.firstKeyOf(Subinterface.class).getIndex());
    }
}
