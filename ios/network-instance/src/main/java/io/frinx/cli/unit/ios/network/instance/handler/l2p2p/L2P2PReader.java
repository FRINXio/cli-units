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

package io.frinx.cli.unit.ios.network.instance.handler.l2p2p;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.l2p2p.AbstractL2P2PReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

public final class L2P2PReader extends AbstractL2P2PReader {

    public static final String SH_INTERFACES_XCONNECT = "show running-config | include ^interface|^ *xconnect";
    public static final Pattern XCONNECT_ID_LINE = Pattern.compile("(?<interface>\\S+)\\s+xconnect\\s+(?<ip>\\S+)\\s+"
            + "(?<vccid>\\S+)\\s+(?<encaps>.*)");
    private static final Pattern PW_CLASS = Pattern.compile(".*pw-class (?<pwclass>\\S+)");

    public static final String SH_LOCAL_CONNECT = "show running-config | include interworking ethernet";
    public static final Pattern LOCAL_CONNECT_ID_LINE = Pattern.compile("connect (?<network>\\S+)\\s+"
            + "(?<interface1>\\S+)\\s+(?<interface2>\\S+)\\s+interworking ethernet");

    public L2P2PReader(Cli cli) {
        super(cli);
    }

    @Override
    protected List<NetworkInstanceKey> parseLocalRemote(String output) {
        String linePerInterface = realignXconnectInterfacesOutput(output);
        return ParsingUtils.parseFields(linePerInterface, 0,
            XCONNECT_ID_LINE::matcher,
            this::getXconnectId,
            NetworkInstanceKey::new);
    }

    @Override
    protected String getReadLocalRemoteCommand() {
        return SH_INTERFACES_XCONNECT;
    }

    @Override
    protected String getReadLocalLocalCommand() {
        return SH_LOCAL_CONNECT;
    }

    @Override
    protected Pattern getLocalLocalLine() {
        return LOCAL_CONNECT_ID_LINE;
    }

    @Override
    protected Pattern getLocalRemoteLine() {
        return XCONNECT_ID_LINE;
    }

    public static String realignXconnectInterfacesOutput(String output) {
        String withoutNewlines = output.replaceAll(ParsingUtils.NEWLINE.pattern(), "");
        return withoutNewlines.replace("interface ", "\n");
    }

    private String getXconnectId(Matcher matcher) {
        String ifc = matcher.group("interface");
        String ipRemote = matcher.group("ip");
        String encaps = matcher.group("encaps");

        Matcher pwClass = PW_CLASS.matcher(encaps);

        // Use pw-class as l2vpn name if possible
        return (pwClass.matches()) ? pwClass.group("pwclass") :
            // otherwise use interface + remote IP as name
            f("%s xconnect %s", ifc, ipRemote);
    }
}
