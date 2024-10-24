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

package io.frinx.cli.unit.ifc.base.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractInterfaceReader
        implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private Cli cli;

    public AbstractInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        return parseInterfaceIds(blockingRead(getReadCommand(), cli, instanceIdentifier, readContext));
    }

    protected abstract String getReadCommand();

    @VisibleForTesting
    public List<InterfaceKey> parseInterfaceIds(String output) {
        return parseAllInterfaceIds(output)
                // Now exclude subinterfaces
                .stream()
                .filter(ifcName -> !isSubinterface(ifcName))
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    public List<InterfaceKey> parseAllInterfaceIds(String output) {
        return  ParsingUtils.parseFields(output, 0,
            getInterfaceIdLine()::matcher,
            matcher -> matcher.group("id"),
            InterfaceKey::new);
    }

    protected abstract Pattern getInterfaceIdLine();

    private boolean isSubinterface(InterfaceKey ifcName) {
        return subinterfaceName().matcher(ifcName.getName()).matches();
    }

    protected abstract Pattern subinterfaceName();

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder builder, @NotNull ReadContext readContext) {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}