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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetReader
        implements CliConfigListReader<IngressAclSet, IngressAclSetKey, IngressAclSetBuilder> {

    //Supported type is inet(IPv4) only.
    private static final String SH_IFACE_FILTERS =
        "show configuration interfaces %s unit %s family inet filter input | display set";
    private static final Pattern IFACE_FILTER_LINE = Pattern.compile(
        "set interfaces (?<ifcname>\\S+) unit (?<unit>\\S+) family inet filter input (?<filter>\\S+)");

    private final Cli cli;

    public IngressAclSetReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<IngressAclSetKey> getAllIds(
        @Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        Matcher matcher = AclInterfaceWriter.INTERFACE_ID_PATTERN.matcher(interfaceName);

        // always return true. (already checked at IIDs.AC_IN_INTERFACE)
        matcher.matches();

        return getInterfaceFilters(
            matcher.group("interface"),
            matcher.group("unit"),
            instanceIdentifier,
            readContext);
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier,
        @Nonnull IngressAclSetBuilder builder,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        IngressAclSetKey key = instanceIdentifier.firstKeyOf(IngressAclSet.class);
        builder.setType(key.getType());
        builder.setSetName(key.getSetName());
    }

    private List<IngressAclSetKey> getInterfaceFilters(
        String ifcName,
        String unit,
        InstanceIdentifier<IngressAclSet> instanceIdentifier,
        ReadContext readContext) throws ReadFailedException {

        String output = blockingRead(f(SH_IFACE_FILTERS, ifcName, unit), cli, instanceIdentifier, readContext);

        // Supported type is IPv4(ACLIPV4) only.
        return ParsingUtils.parseFields(output, 0,
            IFACE_FILTER_LINE::matcher,
            matcher -> matcher.group("filter"),
            name -> new IngressAclSetKey(name , ACLIPV4.class));
    }
}
