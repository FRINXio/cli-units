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

package io.frinx.cli.unit.brocade.conf.handler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadata;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadataBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConfigMetadataReader implements CliOperReader<ConfigurationMetadata, ConfigurationMetadataBuilder> {

    private final Cli cli;
    private int counter = 0;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    public ConfigMetadataReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<ConfigurationMetadata> instanceIdentifier,
                                      @NotNull ConfigurationMetadataBuilder configurationMetadataBuilder,
                                      @NotNull ReadContext readContext) {
        configurationMetadataBuilder.setLastConfigurationFingerprint(String.format("%d", counter++));
    }
}