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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleConfigWriter implements CliWriter<Config> {

    private static final String BUNDLE_MODE_TEMPLATE = "{% if ($lacp_mode) %}lacp mode"
            + "{% if ($lacp_mode == ACTIVE) %} active"
            + "{% else %} passive"
            + "{% endif %}\n"
            + "{% endif %}";
    private static final String BUNDLE_ADD_CONFIG_TEMPLATE = "interface {$ifc_name}\n"
            + BUNDLE_MODE_TEMPLATE
            + MemberConfigWriter.LACP_PERIOD_TEMPLATE
            + "root";
    private static final String BUNDLE_DELETE_CONFIG_TEMPLATE = "interface {$ifc_name}\n"
            + "no lacp mode\n"
            + "no lacp period short\n"
            + "root";

    private final Cli cli;

    public BundleConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        checkInterface(config.getName(), writeContext);
        writeBundleConfig(instanceIdentifier, config);
    }

    void writeBundleConfig(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config)
            throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(BUNDLE_ADD_CONFIG_TEMPLATE,
                        "ifc_name", config.getName(),
                        "lacp_mode", config.getLacpMode(),
                        "lacp_interval", config.getInterval()));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {
        final String ifcName = config.getName();
        checkInterface(ifcName, writeContext);
        removeBundleConfig(instanceIdentifier, config);
    }

    void removeBundleConfig(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config)
            throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(BUNDLE_DELETE_CONFIG_TEMPLATE, "ifc_name", config.getName()));
    }

    private static void checkInterface(@Nonnull String ifcName, WriteContext writeContext) {
        Optional<Interface> interfaceOptional = writeContext.readAfter(IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(ifcName)));
        Preconditions.checkArgument(interfaceOptional.isPresent(), "Cannot change LACP "
                + "configuration for non-existing interface %s", ifcName);
        Preconditions.checkArgument(AggregateConfigReader.isLAGInterface(ifcName),
                "Cannot change LACP configuration for non-bundle interface %s", ifcName);
    }
}
