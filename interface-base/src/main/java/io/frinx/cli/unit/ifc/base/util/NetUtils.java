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

package io.frinx.cli.unit.ifc.base.util;

import java.util.regex.Pattern;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

public final class NetUtils {

    private static final Pattern DOT = Pattern.compile("\\.");

    public static final Pattern NO_MATCH = Pattern.compile("0^");

    private NetUtils() {
    }

    /**
     * Parses string that can be either a prefix length or
     * netmask into prefix of type short.
     *
     * @param netMask string either a number from 0 to 32, or a netmask xxx.xxx.xxx.xxx
     * @return short prefix length
     */
    public static Short prefixFrom(String netMask) {
        try {
            return Short.parseShort(netMask);
        } catch (NumberFormatException e) {
            int prefixLength = DOT.splitAsStream(netMask)
                    .map(Integer::parseInt)
                    .map(Integer::toBinaryString)
                    .map(octet -> octet.replaceAll("0", "").length())
                    .mapToInt(Integer::intValue)
                    .sum();

            return Integer.valueOf(prefixLength).shortValue();
        }
    }

    public static String getSubnetInfo(Ipv4AddressNoZone ip, Short prefixLength) {
        return new SubnetUtils(ip.getValue() + "/" + prefixLength).getInfo().getNetmask();
    }
}
