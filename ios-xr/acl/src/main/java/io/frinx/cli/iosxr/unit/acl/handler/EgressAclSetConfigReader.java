/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.iosxr.unit.acl.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.unit.acl.handler.util.AclUtil;
import io.frinx.cli.iosxr.unit.acl.handler.util.InterfaceCheckUtil;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.EgressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.egress.acl.top.egress.acl.sets.egress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EgressAclSetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_ACL_INTF = "do show running-config interface %s";
    private static final Pattern ACL_LINE = Pattern.compile("(?<type>.+) access-group (?<name>.+) egress");
    private final Cli cli;

    public EgressAclSetConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        InterfaceCheckUtil.checkInterface(readContext, name);

        final String setName = instanceIdentifier.firstKeyOf(EgressAclSet.class).getSetName();

        final String readCommand = f(SH_ACL_INTF, instanceIdentifier.firstKeyOf(Interface.class).getId().getValue());
        final String readConfig = blockingRead(
            readCommand,
            cli,
            instanceIdentifier,
            readContext
        );

        parseAclConfig(readConfig, configBuilder, setName);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((EgressAclSetBuilder) builder).setConfig(config);
    }

    private void parseAclConfig(final String output, final ConfigBuilder configBuilder, final String setName) {
        configBuilder.setSetName(setName);

        ParsingUtils.parseField(output, 0,
            ACL_LINE::matcher,
            matcher -> AclUtil.getType(matcher.group("type")),
            configBuilder::setType);
    }
}
