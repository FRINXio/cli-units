/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.lr.handler.statics;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang._static.types.rev190610.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.AfiSafiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.ext.rev190610.AfiSafiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public StaticConfigReader(Cli cli) {

    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        StaticKey key = id.firstKeyOf(Static.class);
        IpPrefix pf = key.getPrefix();
        AfiSafiAugBuilder afiAugBuilder = new AfiSafiAugBuilder();
        afiAugBuilder.setAfiSafiType(pf.getIpv4Prefix() != null ? IPV4UNICAST.class : IPV6UNICAST.class);
        builder.setPrefix(pf);
        builder.addAugmentation(AfiSafiAug.class, afiAugBuilder.build());
    }
}