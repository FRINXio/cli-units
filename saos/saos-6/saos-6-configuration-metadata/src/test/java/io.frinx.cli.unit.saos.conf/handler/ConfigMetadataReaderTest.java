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

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;

public class ConfigMetadataReaderTest {
    private static final String COMMAND_LOG =
            "+---------------------------------------------------------- COMMAND LOG --------------+\n"
                    +   "|  ID   |   Date & Time (Local)    | User(privilege) on Terminal / Exit Status         |\n"
                    +   "+-------+--------------------------+---------------------------------------------------+\n"
                    +   "| 92898 | Sat Jul 22 16:50:30 2000 | ciena(diag) /ssh_shell_172.30.100.2:51072         |\n"
                    +   "| configuration save\n"
                    +   "|       | Sat Jul 22 16:50:30 2000 | Status: 0                                         |\n"
                    +   "+-------+--------------------------+---------------------------------------------------+\n"
                    +   "| 92899 | Sat Jul 22 16:50:33 2000 | ciena(diag) /ssh_shell_172.30.100.2:51072         |\n"
                    +   "| command-log show verbose containing \"configuration save\" tail 2\n"
                    +   "|       | Sat Jul 22 16:50:33 2000 | Status: 0                                         |\n"
                    +   "+-------+--------------------------+---------------------------------------------------+\n";

    private static final String EXCEPTED_DATE = "Sat Jul 22 16:50:30 2000";
    private static final String WRONG_DATE = "Sat Jul 22 16:50:33 2000";

    @Test
    public void testParse() {
        Optional<String> date = ConfigMetadataReader.getLastConfigurationFingerprint(COMMAND_LOG);
        Assert.assertEquals(date.get(), EXCEPTED_DATE);

        Optional<String> date1 = ConfigMetadataReader.getLastConfigurationFingerprint(COMMAND_LOG);
        Assert.assertNotEquals(date1.get(), WRONG_DATE);

        Optional<String> date2 = ConfigMetadataReader.getLastConfigurationFingerprint("");
        Assert.assertFalse(date2.isPresent());
    }
}
