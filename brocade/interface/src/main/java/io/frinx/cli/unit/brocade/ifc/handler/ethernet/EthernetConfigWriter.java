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

package io.frinx.cli.unit.brocade.ifc.handler.ethernet;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            {% if ($before == null) %}{% if ($autoNegotiate == true) %}configure terminal
            interface {$ifcName}
            gig-default auto-gig
            end{% endif %}{% if ($autoNegotiate == false) %}configure terminal
            interface {$ifcName}
            gig-default neg-off
            end{% endif %}{% endif %}{% if ($before == true) %}{% if ($autoNegotiate == false) %}configure terminal
            interface {$ifcName}
            gig-default neg-off
            end{% endif %}{% if ($autoNegotiate == null) %}configure terminal
            interface {$ifcName}
            no gig-default auto-gig
            end{% endif %}{% endif %}{% if ($before == false) %}{% if ($autoNegotiate == true) %}configure terminal
            interface {$ifcName}
            gig-default auto-gig
            end{% endif %}{% if ($autoNegotiate == null) %}configure terminal
            interface {$ifcName}
            no gig-default neg-off
            end{% endif %}{% endif %}""";

    private final Cli cli;

    public EthernetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        checkIfcType(ifcName);

        blockingWriteAndRead(getCommand(ifcName, null, dataAfter), cli, id, dataAfter);
    }

    @VisibleForTesting
    String getCommand(String ifcName, Config before, Config config) {
        return fT(WRITE_TEMPLATE, "ifcName", ifcName, "before", getAutoNeg(before),
                "autoNegotiate", getAutoNeg(config));
    }

    private String getAutoNeg(Config config) {
        return config == null
                ? "null"
                : (config.isAutoNegotiate() != null ? Boolean.toString(config.isAutoNegotiate()) : "null");
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        checkIfcType(ifcName);

        blockingDeleteAndRead(getCommand(ifcName, dataBefore, null), cli, id);
    }

    private void checkIfcType(String ifcName) {
        Preconditions.checkState(EthernetCsmacd.class.equals(Util.parseType(ifcName)),
                "Cannot change ethernet configuration for non ethernet interface %s", ifcName);
    }
}