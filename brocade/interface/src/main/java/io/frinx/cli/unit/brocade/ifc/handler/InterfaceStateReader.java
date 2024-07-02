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

package io.frinx.cli.unit.brocade.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceStateReader;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public final class InterfaceStateReader extends AbstractInterfaceStateReader {

    public static final String SH_SINGLE_INTERFACE = "sh inter %s %s";

    public static final Pattern STATUS_LINE =
            Pattern.compile("(?<id>.+)[.\\s]* is (?<admin>[^,]+), line protocol is (?<oper>.+)");
    private static final Pattern MTU_LINE = Pattern.compile("\\s*MTU (?<mtu>.+) bytes.*$");
    private static final Pattern DESCR_LINE = Pattern.compile("\\s*Port name is (?<desc>.+)");


    public InterfaceStateReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        Class<? extends InterfaceType> ifcType = Util.parseType(ifcName);
        String ifcTypeOnDevice = Util.getTypeOnDevice(ifcType);
        String ifcNumber = Util.getIfcNumber(ifcName);
        return f(SH_SINGLE_INTERFACE, ifcTypeOnDevice, ifcNumber);
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
        return DESCR_LINE;
    }

    @Override
    @VisibleForTesting
    protected void parseInterfaceState(final String output, final StateBuilder builder, final String name) {
        super.parseInterfaceState(output, builder, name);
        builder.setType(Util.parseType(name));
    }
}
