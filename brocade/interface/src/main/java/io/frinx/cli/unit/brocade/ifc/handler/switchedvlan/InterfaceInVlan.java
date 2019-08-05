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

import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class InterfaceInVlan implements SWInterface {

    private static final Pattern IFC_SINGLE = Pattern.compile("e(the)? (?<prefix>\\d)/(?<port>\\d+)");
    private static final Pattern IFC_TO_COMPARE = Pattern.compile("(?<prefix>\\d)/(?<port>\\d+)");
    private final String tagName;
    private Integer from;
    private Integer prefixFrom;

    InterfaceInVlan(String tag, String ifc) {
        this.tagName = tag;
        Matcher rangeMatcher = IFC_SINGLE.matcher(ifc.trim());
        if (rangeMatcher.matches()) {
            prefixFrom = Integer.valueOf(rangeMatcher.group("prefix"));
            from = Integer.valueOf(rangeMatcher.group("port"));
        }
    }

    @Override
    public boolean containsInterface(@Nonnull String ifcName) {
        Matcher matcher = IFC_TO_COMPARE.matcher(ifcName);
        if (matcher.matches()) {
            Integer prefix = Integer.valueOf(matcher.group("prefix"));
            Integer ifc = Integer.valueOf(matcher.group("port"));
            return prefix.equals(prefixFrom) && ifc.compareTo(from) == 0;
        }
        return false;
    }

    @Override
    public String getTag() {
        return tagName;
    }

    @Override
    public List<String> getInterfaces() {
        return Lists.newArrayList(String.format("ethernet %s/%s", prefixFrom, from));
    }
}
