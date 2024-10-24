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

package io.frinx.cli.unit.ios.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.unit.acl.handler.util.NameTypeEntry;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetReader implements CliConfigListReader<IngressAclSet, IngressAclSetKey, IngressAclSetBuilder> {

    static final String SH_ACL_INTF = "show running-config interface %s";
    private static final Pattern IPV4_INGRESS_ACLS_LINE =
            Pattern.compile("\\s*ip access-group (?<name>.+) in.*", Pattern.DOTALL);
    private static final Pattern IPV6_INGRESS_ACLS_LINE =
            Pattern.compile("\\s*ipv6 traffic-filter (?<name>.+) in.*", Pattern.DOTALL);

    private final Cli cli;

    public IngressAclSetReader(Cli cli) {
        this.cli = cli;
    }

    private static Pattern aclV4PatternByName(final String name) {
        final String regex = String.format("\\s*ip access-group %s in", name);
        return Pattern.compile(regex);
    }

    private static Pattern aclV6PatternByName(final String name) {
        final String regex = String.format("\\s*ipv6 traffic-filter %s in", name);
        return Pattern.compile(regex);
    }

    @VisibleForTesting
    static NameTypeEntry parseAcl(final String output, final String setName) {
        if (ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(aclV4PatternByName(setName)::matcher)
                .anyMatch(Matcher::matches)) {
            return new NameTypeEntry(setName, ACLIPV4.class);
        }

        if (ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(aclV6PatternByName(setName)::matcher)
                .anyMatch(Matcher::matches)) {
            return new NameTypeEntry(setName, ACLIPV6.class);
        }

        throw new IllegalArgumentException("ACL of name " + setName + "not found");
    }

    @VisibleForTesting
    public static List<IngressAclSetKey> parseAclKeys(String output) {
        List<IngressAclSetKey> keys = new ArrayList<>();
        keys.addAll(ParsingUtils.parseFields(output, 0,
            IPV4_INGRESS_ACLS_LINE::matcher,
            matcher -> matcher.group("name"),
            name -> new IngressAclSetKey(name, ACLIPV4.class)
        ));
        keys.addAll(ParsingUtils.parseFields(output, 0,
            IPV6_INGRESS_ACLS_LINE::matcher,
            matcher -> matcher.group("name"),
            name -> new IngressAclSetKey(name, ACLIPV6.class)
        ));
        return keys;
    }

    @NotNull
    @Override
    public List<IngressAclSetKey> getAllIds(@NotNull InstanceIdentifier<IngressAclSet> instanceIdentifier,
                                            @NotNull ReadContext readContext) throws ReadFailedException {
        InterfaceId interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        return parseAclKeys(
                blockingRead(String.format(SH_ACL_INTF, interfaceId.getValue()), cli, instanceIdentifier, readContext));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<IngressAclSet> instanceIdentifier,
                                      @NotNull IngressAclSetBuilder ingressAclSetBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        final String setName = instanceIdentifier.firstKeyOf(IngressAclSet.class).getSetName();

        final String readCommand = f(SH_ACL_INTF, interfaceName);
        final String readConfig = blockingRead(
                readCommand,
                cli,
                instanceIdentifier,
                readContext
        );

        NameTypeEntry entry = parseAcl(readConfig, setName);
        ingressAclSetBuilder.setSetName(entry.getName());
        ingressAclSetBuilder.setType(entry.getType());
    }
}