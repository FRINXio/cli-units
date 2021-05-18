/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.routing.policy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;

public final class Util {

    private Util() {}

    public static String extractRouteMap(final String routeMapName, final String statementId, final String output) {
        final String string = String.format("(?:route-map %s (permit|deny) %s \\r?\\n)(.*?)(?=\\s*route-map|\\s*$)",
                routeMapName, statementId);
        final Matcher matcher = Pattern.compile(string, Pattern.DOTALL).matcher(output);
        return matcher.find() ? matcher.group() : "";
    }

    public static String getIpPrefixAsString(final IpPrefix ipPrefix) {
        return ipPrefix.getIpv4Prefix() != null
                ? ipPrefix.getIpv4Prefix().getValue()
                : ipPrefix.getIpv6Prefix().getValue();
    }

}
