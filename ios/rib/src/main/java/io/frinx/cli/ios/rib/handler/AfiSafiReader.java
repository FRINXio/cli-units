/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.rib.handler;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliListReader;
import java.util.List;

import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafi;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.afi.safi.StateBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class AfiSafiReader implements CliListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    public AfiSafiReader() {

    }

    @Nonnull
    @Override
    public List<AfiSafiKey> getAllIds(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        return Lists.newArrayList(new AfiSafiKey(IPV4UNICAST.class), new AfiSafiKey(IPV6UNICAST.class));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull List<AfiSafi> readValue) {
        ((AfiSafisBuilder) parentBuilder).setAfiSafi(readValue);
    }

    @Nonnull
    @Override
    public AfiSafiBuilder getBuilder(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier) {
        return new AfiSafiBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier, @Nonnull AfiSafiBuilder afiSafiBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
        afiSafiBuilder.setState(new StateBuilder().setAfiSafiName(key).build());
    }
}
