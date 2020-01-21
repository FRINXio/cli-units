/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.conf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import javax.annotation.Nonnull;
import org.apache.commons.codec.digest.DigestUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadata;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadataBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConfigMetadataReader implements CliOperReader<ConfigurationMetadata, ConfigurationMetadataBuilder> {

    @VisibleForTesting
    static final String SHOW_RUNNING_CONFIG = "configuration show brief";

    private final Cli cli;

    public ConfigMetadataReader(final Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static String getLastConfigurationFingerprint(String config) {
        return DigestUtils.md5Hex(config);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ConfigurationMetadata> instanceIdentifier, @Nonnull
            ConfigurationMetadataBuilder configurationMetadataBuilder, @Nonnull ReadContext readContext) throws
            ReadFailedException {
        String output = blockingRead(SHOW_RUNNING_CONFIG, cli, instanceIdentifier, readContext);

        String fingerPrint = getLastConfigurationFingerprint(output);
        configurationMetadataBuilder.setLastConfigurationFingerprint(fingerPrint);
    }
}

