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

package io.frinx.cli.unit.ios.rib.handler;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliOperListReader;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.afi.safi.StateBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AfiSafiReader implements CliOperListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    public AfiSafiReader() {

    }

    @NotNull
    @Override
    public List<AfiSafiKey> getAllIds(@NotNull InstanceIdentifier<AfiSafi> instanceIdentifier, @NotNull ReadContext
            readContext) throws ReadFailedException {
        return Lists.newArrayList(new AfiSafiKey(IPV4UNICAST.class), new AfiSafiKey(IPV6UNICAST.class));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<AfiSafi> instanceIdentifier, @NotNull
            AfiSafiBuilder afiSafiBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class)
                .getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
        afiSafiBuilder.setState(new StateBuilder().setAfiSafiName(key)
                .build());
    }
}