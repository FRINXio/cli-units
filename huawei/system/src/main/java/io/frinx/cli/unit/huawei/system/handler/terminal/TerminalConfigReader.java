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
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.Terminal;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.Config.ProtocolInbound;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.config.Acl.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.terminal.huawei.extension.rev210923.huawei.terminal.extension.terminals.terminal.config.AclBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class TerminalConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String TERMINALS_LIST = "display current-configuration configuration user-interface";
    private static final Pattern AUTH_MODE = Pattern.compile("\\s+authentication-mode (?<name>\\S+)\\s+");
    private static final Pattern PROTOCOL_INBOUND = Pattern.compile("\\s+protocol inbound (?<mode>\\S+)\\s+");
    private static final Pattern PRIVILEGE_LEVEL = Pattern.compile("\\s+user privilege level (?<level>\\d+)\\s+");
    private static final Pattern TIMEOUT = Pattern.compile("\\s+idle-timeout (?<timeMin>\\d+) (?<timeSec>\\d+)?\\s+");
    private static final Pattern ACL = Pattern.compile("\\s+acl (?<id>\\d+) (?<mode>\\S+)\\s+");
    private static final Pattern TERMINAL_FIRST_UI =
            Pattern.compile(".*interface (?<name>\\S+) (?<firstUi>\\d)\\s.*\\s*");
    private static final Pattern TERMINAL_SECOND_UI =
            Pattern.compile(".*interface (?<name>\\S+) (?<firstUi>\\d) (?<lastUi>\\d)\\s+");

    private final Cli cli;

    public TerminalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String accountName = instanceIdentifier.firstKeyOf(Terminal.class).getType();
        parseConfigAttributes(blockingRead(TERMINALS_LIST, cli, instanceIdentifier,
                readContext), configBuilder, accountName);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String type) {
        parsingTerminalFields(output, type, "name", AUTH_MODE, configBuilder::setAuthName);
        parsingTerminalFields(output, type, "firstUi", TERMINAL_FIRST_UI,
            value -> configBuilder.setFirstUiNumber(Short.valueOf(value)));
        parsingTerminalFields(output, type, "lastUi", TERMINAL_SECOND_UI,
            value -> configBuilder.setLastUiNumber(Short.valueOf(value)));
        parsingTerminalFields(output, type, "mode", PROTOCOL_INBOUND,
            value -> configBuilder.setProtocolInbound(ProtocolInbound.valueOf(
                value.substring(0, 1).toUpperCase() + value.substring(1))));
        parsingTerminalFields(output, type, "level", PRIVILEGE_LEVEL,
            value -> configBuilder.setPrivilegeLevel(Short.valueOf(value)));
        parsingTerminalFields(output, type, "timeMin", TIMEOUT,
            value -> configBuilder.setTimeoutMin(Integer.valueOf(value)));
        parsingTerminalFields(output, type, "timeSec", TIMEOUT,
            value -> configBuilder.setTimeoutSec(Short.valueOf(value)));

        AclBuilder aclBuilder = new AclBuilder();
        parsingTerminalFields(output, type, "id", ACL,
            value -> aclBuilder.setAclId(Integer.valueOf(value)));
        parsingTerminalFields(output, type, "mode", ACL, value ->
                aclBuilder.setDirection(Direction.valueOf(value.substring(0, 1).toUpperCase() + value.substring(1))));

        if (aclBuilder.getAclId() != null || aclBuilder.getDirection() != null) {
            configBuilder.setAcl(aclBuilder.build());
        }
    }

    private static void parsingTerminalFields(String output, String type, String find, Pattern pattern,
                                              Consumer<String> consumer) {
        Pattern.compile("\\n\\S").splitAsStream(output)
                .filter(value -> value.contains("interface " + type))
                .flatMap(Pattern.compile("\n")::splitAsStream)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group(find))
                .map(String::trim)
                .findFirst()
                .ifPresent(consumer);
    }
}
