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

package io.frinx.cli.unit.huawei.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.VrpAclSetAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class AclConfigReader {

    public static final String SH_ACL_NAME = "display current-configuration | section acl name %s";
    private static final Pattern ACL_CONFIG_DESCRIPTION = Pattern.compile("description (?<description>\\S.*)");
    private static final Pattern ACL_CONFIG_STEP = Pattern.compile("step (?<step>.*)");

    private final Cli cli;
    private final AclReader aclSetReader;

    public AclConfigReader(final Cli cli, final AclReader aclSetReader) {
        this.cli = cli;
        this.aclSetReader = aclSetReader;
    }

    public void readCurrentAttributes(@NotNull InstanceIdentifier<AclSet> instanceIdentifier,
                                      @NotNull VrpAclSetAugBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String aclName = instanceIdentifier.firstKeyOf(AclSet.class).getName();
        final String showCommand = String.format(SH_ACL_NAME, aclName);
        parsAclConfig(aclSetReader.blockingRead(showCommand, cli, instanceIdentifier, readContext),
                configBuilder, aclName);
    }

    @VisibleForTesting
    static void parsAclConfig(String output, VrpAclSetAugBuilder configBuilder, String aclName) {
        final Optional<String> aclAccessList = ParsingUtils.parseField(output, 0,
            parseAcl(aclName)::matcher,
            matcher -> matcher.group("accessList"));

        final Optional<String> aclDescription = ParsingUtils.parseField(output, 0,
            ACL_CONFIG_DESCRIPTION::matcher,
            matcher -> matcher.group("description"));

        final Optional<String> aclStep = ParsingUtils.parseField(output, 0,
            ACL_CONFIG_STEP::matcher,
            matcher -> matcher.group("step"));

        aclAccessList.ifPresent(configBuilder::setType2);
        aclDescription.ifPresent(configBuilder::setDescription);
        aclStep.ifPresent(configBuilder::setStep);
    }

    private static Pattern parseAcl(final String name) {
        final String regex = String.format("acl name %s (?<accessList>\\S+)", name);
        return Pattern.compile(regex);
    }
}