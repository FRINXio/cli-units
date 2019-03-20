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

package io.frinx.cli.unit.iosxr.lacp.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.aggregation.lacp.members.top.members.member.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class MemberConfigWriter implements CliWriter<Config> {

    private static final String LACP_OPERATION_MODE_TEMPLATE = "mode"
            + "{% if ($lacp_mode == ACTIVE) %} active"
            + "{% elseIf ($lacp_mode == PASSIVE) %} passive"
            + "{% else %} on"
            + "{% endif %}"
            + "\n";
    static final String LACP_PERIOD_TEMPLATE = "{% if ($lacp_interval == FAST) %}lacp period short\n"
            + "{% else %}no lacp period short\n"
            + "{% endif %}";
    private static final String IFC_ETHERNET_CONFIG_TEMPLATE = "interface {$ifc_name}\n"
            + "{% if ($bundle_id) %}bundle id {$bundle_id} "
            + LACP_OPERATION_MODE_TEMPLATE
            + "{% else %}no bundle id\n"
            + "{% endif %}"
            + LACP_PERIOD_TEMPLATE
            + "root";
    private static final String IFC_ETHERNET_REMOVE_CONFIG_TEMPLATE = "interface {$ifc_name}\n"
            + "no bundle id\n"
            + "no lacp period short\n"
            + "root";

    private final Cli cli;

    public MemberConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String bundleName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final String bundleId = MemberReader.parseBundleIdFromBundleName(bundleName);
        writeMemberConfig(instanceIdentifier, config, bundleId);
    }

    void writeMemberConfig(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                           @Nonnull String bundleId)
            throws WriteFailedException.CreateFailedException {
        final String ifcName = config.getInterface();
        checkIfcType(ifcName);
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(IFC_ETHERNET_CONFIG_TEMPLATE,
                        "ifc_name", ifcName,
                        "lacp_interval", config.getInterval(),
                        "lacp_mode", config.getLacpMode(),
                        "bundle_id", bundleId));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String ifcName = config.getInterface();
        checkIfcType(ifcName);
        blockingDeleteAndRead(cli, instanceIdentifier,
                fT(IFC_ETHERNET_REMOVE_CONFIG_TEMPLATE,
                        "ifc_name", ifcName));
    }

    private void checkIfcType(@Nonnull String ifcName) {
        Preconditions.checkState(Util.parseType(ifcName) == EthernetCsmacd.class,
                "Cannot change ethernet configuration for non ethernet interface %s", ifcName);
    }
}
