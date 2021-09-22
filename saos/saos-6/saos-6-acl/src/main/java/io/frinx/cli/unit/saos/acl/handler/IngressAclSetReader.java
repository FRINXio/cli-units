/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.saos.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetReader implements CliConfigListReader<IngressAclSet, IngressAclSetKey, IngressAclSetBuilder> {

    private static final String SH_PORT = "configuration search string \"port set port %s\"";
    private static final Pattern INGRESS_ACL_LINE = Pattern.compile(".*ingress-acl (?<name>.+).*");

    private final Cli cli;

    public IngressAclSetReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<IngressAclSetKey> getAllIds(@Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier,
                                            @Nonnull ReadContext readContext) throws ReadFailedException {
        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        final String output = blockingRead(f(SH_PORT, interfaceName), cli, instanceIdentifier, readContext);
        return parseAclKeys(output);
    }

    @VisibleForTesting
    public static List<IngressAclSetKey> parseAclKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            INGRESS_ACL_LINE::matcher,
            matcher -> matcher.group("name"),
            name -> new IngressAclSetKey(name, ACLIPV4.class));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier,
                                      @Nonnull IngressAclSetBuilder ingressAclSetBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        ingressAclSetBuilder.setSetName(instanceIdentifier.firstKeyOf(IngressAclSet.class).getSetName());
        ingressAclSetBuilder.setType(instanceIdentifier.firstKeyOf(IngressAclSet.class).getType());
    }

}