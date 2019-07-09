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

package io.frinx.cli.unit.iosxr.network.instance.handler.vrf;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.vrf.AbstractL3VrfReader;
import java.util.regex.Pattern;

public final class L3VrfReader extends AbstractL3VrfReader {

    //TODO: get all VRF instance-names from protocol layer configuration.
    //but in the future, should get it from VRF configuration itself.
    private static final String DISPLAY_CONF_VRF = "show running-config | include ^ vrf";
    private static final Pattern VRF_CONFIGURATION_LINE = Pattern.compile("vrf (?<vrfName>\\S+).*");

    public L3VrfReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand() {
        return DISPLAY_CONF_VRF;
    }

    @Override
    protected Pattern getVrfLine() {
        return VRF_CONFIGURATION_LINE;
    }
}
