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
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceReader;
import java.util.regex.Pattern;

public final class InterfaceReader extends AbstractInterfaceReader {

    public static final String SH_INTERFACES = "show running-config | include ^interface";
    private static final Pattern INTERFACE_LINE = Pattern.compile("interface (?<id>\\S+ \\S+)");
    public static final Pattern SUBINTERFACE_NAME = Pattern.compile("(?<ifcId>.+)\\.(?<subifcIndex>\\d+)");

    public InterfaceReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand() {
        return SH_INTERFACES;
    }

    @Override
    protected Pattern getInterfaceIdLine() {
        return INTERFACE_LINE;
    }

    @Override
    protected Pattern subinterfaceName() {
        return SUBINTERFACE_NAME;
    }

}
