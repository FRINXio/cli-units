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

package io.frinx.cli.unit.iosxr.ifc.handler.verify;

import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.AllowConfigTop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWDEFAULT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.RPFALLOWSELFPING;

public final class RpfCheckUtils {

    public static final String IPV4_VERIFY_CMD_BASE = "ipv4 verify unicast source reachable-via %s";
    public static final String IPV6_VERIFY_CMD_BASE = "ipv6 verify unicast source reachable-via %s";
    public static final String CMD_POSTFIX_ALLOW_SELF_PING = " allow-self-ping";
    public static final String CMD_POSTFIX_ALLOW_DEFAULT = " allow-default";

    private RpfCheckUtils() {

    }

    static void appendAllowConfigCmdParams(final @Nonnull AllowConfigTop dataAfter, final StringBuilder verifyCmd) {
        if (dataAfter.getAllowConfig() != null) {
            dataAfter.getAllowConfig()
                    .forEach(allowConfig -> {
                        if (allowConfig.equals(RPFALLOWSELFPING.class)) {
                            verifyCmd.append(CMD_POSTFIX_ALLOW_SELF_PING);
                        }
                        if (allowConfig.equals(RPFALLOWDEFAULT.class)) {
                            verifyCmd.append(CMD_POSTFIX_ALLOW_DEFAULT);
                        }
                    });
        }
    }
}
