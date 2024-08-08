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
import io.frinx.cli.unit.ni.base.handler.l2p2p.AbstractL2P2ConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;

public final class L2P2PConfigReader extends AbstractL2P2ConfigReader {

    private static final Pattern MTU_LINE = Pattern.compile("\\s*vll-mtu (?<mtu>.+)");
    private static final String SH_VLL = "show running-config | begin vll %s";

    public L2P2PConfigReader(Cli cli) {
        super(new L2P2PReader(cli), cli);
    }

    @Override
    protected String getReadCommand(String vllName) {
        return f(SH_VLL, vllName);
    }

    @Override
    protected void parseL2p2(String output, ConfigBuilder builder, String vllName) {
        builder.setName(vllName);

        int endIndex = output.indexOf("\n\n");
        ParsingUtils.parseField(endIndex == -1 ? output : output.substring(0, endIndex),
            getMtuLine()::matcher,
            matcher -> Integer.valueOf(matcher.group("mtu")),
            builder::setMtu);
    }

    @Override
    protected Pattern getMtuLine() {
        return MTU_LINE;
    }
}
