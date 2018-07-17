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

package io.frinx.cli.ios.rib.handler;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliOperListReader;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.afi.safi.StateBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AfiSafiReader implements CliOperListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    public AfiSafiReader() {

    }

    @Nonnull
    @Override
    public List<AfiSafiKey> getAllIds(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier, @Nonnull ReadContext
            readContext) throws ReadFailedException {
        return Lists.newArrayList(new AfiSafiKey(IPV4UNICAST.class), new AfiSafiKey(IPV6UNICAST.class));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull List<AfiSafi> readValue) {
        ((AfiSafisBuilder) parentBuilder).setAfiSafi(readValue);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier, @Nonnull
            AfiSafiBuilder afiSafiBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class)
                .getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
        afiSafiBuilder.setState(new StateBuilder().setAfiSafiName(key)
                .build());
    }
}
