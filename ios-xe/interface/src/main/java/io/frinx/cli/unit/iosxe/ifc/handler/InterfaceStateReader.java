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

package io.frinx.cli.unit.iosxe.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceStateReader;
import io.frinx.cli.unit.iosxe.ifc.Util;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;

public final class InterfaceStateReader extends AbstractInterfaceStateReader {

    public static final String SH_SINGLE_INTERFACE = "show interfaces %s";

    public static final Pattern STATUS_LINE =
            Pattern.compile("(?<id>.+).*(?<admin>up|down).*, line protocol is (?<oper>up|down).*");
    private static final Pattern MTU_LINE = Pattern.compile("MTU (?<mtu>.+) bytes.*");
    public static final Pattern DESCRIPTION_LINE = Pattern.compile("Description: (?<desc>.+)");

    public InterfaceStateReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_SINGLE_INTERFACE, ifcName);
    }

    @Override
    protected void parseInterfaceState(String output, StateBuilder builder, String name) {
        super.parseInterfaceState(output, builder, name);
        builder.setType(Util.parseType(name));
    }

    @Override
    protected Pattern getMtuLine() {
        return MTU_LINE;
    }

    @Override
    protected Pattern getAdminStatusLine() {
        return STATUS_LINE;
    }

    @Override
    protected Pattern getOperStatusLine() {
        return STATUS_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESCRIPTION_LINE;
    }

}