/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.essential.crud;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.version.StorageBuilder;

import static org.junit.Assert.assertEquals;

public class StorageReaderTest {
    private static final String SH_FILE_SYSTEM_XE = "File Systems:\n" +
            "\n" +
            "       Size(b)       Free(b)      Type  Flags  Prefixes\n" +
            "             -             -    opaque     rw   system:\n" +
            "             -             -    opaque     rw   tmpsys:\n" +
            "*  16290590720   14679863296      disk     rw   bootflash: flash:\n" +
            "             -             -    opaque     rw   null:\n" +
            "             -             -    opaque     ro   tar:\n" +
            "             -             -   network     rw   tftp:\n" +
            "      33554432      33542283     nvram     rw   nvram:\n" +
            "             -             -    opaque     wo   syslog:\n" +
            "             -             -   network     rw   rcp:\n" +
            "             -             -   network     rw   ftp:\n" +
            "             -             -   network     rw   http:\n" +
            "             -             -   network     rw   scp:\n" +
            "             -             -   network     rw   https:\n" +
            "             -             -    opaque     ro   cns:\n" +
            "\n";

    private static final String SH_FILE_SYSTEM_CLASSIC_IOS = "File Systems:\n" +
            "\n" +
            "       Size(b)       Free(b)      Type  Flags  Prefixes\n" +
            "             -             -    opaque     rw   system:\n" +
            "             -             -    opaque     rw   tmpsys:\n" +
            "             -             -    opaque     rw   null:\n" +
            "             -             -    opaque     ro   tar:\n" +
            "             -             -   network     rw   tftp:\n" +
            "             -             -    opaque     wo   syslog:\n" +
            "        522232        517183     nvram     rw   nvram:\n" +
            "*            -             -      disk     rw   disk0:\n" +
            "             -             -      disk     rw   disk1:\n" +
            "             -             -     flash     rw   slot0: flash:\n" +
            "             -             -     flash     rw   slot1:\n" +
            "             -             -     flash     rw   bootflash:\n" +
            "             -             -   network     rw   rcp:\n" +
            "             -             -   network     rw   pram:\n" +
            "             -             -   network     rw   ftp:\n" +
            "             -             -   network     rw   http:\n" +
            "             -             -   network     rw   scp:\n" +
            "             -             -   network     rw   https:\n" +
            "             -             -    opaque     ro   cns:\n";

    @Test
    public void testParseXEShowFileSystem() {
        StorageBuilder expectedStorage = new StorageBuilder();
        expectedStorage.setStorageSize("16324145152B")
                .setAvailableBytes("14713405579B");

        StorageBuilder actualStorage = new StorageBuilder();
        StorageReader.parseShowFileSystem(SH_FILE_SYSTEM_XE, actualStorage);
        assertEquals(expectedStorage.build(), actualStorage.build());
    }

    @Test
    public void testParseClassicIOSShowFileSystem() {
        StorageBuilder expectedStorageInfo = new StorageBuilder();
        expectedStorageInfo.setStorageSize("522232B")
                .setAvailableBytes("517183B");

        StorageBuilder actualStorageInfo = new StorageBuilder();
        StorageReader.parseShowFileSystem(SH_FILE_SYSTEM_CLASSIC_IOS, actualStorageInfo);
        assertEquals(expectedStorageInfo.build(), actualStorageInfo.build());
    }
}
