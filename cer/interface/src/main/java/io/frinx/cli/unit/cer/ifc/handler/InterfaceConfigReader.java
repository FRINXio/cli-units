/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.cer.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigReader;
import java.util.regex.Pattern;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public class InterfaceConfigReader extends AbstractInterfaceConfigReader {

    public static final String SH_SINGLE_INTERFACE_CFG = "show running-config interface %s";

    public static final Pattern SHUTDOWN_LINE = Pattern.compile("shutdown");
    public static final Pattern DESCRIPTION_LINE = Pattern.compile("\\s*description (?<desc>.+)");

    public InterfaceConfigReader(Cli cli) {
        super(cli);
    }

    @Override
    public void parseInterface(String output, ConfigBuilder builder, String name) {
        super.parseInterface(output, builder, name);
    }

    @Override
    protected String getReadCommand(String ifcName) {
        return f(SH_SINGLE_INTERFACE_CFG, ifcName);
    }

    @Override
    protected Pattern getShutdownLine() {
        return SHUTDOWN_LINE;
    }

    @Override
    protected Pattern getDescriptionLine() {
        return DESCRIPTION_LINE;
    }

    @Override
    public Class<? extends InterfaceType> parseType(String name) {
        return Util.parseType(name);
    }
}
