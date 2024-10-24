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
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String BUNDLE_MODE_TEMPLATE = """
            {% if ($lacp_mode) %}lacp mode{% if ($lacp_mode == ACTIVE) %} active{% elseIf ($lacp_mode == PASSIVE) %} passive{% endif %}
            {% elseIf ($supported_bundle_mode == TRUE) %}no lacp mode
            {% endif %}""";
    private static final String BUNDLE_ADD_CONFIG_TEMPLATE = "interface {$ifc_name}\n"
            + BUNDLE_MODE_TEMPLATE
            + MemberConfigWriter.LACP_PERIOD_TEMPLATE
            + "root";
    private static final String BUNDLE_DELETE_CONFIG_TEMPLATE = """
            interface {$ifc_name}
            {% if ($lacp_mode) %}no lacp mode
            {% endif %}{% if ($lacp_interval) %}no lacp period short
            {% endif %}root""";

    private final Cli cli;

    public BundleConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        checkInterface(config.getName(), writeContext);
        writeBundleConfig(instanceIdentifier, config, false);
    }

    void writeBundleConfig(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                           @NotNull Config config,
                           boolean supportedBundleMode)
            throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(BUNDLE_ADD_CONFIG_TEMPLATE,
                        "ifc_name", config.getName(),
                        "lacp_mode", config.getLacpMode(),
                        "lacp_interval", config.getInterval(),
                        "supported_bundle_mode", supportedBundleMode));
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
                                        @NotNull Config dataAfter, @NotNull WriteContext writeContext)
            throws WriteFailedException {
        checkInterface(dataAfter.getName(), writeContext);
        if (Objects.isNull(dataBefore.getLacpMode())) {
            // lacp hasn't been set to active or passive mode - the command "no lacp mode" won't be send to device (XR5
            // doesn't support it)
            writeBundleConfig(id, dataAfter, false);
        } else {
            // lacp is already set to non-default value - then device also supports "no lacp mode" command
            writeBundleConfig(id, dataAfter, true);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config config,
                                        @NotNull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {
        final String ifcName = config.getName();
        checkInterface(ifcName, writeContext);
        removeBundleConfig(instanceIdentifier, config);
    }

    void removeBundleConfig(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config config)
            throws WriteFailedException.CreateFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(BUNDLE_DELETE_CONFIG_TEMPLATE,
                        "ifc_name", config.getName(),
                        "lacp_mode", config.getLacpMode(),
                        "lacp_interval", config.getInterval()));
    }

    private void checkInterface(@NotNull String ifcName, WriteContext writeContext) {
        Optional<Interface> interfaceOptional = writeContext.readAfter(IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(ifcName)));
        Preconditions.checkArgument(interfaceOptional.isPresent(), "Cannot change LACP "
                + "configuration for non-existing interface %s", ifcName);
        Preconditions.checkArgument(new AggregateConfigReader(cli).isLAGInterface(ifcName),
                "Cannot change LACP configuration for non-bundle interface %s", ifcName);
    }
}