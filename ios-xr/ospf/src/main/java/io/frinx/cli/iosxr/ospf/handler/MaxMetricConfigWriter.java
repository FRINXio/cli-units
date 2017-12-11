/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MaxMetricConfigWriter implements OspfWriter<Config> {

    private final Cli cli;

    public MaxMetricConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        if (!data.isSet()) {
            deleteCurrentAttributes(instanceIdentifier, data, writeContext);
        }
        final String timeout = (data.getTimeout() != null) ? "on-startup " + data.getTimeout() : "";
        final StringBuilder builder = new StringBuilder();
        for (Class<? extends MAXMETRICINCLUDE> include : data.getInclude()) {
            builder.append(parseIncludes(include));
        }
        blockingWriteAndRead(cli, instanceIdentifier, data,
                "configure terminal",
                f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
                f("max-metric router-lsa %s %s", timeout, builder.toString()),
                "commit",
                "end");
    }

    private String parseIncludes(Class<? extends MAXMETRICINCLUDE> include) {
        if (MAXMETRICINCLUDESTUB.class.equals(include)) {
            return "include-stub ";
        } else if (MAXMETRICINCLUDETYPE2EXTERNAL.class.equals(include)) {
            return "external-lsa ";
        } else if (MAXMETRICSUMMARYLSA.class.equals(include)) {
            return "summary-lsa ";
        }
        return "";
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final String timeout = (data.getTimeout() != null) ? "on-startup " + data.getTimeout() : "";
        final StringBuilder builder = new StringBuilder();
        for (Class<? extends MAXMETRICINCLUDE> include : data.getInclude()) {
            builder.append(parseIncludes(include));
        }
        blockingWriteAndRead(cli, instanceIdentifier, data,
                "configure terminal",
                f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
                f("no max-metric router-lsa %s %s", timeout, builder.toString()),
                "commit",
                "end");
    }
}
