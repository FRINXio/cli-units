/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class RouterInterfaceInVlan implements SWInterface {

    private static final Pattern IFC_VE = Pattern.compile("ve (?<port>\\d+)");
    private String port;

    RouterInterfaceInVlan(String ifc) {
        port = extractPort(ifc);
    }

    private String extractPort(String ifc) {
        Matcher matcher = IFC_VE.matcher(ifc.trim());
        return matcher.matches() ? matcher.group("port") : null;
    }

    @Override
    public boolean containsInterface(@NotNull String ifcName) {
        String ifcPort = extractPort(ifcName);
        return ifcPort != null && ifcPort.equalsIgnoreCase(port);
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public List<String> getInterfaces() {
        return Lists.newArrayList(String.format("ve %s", port));
    }
}