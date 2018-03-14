/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
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
