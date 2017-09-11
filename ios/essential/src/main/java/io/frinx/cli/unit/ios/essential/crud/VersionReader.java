/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.essential.crud;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.Version;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.VersionBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

public class VersionReader implements CliReader<Version, VersionBuilder> {

    private Cli cli;

    public VersionReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_VERSION = "sh version";

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Version> id,
                                      @Nonnull final VersionBuilder builder,
                                      @Nonnull final ReadContext ctx) throws ReadFailedException {
        parseVersion(blockingRead(SH_VERSION, cli, id), builder);
    }

    private static final Pattern DESCRIPTION_LINE = Pattern.compile("System image file is \"(?<image>[^\"]+)\"");
    private static final Pattern FIRST_LINE = Pattern.compile("(?<osFamily>.+) Software, (?<platform>.+) Software .* Version (?<version>.+)");
    private static final Pattern REGISTRY_LINE = Pattern.compile("Configuration register is (?<registry>.+)");
    private static final Pattern MEMORY_LINE = Pattern.compile(".*with (?<memory>.+) bytes of memory.*");
    private static final Pattern PROCESSOR_LINE = Pattern.compile("Processor board ID (?<processor>.+)");

    @VisibleForTesting
    static void parseVersion(String output, VersionBuilder builder) {
        parseField(output, 0,
                DESCRIPTION_LINE::matcher,
                matcher -> matcher.group("image"),
                builder::setSysImage);

        parseField(output, 0,
                FIRST_LINE::matcher,
                matcher -> matcher.group("osFamily"),
                builder::setOsFamily);

        parseField(output, 0,
                FIRST_LINE::matcher,
                matcher -> matcher.group("platform"),
                builder::setPlatform);

        parseField(output, 0,
                FIRST_LINE::matcher,
                matcher -> matcher.group("version"),
                builder::setOsVersion);

        parseField(output, 0,
                REGISTRY_LINE::matcher,
                matcher -> matcher.group("registry"),
                builder::setConfReg);

        parseField(output, 0,
                MEMORY_LINE::matcher,
                matcher -> matcher.group("memory"),
                builder::setSysMemory);

        parseField(output, 0,
                PROCESSOR_LINE::matcher,
                matcher -> matcher.group("processor"),
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
