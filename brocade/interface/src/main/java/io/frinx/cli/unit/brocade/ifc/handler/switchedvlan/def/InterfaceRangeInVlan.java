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
import javax.annotation.Nonnull;

public class InterfaceRangeInVlan implements SWInterface {

    private static final Pattern RANGE = Pattern.compile("e(the)? (?<slotlow>\\d)/(?<portlow>\\d+)"
            + " to (?<slothigh>\\d)/(?<porthigh>\\d+)");
    private static final Pattern IFC_TO_COMPARE = Pattern.compile("(?<slot>\\d)/(?<port>\\d+)");

    private Integer slotLow;
    private Integer slotHigh;
    private Integer portLow;
    private Integer portHigh;
    private String tagName;

    InterfaceRangeInVlan(String tag, String group) {
        this.tagName = tag;
        Matcher rangeMatcher = RANGE.matcher(group.trim());
        if (rangeMatcher.matches()) {
            slotLow = Integer.valueOf(rangeMatcher.group("slotlow"));
            slotHigh = Integer.valueOf(rangeMatcher.group("slothigh"));
            portLow = Integer.valueOf(rangeMatcher.group("portlow"));
            portHigh = Integer.valueOf(rangeMatcher.group("porthigh"));
        }
    }

    @Override
    public boolean containsInterface(@Nonnull String ifcName) {
        Matcher matcher = IFC_TO_COMPARE.matcher(ifcName);
        if (matcher.matches()) {
            Integer slot = Integer.valueOf(matcher.group("slot"));
            Integer port = Integer.valueOf(matcher.group("port"));
            return slot.equals(slotLow) && slot.equals(slotHigh)
                    && port.compareTo(portLow) > -1 && port.compareTo(portHigh) < 1;
        }
        return false;
    }

    @Override
    public String getTag() {
        return tagName;
    }

    @Override
    public List<String> getInterfaces() {
        List<String> ifcs = Lists.newArrayList();
        for (int i = portLow; i <= portHigh; i++) {
            ifcs.add(String.format("ethernet %s/%s", slotLow, i));
        }
        return ifcs;
    }
}
