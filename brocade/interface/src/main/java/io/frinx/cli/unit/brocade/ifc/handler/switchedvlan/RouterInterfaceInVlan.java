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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class RouterInterfaceInVlan implements SWInterface {

    private static final Pattern IFC_VE = Pattern.compile("ve (?<port>\\d+)");
    private String port;
    private String tagName;

    RouterInterfaceInVlan(String tag, String ifc) {
        this.tagName = tag;
        Matcher matcher = IFC_VE.matcher(ifc.trim());
        if (matcher.matches()) {
            port = matcher.group("port");
        }
    }

    @Override
    public boolean containsInterface(@Nonnull String ifcName) {
        return ifcName.equalsIgnoreCase(port);
    }

    @Override
    public String getTag() {
        return tagName;
    }
}
