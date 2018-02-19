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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.ingress.acl.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_ACL_INTF = "do show running-config interface %s";
    private final Cli cli;

    public IngressAclSetConfigReader(final Cli cli) {
        this.cli = cli;
    }

    private Pattern aclLine(final String name) {
        final String regex = f("(?<type>.+) access-group %s ingress", name);
        return Pattern.compile(regex);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext)
        throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getId().getValue();
        InterfaceCheckUtil.checkInterface(readContext, name);

        final String setName = instanceIdentifier.firstKeyOf(IngressAclSet.class).getSetName();

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
        ((IngressAclSetBuilder) builder).setConfig(config);
    }

    void parseAclConfig(final String output, final ConfigBuilder configBuilder, final String setName) {
        configBuilder.setSetName(setName);

        final Pattern aclLine = aclLine(setName);

        ParsingUtils.parseField(output, 0,
            aclLine::matcher,
            matcher -> AclUtil.getType(matcher.group("type")),
            configBuilder::setType);
    }
}
