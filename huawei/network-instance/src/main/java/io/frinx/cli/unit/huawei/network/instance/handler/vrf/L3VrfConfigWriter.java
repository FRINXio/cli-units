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

package io.frinx.cli.unit.huawei.network.instance.handler.vrf;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.vrf.AbstractL3VrfConfigWriter;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L3VrfConfigWriter extends AbstractL3VrfConfigWriter {

    private static final String UPDATE_TEMPLATE = "system-view\n"
            + "ip vpn-instance {$data.name}\n"
            + "{$data|update(description,description `$data.description`\n,undo description\n)}"
            + "{% if($config.route_distinguisher) %}route-distinguisher {$config.route_distinguisher}\n{% endif %}"
            + "commit\n"
            + "return";

    private static final String DELETE_TEMPLATE = "system-view\n"
            + "undo ip vpn-instance {$data.name}\n"
            + "commit\n"
            + "return";

    public L3VrfConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        return fT(UPDATE_TEMPLATE, "before", before, "date", after);
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException.UpdateFailedException {
        RouteDistinguisher rdBefore = dataBefore.getRouteDistinguisher();
        RouteDistinguisher rdAfter = dataAfter.getRouteDistinguisher();
        Preconditions.checkArgument(Objects.equals(rdBefore, rdAfter),
                "Cannot update route distinguisher once l3vrf already created");

        return super.updateCurrentAttributesWResult(id, dataBefore, dataAfter, writeContext);
    }

    @Override
    protected String deleteTemplate(Config config) {
        return fT(DELETE_TEMPLATE, "data", config);
    }
}
