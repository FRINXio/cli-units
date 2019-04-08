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

package io.frinx.cli.ospf.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.unit.utils.CliConfigReader;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    @Override
    public void readCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier, ConfigBuilder
            configBuilder, ReadContext readContext) {
        configBuilder.setIdentifier(instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier());
    }

}
