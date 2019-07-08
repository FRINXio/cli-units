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

package io.frinx.cli.unit.nexus.ifc.handler.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.nexus.ifc.Util;
import io.frinx.cli.unit.nexus.ifc.handler.InterfaceReader;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

public final class SubinterfaceReader extends AbstractSubinterfaceReader {

    public static final String SEPARATOR = ".";

    private InterfaceReader ifaceReader;

    public SubinterfaceReader(Cli cli) {
        super(cli);
        ifaceReader = new InterfaceReader(cli);
    }

    @VisibleForTesting
    public List<SubinterfaceKey> parseSubinterfaceIds(String output, String ifcName) {
        return ifaceReader.parseAllInterfaceIds(output)
                // Now exclude interfaces
                .stream()
                .filter(Util::isSubinterface)
                .map(InterfaceKey::getName)
                .filter(subifcName -> subifcName.startsWith(ifcName))
                .map(name -> name.substring(name.lastIndexOf(SEPARATOR) + SEPARATOR.length()))
                .map(subifcIndex -> new SubinterfaceKey(Long.valueOf(subifcIndex)))
                .collect(Collectors.toList());
    }

    @Override
    protected String getReadCommand() {
        return InterfaceReader.SH_RUN_INTERFACE;
    }
}
