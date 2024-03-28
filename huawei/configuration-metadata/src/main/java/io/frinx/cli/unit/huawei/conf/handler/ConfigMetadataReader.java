/*
 * Copyright © 2023 Frinx and others.
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

package io.frinx.cli.unit.huawei.conf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadata;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadataBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConfigMetadataReader implements CliOperReader<ConfigurationMetadata, ConfigurationMetadataBuilder> {

    private static final String SHOW_FULL_CONFIG = "display current-configuration";

    private final Cli cli;

    public ConfigMetadataReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<ConfigurationMetadata> instanceIdentifier,
                                      @NotNull ConfigurationMetadataBuilder configurationMetadataBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var output = blockingRead(SHOW_FULL_CONFIG, cli, instanceIdentifier, readContext);
        parseFingerprint(output, configurationMetadataBuilder);
    }

    @VisibleForTesting
    static void parseFingerprint(String output, ConfigurationMetadataBuilder builder) {
        builder.setLastConfigurationFingerprint(DigestUtils.md5Hex(output));
    }
}