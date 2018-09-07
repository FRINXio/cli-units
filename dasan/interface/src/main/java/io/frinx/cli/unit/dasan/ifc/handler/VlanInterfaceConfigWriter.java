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

package io.frinx.cli.unit.dasan.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Optional;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanInterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public VlanInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return;
        }

        validateIfcNameAgainstType(data);
        writeOrUpdateInterface(id, data, matcher.group("id"));
    }

    @VisibleForTesting
    static void validateIfcNameAgainstType(Config data) {

        Preconditions.checkArgument(data.getType() == L3ipvlan.class,
                "Cannot create interface of type: " + data.getType());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return;
        }
        Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                "Changing interface type is not permitted. Before: %s, After: %s", dataBefore.getType(),
                dataAfter.getType());

        validateIfcNameAgainstType(dataAfter);
        writeOrUpdateInterface(id, dataAfter, name);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        validateIfcNameAgainstType(dataBefore);
        deleteInterface(id, dataBefore, name);
    }

    private void deleteInterface(InstanceIdentifier<Config> id, Config data, String vlanId)
            throws WriteFailedException.DeleteFailedException {
        blockingDeleteAndRead(cli, id, "configure terminal", f("no interface %s", vlanId.replace("Vlan", "br")), "end");
    }

    @VisibleForTesting
    void writeOrUpdateInterface(InstanceIdentifier<Config> id, Config data, String vlanId)
            throws WriteFailedException.CreateFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, id, data, "configure terminal",
                f("interface %s", ifcName.replace("Vlan", "br")),
                Optional.ofNullable(data.isEnabled()).orElse(Boolean.FALSE) ? "shutdown" : "no shutdown",
                data.getMtu() == null ? "no mtu" : f("mtu %d", data.getMtu()), "end");
    }
}
