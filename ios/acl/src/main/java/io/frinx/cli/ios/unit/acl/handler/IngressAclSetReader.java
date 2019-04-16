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

package io.frinx.cli.ios.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.unit.acl.handler.util.AclUtil;
import io.frinx.cli.ios.unit.acl.handler.util.NameTypeEntry;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetReader implements CliConfigListReader<IngressAclSet, IngressAclSetKey, IngressAclSetBuilder> {

    private static final String SH_ACL_INTF = "show running-config interface %s";
    private static final Pattern ALL_INGRESS_ACLS_LINE =
            Pattern.compile("(?<type>.+) access-group (?<name>.+) in.*", Pattern.DOTALL);

    private final Cli cli;

    public IngressAclSetReader(Cli cli) {
        this.cli = cli;
    }

    private static Pattern aclPatternByName(final String name) {
        final String regex = String.format("(?<type>.+) access-group %s in", name);
        return Pattern.compile(regex);
    }

    @VisibleForTesting
    static void parseAcl(final String output, final Builder configBuilder, final String setName) {
        Preconditions.checkArgument(
                configBuilder instanceof IngressAclSetBuilder || configBuilder instanceof ConfigBuilder
        );

        final Pattern aclLine = aclPatternByName(setName);

        ParsingUtils.parseField(output, 0,
            aclLine::matcher,
            matcher -> AclUtil.getType(matcher.group("type")),
            value -> setNameAndType(configBuilder, setName, value));
    }

    private static void setNameAndType(final Builder builder, String name, Class<? extends ACLTYPE> type) {
        if (builder instanceof IngressAclSetBuilder) {
            ((IngressAclSetBuilder) builder).setType(type);
            ((IngressAclSetBuilder) builder).setSetName(name);
        } else if (builder instanceof ConfigBuilder) {
            ((ConfigBuilder) builder).setType(type);
            ((ConfigBuilder) builder).setSetName(name);
        }
    }

    @VisibleForTesting
    public static List<IngressAclSetKey> parseAclKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            ALL_INGRESS_ACLS_LINE::matcher,
            NameTypeEntry::fromMatcher,
            nameTypeEntry -> new IngressAclSetKey(nameTypeEntry.getName(), nameTypeEntry.getType())
        );
    }

    @Nonnull
    @Override
    public List<IngressAclSetKey> getAllIds(@Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier,
                                            @Nonnull ReadContext readContext) throws ReadFailedException {
        InterfaceId interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        return parseAclKeys(
                blockingRead(String.format(SH_ACL_INTF, interfaceId.getValue()), cli, instanceIdentifier, readContext));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<IngressAclSet> list) {
        ((IngressAclSetsBuilder) builder).setIngressAclSet(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier,
                                      @Nonnull IngressAclSetBuilder ingressAclSetBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        final String setName = instanceIdentifier.firstKeyOf(IngressAclSet.class).getSetName();

        final String readCommand = f(IngressAclSetConfigReader.SH_ACL_INTF, interfaceName);
        final String readConfig = blockingRead(
                readCommand,
                cli,
                instanceIdentifier,
                readContext
        );

        parseAcl(readConfig, ingressAclSetBuilder, setName);
    }
}
