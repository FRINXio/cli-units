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

package io.frinx.cli.unit.nexus.ifc.handler;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    private static final Set<Class<? extends InterfaceType>> SUPPORTED_INTERFACE_TYPES = Sets.newHashSet();

    static {
        SUPPORTED_INTERFACE_TYPES.add(Ieee8023adLag.class);
        SUPPORTED_INTERFACE_TYPES.add(SoftwareLoopback.class);
    }

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(data)) {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Physical interface cannot be created"));
        }

        validateIfcNameAgainstType(data);
        writeInterface(id, data);
    }

    @SuppressWarnings("IllegalCatch")
    private void writeInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.CreateFailedException {

        blockingWriteAndRead(cli, id, data,
                f("interface %s", data.getName()),
                data.getMtu() == null ? "no mtu" : f("mtu %s", data.getMtu()),
                data.getDescription() == null ? "no description" : f("description %s", data.getDescription()),
                data.isEnabled() != null && data.isEnabled() ? "no shutdown" : "shutdown",
                "root");
    }

    private static final Pattern LOOPBACK_NAME_PATTERN = Pattern.compile("Loopback(?<number>[0-9]+)");
    private static final Pattern LAG_IFC_NAME_PATTERN = Pattern.compile("Port-Channel(?<number>\\d+)");

    private static void validateIfcNameAgainstType(Config data) {
        if (data.getType() == SoftwareLoopback.class) {
            Matcher matcher = LOOPBACK_NAME_PATTERN.matcher(data.getName());
            Preconditions.checkArgument(matcher.matches(),
                    "Loopback name must be in format: %s, not: %s", LOOPBACK_NAME_PATTERN, data.getName());
        } else if (data.getType() == Ieee8023adLag.class) {
            Matcher matcher = LAG_IFC_NAME_PATTERN.matcher(data.getName());
            boolean result = matcher.matches();
            Preconditions.checkArgument(result,
                    "LAG interface name must be in format: %s, not: %s", LAG_IFC_NAME_PATTERN, data.getName());
        } else {
            throw new IllegalArgumentException("Interface " + data.getName() + " has unknown type: " + data.getType()
                    .getSimpleName());
        }
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

        Preconditions.checkArgument(dataBefore.getType()
                        .equals(dataAfter.getType()),
                "Changing interface type is not permitted. Before: %s, After: %s",
                dataBefore.getType(), dataAfter.getType());

        if (isPhysicalInterface(dataAfter)) {
            updatePhysicalInterface(id, dataAfter);
        } else {
            validateIfcNameAgainstType(dataAfter);
            writeInterface(id, dataAfter);
        }
    }

    private void updatePhysicalInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.CreateFailedException {

        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("interface %s", data.getName()),
                data.getDescription() == null ? "no description" : f("description %s", data.getDescription()),
                data.getMtu() == null ? "no mtu" : f("mtu %s", data.getMtu()),
                data.isEnabled() ? "no shutdown" : "shutdown",
                "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(dataBefore)) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        }

        validateIfcNameAgainstType(dataBefore);
        deleteInterface(id, dataBefore);
    }

    @SuppressWarnings("IllegalCatch")
    private void deleteInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.DeleteFailedException {
        blockingDeleteAndRead(cli, id,
                f("no interface %s", data.getName()));
    }
}
