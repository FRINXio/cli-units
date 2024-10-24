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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclInterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {
    @VisibleForTesting
    static final String SH_IFACES = "show configuration interfaces | display set";
    private static final Pattern IFACE_LINE = Pattern.compile(
        "set interfaces (?<ifcname>\\S+) unit (?<unit>\\S+) family inet(6)? filter (input|output) (?<filter>\\S+)");

    private final Cli cli;

    public AclInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(
        @NotNull InstanceIdentifier<Interface> instanceIdentifier,
        @NotNull ReadContext readContext) throws ReadFailedException {

        return getInterfaceIds(this, cli, instanceIdentifier, readContext).stream()
            .map(InterfaceId::new)
            .map(InterfaceKey::new)
            .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(
        @NotNull InstanceIdentifier<Interface> instanceIdentifier,
        @NotNull InterfaceBuilder interfaceBuilder,
        @NotNull ReadContext readContext) throws ReadFailedException {

        InterfaceKey key = instanceIdentifier.firstKeyOf(Interface.class);
        interfaceBuilder.setId(key.getId());
    }

    private static <O extends DataObject, B extends Builder<O>> List<String> getInterfaceIds(
            CliReader<O, B> cliReader,
            Cli cli,
            InstanceIdentifier<O> instanceIdentifier,
            ReadContext readContext) throws ReadFailedException {

        String output = cliReader.blockingRead(SH_IFACES, cli, instanceIdentifier, readContext);

        // In Junos we can set ACLs(inet filter) only for subinterfaces,
        // so the format of interface-id is fixed to <interface-name>.<unit-number>.
        Set<String> interfaceIds = new HashSet<>(ParsingUtils.parseFields(output, 0,
            IFACE_LINE::matcher,
            matcher -> String.format("%s.%s", matcher.group("ifcname"), matcher.group("unit")),
            s -> s));
        return Lists.newArrayList(interfaceIds);
    }
}