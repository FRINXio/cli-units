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

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceReader;
import java.util.regex.Pattern;

public final class InterfaceReader extends AbstractInterfaceReader {

    private static final String SH_INTERFACE = "display ip interface brief";

    private static final Pattern INTERFACE_ID_LINES = Pattern.compile(
            "(?<id>[a-zA-Z]+[0-9/*.]+)\\s+(?<ipAddr>[^\\s]+)\\s+(?<status>[^\\s]+)\\s+(?<protocol>[^\\s]+)\\s*"
    );

    private static final Pattern SUBINTERFACE_NAME = Pattern.compile("(?<ifcId>.+)[.](?<subifcIndex>[0-9]+)");

    public InterfaceReader(Cli cli) {
        super(cli);
    }

    @Override
    protected String getReadCommand() {
        return SH_INTERFACE;
    }

    @Override
    protected Pattern getInterfaceIdLine() {
        return INTERFACE_ID_LINES;
    }

    @Override
    protected Pattern subinterfaceName() {
        return SUBINTERFACE_NAME;
    }
}
