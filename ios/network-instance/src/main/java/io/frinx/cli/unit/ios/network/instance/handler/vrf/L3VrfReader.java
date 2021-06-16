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

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.vrf.AbstractL3VrfReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L3VrfReader extends AbstractL3VrfReader {

    private static final String SH_IP_VRF = "show running-config | include ^vrf definition |^ip vrf";
    private static final Pattern VRF_ID_LINE = Pattern.compile("(ip )?vrf( definition)? (?<vrfName>[\\S]+).*");

    public L3VrfReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand() {
        return SH_IP_VRF;
    }

    @Override
    protected Pattern getVrfLine() {
        return VRF_ID_LINE;
    }

    public List<NetworkInstanceKey> getAllIds(@Nonnull CliReader reader,
                                              @Nonnull InstanceIdentifier<?> id,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {
        List<NetworkInstanceKey> keys = super.getAllIds(reader, id, readContext);
        keys.add(NetworInstance.DEFAULT_NETWORK);
        return keys;
    }
}
