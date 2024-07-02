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

package io.frinx.cli.unit.junos.unit.acl.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.unit.utils.CliListWriter;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclInterfaceWriter implements CliListWriter<Interface, InterfaceKey> {

    static final Pattern INTERFACE_ID_PATTERN = Pattern.compile("(?<interface>[^\\.]+)\\.(?<unit>\\S+)");

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                       @NotNull Interface dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {

        String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();

        // In Junos we can set ACLs(inet filter) only for subinterfaces,
        // so the format of interface-id is fixed to <interface-name>.<unit-number>.
        Preconditions.checkArgument(INTERFACE_ID_PATTERN.matcher(interfaceName).matches(),
            "Interface id does not match '<interface>.<unit>'. id=" + interfaceName);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull Interface dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // This writer only has a check logic called when creating a container.
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Interface> id,
                                        @NotNull Interface dataBefore,
                                        @NotNull Interface dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // This writer only has a check logic called when creating a container.
    }
}