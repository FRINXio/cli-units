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
import io.frinx.cli.ni.base.handler.vrf.AbstractL3VrfConfigReader;
import java.util.regex.Pattern;

public final class L3VrfConfigReader extends AbstractL3VrfConfigReader {

    private static final String DISPLAY_VRF_CFG = "display current-configuration configuration vpn-instance %s";
    private static final Pattern DESC_CONFIG = Pattern.compile("description (?<desc>.*)");
    private static final Pattern RD_CONFIG = Pattern.compile("route-distinguisher (?<rd>\\S+)");

    public L3VrfConfigReader(Cli cli) {
        super(new L3VrfReader(cli), cli);
    }

    @Override
    protected String getReadCommand() {
        return DISPLAY_VRF_CFG;
    }

    @Override
    protected Pattern getRouteDistinguisherLine() {
        return RD_CONFIG;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESC_CONFIG;
    }
}
