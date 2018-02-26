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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.ActionsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.action.top.actions.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryActionsConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private final Cli cli;

    public AclEntryActionsConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext context) throws ReadFailedException {
        String command = AclEntryReader.getAclCommand(id);
        String output = blockingRead(command, cli, id, context);
        Class<? extends FORWARDINGACTION> forwardingAction = tryToParseForwardingAction(id, output);
        configBuilder.setForwardingAction(forwardingAction);
    }

    @VisibleForTesting
    @Nullable
    static Class<? extends FORWARDINGACTION> tryToParseForwardingAction(@Nonnull InstanceIdentifier<Config> id,
                                                                        String output) {
        Optional<String> maybeLine = AclEntryLineParser.findAclEntryWithSequenceId(id, output);
        if (maybeLine.isPresent()) {
            String line = maybeLine.get();
            String[] words = line.split("\\s");
            return AclEntryLineParser.parseAction(words[1]);
        }
        return null;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((ActionsBuilder) builder).setConfig(config);
    }
}
