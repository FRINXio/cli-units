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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.unit.acl.handler.util.NameTypeEntry;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EgressAclSetReader implements CliConfigListReader<EgressAclSet, EgressAclSetKey, EgressAclSetBuilder> {

    private static final String SH_IFACE_FILTERS = "show configuration interfaces %s unit %s | display set";
    private static final Pattern IFACE_FILTER_LINE = Pattern.compile(
            "set interfaces (?<ifcname>\\S+) unit (?<unit>\\S+) family (?<type>\\S+) filter output (?<name>\\S+)");

    private final Cli cli;

    public EgressAclSetReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<EgressAclSetKey> getAllIds(
            @NotNull InstanceIdentifier<EgressAclSet> instanceIdentifier,
            @NotNull ReadContext readContext) throws ReadFailedException {

        String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        Matcher matcher = AclInterfaceWriter.INTERFACE_ID_PATTERN.matcher(interfaceName);

        // always return true. (already checked at IIDs.AC_IN_INTERFACE)
        matcher.matches();
        String output = blockingRead(f(SH_IFACE_FILTERS, matcher.group("interface"), matcher.group("unit")),
                cli, instanceIdentifier, readContext);

        return ParsingUtils.parseFields(output, 0,
            IFACE_FILTER_LINE::matcher,
            NameTypeEntry::fromMatcher,
            e -> new EgressAclSetKey(e.getName(), e.getType()));
    }

    @Override
    public void readCurrentAttributes(
            @NotNull InstanceIdentifier<EgressAclSet> instanceIdentifier,
            @NotNull EgressAclSetBuilder builder,
            @NotNull ReadContext readContext) throws ReadFailedException {

        EgressAclSetKey key = instanceIdentifier.firstKeyOf(EgressAclSet.class);
        builder.setType(key.getType());
        builder.setSetName(key.getSetName());
    }
}