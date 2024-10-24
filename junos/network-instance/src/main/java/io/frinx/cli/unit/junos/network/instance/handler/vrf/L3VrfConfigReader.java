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

package io.frinx.cli.unit.junos.network.instance.handler.vrf;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.vrf.AbstractL3VrfConfigReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfConfigReader extends AbstractL3VrfConfigReader {

    public L3VrfConfigReader(Cli cli) {
        super(new L3VrfReader(cli), cli);
    }

    @Override
    public void readCurrentAttributes(
            @NotNull InstanceIdentifier<Config> instanceIdentifier,
            @NotNull ConfigBuilder configBuilder,
            @NotNull ReadContext readContext) throws ReadFailedException {
        String niName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(niName)) {
            return;
        }
        super.readCurrentAttributes(instanceIdentifier, configBuilder, readContext);
    }

    @Override
    protected String getReadCommand() {
        return "";
    }
}