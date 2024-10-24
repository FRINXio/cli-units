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

package io.frinx.cli.unit.huawei.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceStateReader;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;

public final class InterfaceStateReader extends AbstractInterfaceStateReader {

    private static final String DISPLAY_SINGLE_INTERFACE = "display inter %s";

    private static final Pattern ADMIN_STATUS_LINE =
            Pattern.compile("(?<id>\\S+) current state : (?<admin>UP|DOWN).*");
    private static final Pattern OPER_STATUS_LINE =
            Pattern.compile("\\s*Line protocol current state : (?<oper>UP|DOWN).*");

    private static final Pattern MTU_LINE = Pattern.compile(".*The Maximum Transmit Unit is (?<mtu>.+).*$");
    private static final Pattern DESCR_LINE = Pattern.compile("\\s*Description: (?<desc>.+)\\s*");

    public InterfaceStateReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(DISPLAY_SINGLE_INTERFACE, ifcName);
    }

    @VisibleForTesting
    public void parseInterfaceState(final String output, final StateBuilder builder, final String name) {
        super.parseInterfaceState(output, builder, name);
        builder.setType(Util.parseType(name));
    }

    @Override
    protected Pattern getMtuLine() {
        return MTU_LINE;
    }

    @Override
    protected Pattern getAdminStatusLine() {
        return ADMIN_STATUS_LINE;
    }

    @Override
    protected Pattern getOperStatusLine() {
        return OPER_STATUS_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESCR_LINE;
    }
}
