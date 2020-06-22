/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.logical.ring.handler.virtual;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliConfigReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.VirtualRing;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.virtual.ring.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VirtualRingConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        configBuilder.setName(instanceIdentifier.firstKeyOf(VirtualRing.class).getName());
    }
}
