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

package io.frinx.cli.unit.saos.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.acl.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Saos6AclSetAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.Saos6AclSetAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.acl.set.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclSetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public AclSetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String aclSetName = instanceIdentifier.firstKeyOf(AclSet.class).getName();
        String output = blockingRead(AclSetReader.SHOW_COMMAND, cli, instanceIdentifier, readContext);

        parseConfig(output, configBuilder, aclSetName);
    }

    @VisibleForTesting
    void parseConfig(String output, ConfigBuilder builder, String aclSetName) {
        Saos6AclSetAugBuilder augBuilder = new Saos6AclSetAugBuilder();

        builder.setName(aclSetName);
        builder.setType(ACLIPV4.class);
        setFwdAction(output, augBuilder, aclSetName);
        setEnabled(output, augBuilder, aclSetName);

        builder.addAugmentation(Saos6AclSetAug.class, augBuilder.build());
    }

    private void setFwdAction(String output, Saos6AclSetAugBuilder builder, String aclSetName) {
        Pattern pattern = Pattern.compile(".*" + aclSetName + " default-filter-action (?<action>\\S+)");

        ParsingUtils.parseField(output,
            pattern::matcher,
            matcher -> matcher.group("action"),
            action -> builder.setDefaultFwdAction(Util.getFwdAction(action)));
    }

    private void setEnabled(String output, Saos6AclSetAugBuilder builder, String aclSetName) {
        Pattern pattern = Pattern.compile("access-list disable profile " + aclSetName);

        ParsingUtils.parseField(output,
            pattern::matcher,
            matcher -> true,
            action -> builder.setEnabled(false));
    }
}
