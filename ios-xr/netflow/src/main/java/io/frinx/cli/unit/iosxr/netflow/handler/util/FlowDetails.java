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

package io.frinx.cli.unit.iosxr.netflow.handler.util;

import java.util.regex.Matcher;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWTYPE;

public class FlowDetails {

    private final String monitorName;
    private final Class<? extends NETFLOWTYPE> monitorType;
    private final String samplerName;

    public FlowDetails(final String monitorName, final Class<? extends NETFLOWTYPE> monitorType,
                       final String samplerName) {

        this.monitorName = monitorName;
        this.monitorType = monitorType;
        this.samplerName = samplerName;
    }

    public FlowDetails(final String monitorName, final Class<? extends NETFLOWTYPE> monitorType) {

        this.monitorName = monitorName;
        this.monitorType = monitorType;
        this.samplerName = null;
    }

    public static FlowDetails fromMatcher(final Matcher matcher) {
        final String monitorName = matcher.group("monitorName");
        final Class<? extends NETFLOWTYPE> monitorType = NetflowUtils.getType(matcher.group("type"));
        final String samplerName = matcher.group("samplerName");

        return new FlowDetails(monitorName, monitorType, samplerName);
    }

    public String getMonitorName() {
        return monitorName;
    }

    public Class<? extends NETFLOWTYPE> getMonitorType() {
        return monitorType;
    }

    public String getSamplerName() {
        return samplerName;
    }
}
