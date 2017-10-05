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
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.frinx.cli.unit.utils.ParsingUtils.parseField;
import static io.frinx.cli.unit.utils.ParsingUtils.parseFields;

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
        parseShowVersion(blockingRead(SH_VERSION, cli, id), builder);
    }

    private static final Pattern DESCRIPTION_LINE = Pattern.compile("System image file is \"(?<image>[^\"]+)\"");
    private static final Pattern FIRST_LINE = Pattern.compile("(?<osFamily>.+) Software, .*");
    private static final Pattern VERSION_LINE = Pattern.compile(".+ Software, .*Version (?<version>.+)");
    private static final Pattern REGISTRY_LINE = Pattern.compile("Configuration register .*is (?<registry>.+)");
    private static final Pattern PROCESSOR_LINE = Pattern.compile("Processor board ID (?<processor>.+)");
    private static final Pattern PLATFORM_AND_MEMORY_LINE = Pattern.compile("(?<platform>.+) with (?<memory>.+) bytes of memory.*");

    @VisibleForTesting
    static void parseShowVersion(String output, VersionBuilder builder) {
        parseField(output, 0,
                DESCRIPTION_LINE::matcher,
                matcher -> matcher.group("image"),
                builder::setSysImage);

        parseField(output, 0,
                FIRST_LINE::matcher,
                matcher -> matcher.group("osFamily"),
                builder::setOsFamily);

        parseField(output, 0,
                PLATFORM_AND_MEMORY_LINE::matcher,
                matcher -> matcher.group("platform"),
                builder::setPlatform);

        // For IOS XE this will match IOS version
        // but also the platform version.
        // We want to parse the latter one.
        parseFields(output, 0,
                VERSION_LINE::matcher,
                matcher -> matcher.group("version"),
                Function.identity()).stream().reduce((a, b) -> b).ifPresent(builder::setOsVersion);

        parseField(output, 0,
                REGISTRY_LINE::matcher,
                matcher -> matcher.group("registry"),
                builder::setConfReg);

        parseField(output, 0,
                PLATFORM_AND_MEMORY_LINE::matcher,
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
