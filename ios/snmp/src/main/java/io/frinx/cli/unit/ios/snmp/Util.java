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

package io.frinx.cli.unit.ios.snmp;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.SnmpCommunityConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.config.Mib;

public final class Util {

    private Util() {

    }

    public static Mib.Inclusion getInclusionType(String name) {
        switch (name) {
            case "included":
                return Mib.Inclusion.Included;
            case "excluded":
                return Mib.Inclusion.Excluded;
            default:
                return null;
        }
    }

    public static SnmpCommunityConfig.Access getAccessType(String name) {
        switch (name) {
            case "RO":
                return SnmpCommunityConfig.Access.Ro;
            case "RW":
                return SnmpCommunityConfig.Access.Rw;
            default:
                return null;
        }
    }

}