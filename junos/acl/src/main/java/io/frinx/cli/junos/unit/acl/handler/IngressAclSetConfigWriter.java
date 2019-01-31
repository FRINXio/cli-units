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

package io.frinx.cli.junos.unit.acl.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetConfigWriter implements CliWriter<Config> {
    private static final String CREATE_TEMPLATE = "set interfaces %s unit %s family inet filter input %s";
    private static final String DELETE_TEMPLATE = "delete interfaces %s unit %s family inet filter input %s";

    private final Cli cli;

    public IngressAclSetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> instanceIdentifier,
        @Nonnull Config config,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        Class<? extends ACLTYPE> aclType = config.getType();
        Preconditions.checkArgument(
            aclType == ACLIPV4.class,
            "Unsupported acl type is specified. aclType=" + aclType.getSimpleName());

        String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        Matcher matcher = AclInterfaceWriter.INTERFACE_ID_PATTERN.matcher(interfaceName);

        // always return true. (already checked at IIDs.AC_IN_INTERFACE)
        matcher.matches();

        blockingWriteAndRead(
            cli,
            instanceIdentifier,
            config,
            f(CREATE_TEMPLATE, matcher.group("interface"), matcher.group("unit"), config.getSetName()));
    }

    @Override
    public void updateCurrentAttributes(
        @Nonnull final InstanceIdentifier<Config> id,
        @Nonnull final Config dataBefore,
        @Nonnull final Config dataAfter,
        @Nonnull final WriteContext writeContext) throws WriteFailedException {

        // Both type and set-name attributes are keys in parent container.
        // So this container has no modifiable attributes.
    }

    @Override
    public void deleteCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> instanceIdentifier,
        @Nonnull Config config,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        Matcher matcher = AclInterfaceWriter.INTERFACE_ID_PATTERN.matcher(interfaceName);

        // always return true. (already checked at IIDs.AC_IN_INTERFACE)
        matcher.matches();

        blockingWriteAndRead(
            cli,
            instanceIdentifier,
            config,
            f(DELETE_TEMPLATE, matcher.group("interface"), matcher.group("unit"), config.getSetName()));
    }
}
