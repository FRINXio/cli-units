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
import io.frinx.translate.unit.commons.handler.spi.CompositeChildWriter;
import java.util.Optional;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L3ipvlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanInterfaceConfigWriter implements CliWriter<Config>, CompositeChildWriter<Config> {

    private Cli cli;

    public VlanInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(name);
        if (!matcher.matches() || data.getType() != L3ipvlan.class) {
            return false;
        }
        writeOrUpdateInterface(id, data, matcher.group("id"));
        return true;
    }

    @VisibleForTesting
    static void validateIfcNameAgainstType(Config data) {

        Preconditions.checkArgument(data.getType() == L3ipvlan.class,
                "Cannot create interface of type: " + data.getType());
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(name);
        if (!matcher.matches() || dataAfter.getType() != L3ipvlan.class) {
            return false;
        }
        validateIfcNameAgainstType(dataAfter);
        writeOrUpdateInterface(id, dataAfter, name);
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = VlanInterfaceReader.INTERFACE_NAME_PATTERN.matcher(name);
        if (!matcher.matches() || dataBefore.getType() != L3ipvlan.class) {
            return false;
        }
        deleteInterface(id, dataBefore, name);
        return true;
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
                Optional.ofNullable(data.isEnabled()).orElse(Boolean.FALSE) ? "no shutdown" : "shutdown",
                data.getMtu() == null ? "no mtu" : f("mtu %d", data.getMtu()), "end");
    }
}
