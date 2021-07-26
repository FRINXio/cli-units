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

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.vrf.AbstractL3VrfConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.huawei.rev210726.HuaweiNiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;

public final class L3VrfConfigWriter extends AbstractL3VrfConfigWriter {

    private static final String UPDATE_TEMPLATE = "system-view\n"
            + "ip vpn-instance {$data.name}\n"
            + "{% if ($before) %}undo ipv4-family\n{% endif %}"
            + "{$data|update(description,description `$data.description`\n,undo description\n)}"
            + "{% if($data.route_distinguisher) %}ipv4-family\n"
            + "route-distinguisher {$data.route_distinguisher.string}\n"
            // prefix limit can exist only if rd is configured
            + "{% if($aug_after) %}prefix limit {$aug_after.prefix_limit_from} {$aug_after.prefix_limit_to}\n"
            + "{% else %}undo prefix limit\n{% endif %}"
            // from huawei documentation - after setup we cant delete rd directly,
            // only if we delete ipv4-family conf, rd will be deleted automatically
            + "{% elseIf (!$before) %}undo ipv4-family\n{% endif %}"
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
        HuaweiNiAug augConfigAfter = after.getAugmentation(HuaweiNiAug.class);
        HuaweiNiAug augConfigBefore = null;
        if (before != null) {
            augConfigBefore = before.getAugmentation(HuaweiNiAug.class);
        }
        return fT(UPDATE_TEMPLATE, "before", before, "aug_before", augConfigBefore,
                "data", after, "aug_after", augConfigAfter);
    }

    @Override
    protected String deleteTemplate(Config config) {
        return fT(DELETE_TEMPLATE, "data", config);
    }
}
