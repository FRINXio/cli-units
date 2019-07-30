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

package io.frinx.cli.unit.brocade.network.instance.l2p2p;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.l2p2p.AbstractL2P2PReader;
import java.util.regex.Pattern;

public final class L2P2PReader extends AbstractL2P2PReader {

    private static final String SH_VLL = "show running-config | include vll";
    private static final Pattern VLL_ID_LINE = Pattern.compile("vll (?<network>\\S+)\\s+(?<vccid>\\S+).*");

    private static final String SH_VLL_LOCAL = "show running-config | include vll-local";
    private static final Pattern VLL_LOCAL_ID_LINE = Pattern.compile("vll-local (?<network>\\S+).*");

    public L2P2PReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadLocalRemoteCommand() {
        return SH_VLL;
    }

    @Override
    protected String getReadLocalLocalCommand() {
        return SH_VLL_LOCAL;
    }

    @Override
    protected Pattern getLocalLocalLine() {
        return VLL_LOCAL_ID_LINE;
    }

    @Override
    protected Pattern getLocalRemoteLine() {
        return VLL_ID_LINE;
    }
}
