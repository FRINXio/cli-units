/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.essential.crud;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.version.StorageBuilder;

import static org.junit.Assert.assertEquals;

public class StorageReaderTest {
    private static final String SH_FILE_SYSTEM_XR = "Thu Oct  5 20:38:00.292 UTC\n" +
            "File Systems:\n" +
            "\n" +
            "     Size(b)     Free(b)        Type  Flags  Prefixes\n" +
            "           -           -     network     rw  qsm/dev/fs/tftp:\n" +
            "           -           -     network     rw  qsm/dev/fs/rcp:\n" +
            "           -           -     network     rw  qsm/dev/fs/ftp:\n" +
            "  2377105408  1559088128  dumper-lnk     rw  qsm/dumper_disk0:\n" +
            "   908369920   908017664  dumper-lnk     rw  qsm/dumper_harddisk:\n" +
            "    99399680    99379200  dumper-lnk     rw  qsm/dumper_nvram:\n" +
            "   798384128   541450240  dumper-lnk     rw  qsm/dumper_bootflash:\n" +
            "  2377105408  1559088128  flash-disk     rw  disk0:\n" +
            "   908369920   908017664    harddisk     rw  harddisk:\n" +
            "    99399680    99379200       nvram     rw  nvram:\n" +
            "   798384128   541450240       flash     rw  bootflash:";

    @Test
    public void testParseXRShowFileSystem() {
        StorageBuilder expectedStorage = new StorageBuilder();
        expectedStorage.setStorageSize("4183259136B")
                .setAvailableBytes("3107935232B");

        StorageBuilder actualStorage = new StorageBuilder();
        StorageReader.parseShowFileSystem(SH_FILE_SYSTEM_XR, actualStorage);
        assertEquals(expectedStorage.build(), actualStorage.build());
    }
}
