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
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadata;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.configuration.metadata.rev180731.metadata.ConfigurationMetadataBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConfigMetadataReader implements CliOperReader<ConfigurationMetadata, ConfigurationMetadataBuilder> {

    @VisibleForTesting
    private static final String SHOW_LAST_COMMIT_TIME = "configuration show differences-from-saved";
    private static final Pattern PATTERN = Pattern.compile(".*< ! Created: {7}(?<date>[A-Za-z]{3} [A-Za-z]{3} +"
        + "([0-9]{1}|[0-9]{2}) [0-9]{2}:[0-9]{2}:[0-9]{2} [0-9]{4}).*");

    private final Cli cli;

    public ConfigMetadataReader(final Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static Optional<String> getLastConfigurationFingerprint(String output) {
        output = output.replaceAll("[\\n\\r]", "");
        return ParsingUtils.parseField(output, 0, PATTERN::matcher, m -> m.group("date"));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<ConfigurationMetadata> instanceIdentifier,
                                      @NotNull ConfigurationMetadataBuilder configurationMetadataBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SHOW_LAST_COMMIT_TIME, cli, instanceIdentifier, readContext);
        Optional<String> fingerPrint = getLastConfigurationFingerprint(output);

        if (fingerPrint.isPresent()) {
            configurationMetadataBuilder.setLastConfigurationFingerprint(fingerPrint.get());
        } else {
            configurationMetadataBuilder.setLastConfigurationFingerprint(DigestUtils.md5Hex(output));
        }
    }
}