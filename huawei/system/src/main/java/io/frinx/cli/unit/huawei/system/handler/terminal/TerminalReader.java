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

package io.frinx.cli.unit.huawei.system.handler.terminal;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.Terminal;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.TerminalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.TerminalKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class TerminalReader implements CliConfigListReader<Terminal, TerminalKey, TerminalBuilder> {

    private static final String TERMINALS_LIST = "display current-configuration configuration user-interface";
    private static final Pattern TERMINALS_LINE = Pattern.compile("user-interface (?<name>\\S+) .*");
    private final Cli cli;

    public TerminalReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<TerminalKey> getAllIds(@NotNull InstanceIdentifier<Terminal> instanceIdentifier,
                                     @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(TERMINALS_LIST, cli, instanceIdentifier, readContext);
        return getAllIds(output);
    }

    @VisibleForTesting
    static List<TerminalKey> getAllIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            TERMINALS_LINE::matcher,
            matcher -> matcher.group("name"),
            TerminalKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Terminal> instanceIdentifier,
                                      @NotNull TerminalBuilder builder,
                                      @NotNull ReadContext readContext) {
        builder.setKey(instanceIdentifier.firstKeyOf(Terminal.class));
    }
}