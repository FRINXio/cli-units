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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.VersionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.version.Storage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.version.StorageBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.frinx.cli.unit.utils.ParsingUtils.parseFields;

public class StorageReader implements CliReader<Storage, StorageBuilder> {

    private static final String SH_FILE_SYSTEM = "sh file system";
    private static final Pattern FILE_SYSTEM_LINE = Pattern.compile("\\D*(?<size>\\d+)\\s+(?<freeSpace>\\d+).*");

    private Cli cli;

    public StorageReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public StorageBuilder getBuilder(@Nonnull InstanceIdentifier<Storage> id) {
        return new StorageBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Storage> id,
                                      @Nonnull StorageBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        parseShowFileSystem(blockingRead(SH_FILE_SYSTEM, cli, id), builder);
    }

    @VisibleForTesting
    static void parseShowFileSystem(String output, StorageBuilder builder) {
        Collection<String> sizeColumn = parseFields(output, 0,
                FILE_SYSTEM_LINE::matcher,
                matcher -> matcher.group("size"), Function.identity());

        long storageSize = sizeColumn.stream().mapToLong(Long::parseLong).sum();
        builder.setStorageSize(Long.toString(storageSize) + "B");

        Collection<String> freeSpaceColumn = parseFields(output, 0,
                FILE_SYSTEM_LINE::matcher,
                matcher -> matcher.group("freeSpace"), Function.identity());

        long freeSpace = freeSpaceColumn.stream().mapToLong(Long::parseLong).sum();
        builder.setAvailableBytes(Long.toString(freeSpace) + "B");
    }


    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Storage readValue) {
        ((VersionBuilder) parentBuilder).setStorage(readValue);
    }
}
