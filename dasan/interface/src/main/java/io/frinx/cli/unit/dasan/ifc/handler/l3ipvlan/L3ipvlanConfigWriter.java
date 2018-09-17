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

package io.frinx.cli.unit.dasan.ifc.handler.l3ipvlan;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.VlanInterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.l3ipvlan.rev180802.l3ipvlan._interface.top.l3ipvlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3ipvlanConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public L3ipvlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(ifName);

        if (!matcher.matches()) {
            return;
        }

        writeOrUpdateInterface(id, data, matcher.group("id"));
    }

    private void writeOrUpdateInterface(InstanceIdentifier<Config> id, Config data, String vlanId)
            throws WriteFailedException.CreateFailedException {

        blockingWriteAndRead(cli, id, data, "configure terminal", f("interface br%s", vlanId),
                BooleanUtils.isNotFalse(data.isIpRedirects()) ? "ip redirect" : "no ip redirect", "end");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(ifName);

        if (!matcher.matches()) {
            return;
        }

        writeOrUpdateInterface(id, dataAfter, matcher.group("id"));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(ifName);

        if (!matcher.matches()) {
            return;
        }

        blockingWriteAndRead(cli, id, dataBefore, "configure terminal", f("interface br%s", matcher.group("id")),
                "ip redirect",
                "end");
    }
}
