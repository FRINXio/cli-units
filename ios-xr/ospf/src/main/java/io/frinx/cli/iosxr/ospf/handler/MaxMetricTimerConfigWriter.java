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

package io.frinx.cli.iosxr.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICONSWITCHOVER;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.max.metrics.fields.max.metric.timers.max.metric.timer.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICONSYSTEMBOOT;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MaxMetricTimerConfigWriter implements OspfWriter<Config> {

    private final Cli cli;

    public MaxMetricTimerConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, data,
            f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
            getMaxMetricCommands(data, false),
            "root");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                               WriteContext writeContext) throws WriteFailedException {

        blockingWriteAndRead(cli, instanceIdentifier, data,
            f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
            getMaxMetricCommands(data, true),
            "root");
    }

    private String getMaxMetricCommands(Config data, boolean delete) {
        final String timeout = (data.getTimeout() != null) ? data.getTimeout().toString() : "";
        final StringBuilder includes = new StringBuilder();
        if (data.getInclude() != null) {
            for (Class<? extends MAXMETRICINCLUDE> include : data.getInclude()) {
                includes.append(parseIncludes(include));
            }
        }
        final String prefix = delete ? "no " : "";
        String trigger_cmd;
        if (data.getTrigger().equals(MAXMETRICONSYSTEMBOOT.class)) {
            trigger_cmd = "on-startup";
        } else if (data.getTrigger().equals(MAXMETRICONSWITCHOVER.class)){
            trigger_cmd = "on-switchover";
        } else {
            return f("%smax-metric router-lsa %s", prefix, includes.toString());
        }
        return f("%smax-metric router-lsa %s %s %s", prefix, trigger_cmd, timeout, includes.toString());
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
}
