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

package io.frinx.cli.unit.ios.ifc.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{% if ($data.description) %} description {$data.description}\n{% endif %}"
            + "{% if ($data.mtu) %} mtu {$data.mtu}\n{% endif %}"
            + "{% if ($data.enabled) %} no shutdown\n{% endif %}"
            + "end";

    private static final String TO_DELETE_TEMPLATE = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{% if ($data.description) %} no description\n{% endif %}"
            + "{% if ($data.mtu) %} no mtu\n{% endif %}"
            + "{% if ($data.enabled) %} shutdown\n{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no interface {$data.name}\n"
            + "end";

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!isPhysicalInterface(data)) {
            writeInterface(id, data);
        } else {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Cannot create interface of type: " + data.getType()));
        }
    }

    private void writeInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, id, data, fT(WRITE_TEMPLATE, "data", data));
    }

    public static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES = Collections.singleton(EthernetCsmacd
            .class);

    public static boolean isPhysicalInterface(Config data) {
        return PHYS_IFC_TYPES.contains(data.getType());
    }

    @Override
    @SuppressWarnings("IllegalCatch")
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            Preconditions.checkArgument(dataBefore.getType()
                            .equals(dataAfter.getType()),
                    "Changing interface type is not permitted. Before: %s, After: %s",
                    dataBefore.getType(), dataAfter.getType());
        } catch (RuntimeException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
        ConfigBuilder toDelete = new ConfigBuilder().setName(dataAfter.getName());
        ConfigBuilder toUpdate = new ConfigBuilder().setName(dataAfter.getName());
        if (!Objects.equals(dataBefore.getDescription(), dataAfter.getDescription())) {
            if (dataAfter.getDescription() == null) {
                toDelete.setDescription(dataBefore.getDescription());
            } else {
                toUpdate.setDescription(dataAfter.getDescription());
            }
        }
        if (!Objects.equals(dataBefore.getMtu(), dataAfter.getMtu())) {
            if (dataAfter.getMtu() == null) {
                toDelete.setMtu(dataBefore.getMtu());
            } else {
                toUpdate.setMtu(dataAfter.getMtu());
            }
        }
        if (!Objects.equals(dataBefore.isEnabled(), dataAfter.isEnabled())) {
            if (dataAfter.isEnabled() == null) {
                toDelete.setEnabled(dataBefore.isEnabled());
            } else {
                toUpdate.setEnabled(dataAfter.isEnabled());
            }
        }
        writeInterface(id, toUpdate.build());
        updateInterface(id, toDelete.build());
    }

    private void updateInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, id, data, fT(TO_DELETE_TEMPLATE, "data", data));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(dataBefore)) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        }
        deleteInterface(id, dataBefore);
    }

    private void deleteInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.DeleteFailedException {
        blockingDeleteAndRead(cli, id, fT(DELETE_TEMPLATE, data));
    }
}
