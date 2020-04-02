/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.cubro.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.ACLIP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetReader implements CliConfigListReader<IngressAclSet, IngressAclSetKey, IngressAclSetBuilder> {

    static final String SH_CONFIGURATION = "show running-config";

    static final Pattern INGRESS_ACLS_LINE =
            Pattern.compile("\\s*apply access-list ip (?<name>.+) in.*", Pattern.DOTALL);

    private final Cli cli;

    public IngressAclSetReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<IngressAclSetKey> getAllIds(@Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier,
                                            @Nonnull ReadContext readContext) throws ReadFailedException {
        return parseAclKeys(blockingRead(SH_CONFIGURATION, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    public static List<IngressAclSetKey> parseAclKeys(String output) {
        List<IngressAclSetKey> keys = new ArrayList<>();
        keys.addAll(ParsingUtils.parseFields(output, 0,
            INGRESS_ACLS_LINE::matcher,
            matcher -> matcher.group("name"),
            name -> new IngressAclSetKey(name, ACLIP.class)
        ));
        return keys;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier,
                                      @Nonnull IngressAclSetBuilder ingressAclSetBuilder,
                                      @Nonnull ReadContext readContext) {
        ingressAclSetBuilder.setType(ACLIP.class);
        ingressAclSetBuilder.setSetName(instanceIdentifier.firstKeyOf(IngressAclSet.class).getSetName());
    }
}