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

package io.frinx.cli.unit.ios.privilege.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.LevelId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.Level;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.level.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.privilege.rev210217.privilege.level.top.levels.level.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LevelConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_LEVEL = "show running-config | include privilege exec level %s";
    private static final Pattern COMMAND_LINE = Pattern.compile("privilege exec level \\S+ (?<command>.+)");

    private final Cli cli;

    public LevelConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final LevelId levelId = instanceIdentifier.firstKeyOf(Level.class).getId();
        final String output = blockingRead(f(SHOW_LEVEL, levelId.getValue()), cli, instanceIdentifier, readContext);
        configBuilder.setId(levelId);
        configBuilder.setCommands(getCommands(output));
    }

    @VisibleForTesting
    public static List<String> getCommands(final String output) {
        return ParsingUtils.parseFields(output, 0,
            COMMAND_LINE::matcher,
            matcher -> matcher.group("command"),
            String::new);
    }

}
