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

package io.frinx.cli.unit.junos.ifc.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Set;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriter implements CliWriter<Config> {

    public static final Set<Class<? extends InterfaceType>> SUPPORTED_INTERFACE_TYPES =
        ImmutableSet.of(EthernetCsmacd.class);

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        Preconditions.checkArgument(isSupportedType(data.getType()),
            "Interface %s has unknown type: %s", data.getName(), data.getType().getSimpleName());

        writeInterface(id, data);
    }

    private void writeInterface(InstanceIdentifier<Config> id, Config data)
        throws WriteFailedException.CreateFailedException {

        String name = id.firstKeyOf(Interface.class).getName();

        blockingWriteAndRead(cli, id, data,
              StringUtils.isNotEmpty(data.getDescription())
                  ? f("set interfaces %s description %s", name, data.getDescription())
                  : f("delete interfaces %s description", name),
              data.isEnabled() == null || data.isEnabled()
                  ? f("delete interfaces %s disable", name) : f("set interfaces %s disable", name));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {

        Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
            "Changing interface type is not permitted. Before: %s, After: %s", dataBefore.getType(),
            dataAfter.getType());

        Preconditions.checkArgument(isSupportedType(dataAfter.getType()),
            "Interface %s has unknown type: %s", dataAfter.getName(), dataAfter.getType().getSimpleName());

        writeInterface(id, dataAfter);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        Preconditions.checkArgument(isSupportedType(dataBefore.getType()),
            "Interface %s has unknown type: %s", dataBefore.getName(), dataBefore.getType().getSimpleName());

        deleteInterface(id);
    }

    private void deleteInterface(InstanceIdentifier<Config> id)
        throws WriteFailedException.DeleteFailedException {

        InterfaceKey ifcKey = id.firstKeyOf(Interface.class);

        blockingDeleteAndRead(cli, id, f("delete interfaces %s", ifcKey.getName()));
    }

    public static boolean isSupportedType(Class<? extends InterfaceType> parentType) {
        return SUPPORTED_INTERFACE_TYPES.contains(parentType);
    }
}
