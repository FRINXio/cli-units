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

import io.frinx.cli.io.Cli;
import io.frinx.cli.ni.base.handler.vrf.AbstractL3VrfConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;

public final class L3VrfConfigWriter extends AbstractL3VrfConfigWriter {

    private static final String UPDATE_TEMPLATE = "set routing-instances {$data.name} instance-type virtual-router";

    private static final String DELETE_TEMPLATE = "delete routing-instances {$data.name}";

    public L3VrfConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        return fT(UPDATE_TEMPLATE, "data", after);
    }

    @Override
    protected String deleteTemplate(Config config) {
        return fT(DELETE_TEMPLATE, "data", config);
    }
}
