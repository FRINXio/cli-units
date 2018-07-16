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

package io.frinx.cli.unit.brocade.essential;

import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.Version;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.VersionBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VersionReader implements CliOperReader<Version, VersionBuilder> {

    private Cli cli;

    public VersionReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_VERSION = "sh version";

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Version> id,
                                      @Nonnull final VersionBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        parseShowVersion(blockingRead(SH_VERSION, cli, id, ctx), builder);
    }

    private static final Pattern DESCRIPTION_LINE = Pattern.compile("Compiled on .* labeled as (?<image>\\S+)");
    private static final Pattern VERSION_LINE = Pattern.compile("IronWare\\s+: Version (?<version>\\S+) Copyright.*");
    private static final Pattern PLATFORM_LINE = Pattern.compile("(Chassis|System): (?<platform>[^(]+) \\(Serial "
            + "#:\\s+(?<serial>[^,]+),\\s+Part #:\\s+(?<part>[^)]+)\\)");

    @VisibleForTesting
    static void parseShowVersion(String output, VersionBuilder builder) {
        parseField(output, 0,
                DESCRIPTION_LINE::matcher,
            matcher -> {
                return matcher.group("image");
            },
            builder::setSysImage);

        builder.setOsFamily("IronWare");
        parseField(output, 0,
            VERSION_LINE::matcher,
            matcher -> matcher.group("version"),
            builder::setOsVersion);
        parseField(output, 0,
            PLATFORM_LINE::matcher,
            matcher -> matcher.group("platform"),
            builder::setPlatform);
        parseField(output, 0,
            PLATFORM_LINE::matcher,
            matcher -> matcher.group("serial"),
            builder::setSerialId);
    }

    @Nonnull
    @Override
    public VersionBuilder getBuilder(@Nonnull InstanceIdentifier<Version> id) {
        return new VersionBuilder();
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> parentBuilder, @Nonnull final Version readValue) {
        // NOOP root element
    }
}
