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

package io.frinx.cli.unit.huawei.network.instance.handler;

import io.fd.honeycomb.translate.spi.read.ListReaderCustomizer;
import io.frinx.cli.handlers.def.DefaultReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.huawei.network.instance.handler.l3vrf.L3VrfReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NetworkInstanceReader extends CompositeListReader<NetworkInstance, NetworkInstanceKey,
        NetworkInstanceBuilder>
        implements CliConfigListReader<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder> {

    public NetworkInstanceReader(Cli cli) {
        super(new ArrayList<ListReaderCustomizer<NetworkInstance, NetworkInstanceKey, NetworkInstanceBuilder>>() {{
                add(new DefaultReader());
                add(new L3VrfReader(cli));
            }
        });
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<NetworkInstance> list) {
        ((NetworkInstancesBuilder) builder).setNetworkInstance(list);
    }

    @Nonnull
    @Override
    public NetworkInstanceBuilder getBuilder(@Nonnull InstanceIdentifier<NetworkInstance> instanceIdentifier) {
        return new NetworkInstanceBuilder();
    }
}
