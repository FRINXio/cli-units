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

package io.frinx.binding.ids;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;


/**
 * Class responsible for writing our generated Yang into file.
 */
public final class YangDocsWriter {

    private static final String DIR = "/yang/META-INF/yang";
    private static final String FILE_NAME = "frinx-openconfig-%s-%s.yang";
    private final Log log = new SystemStreamLog();

    YangDocsWriter() {

    }

    public File writeOut(File outputBaseDir, TranslationUnitMetadataHandler dataStoreHandler, SchemaContext context) {

        File outputDir = null;
        try {
            outputDir = new File(outputBaseDir.getParentFile().getAbsolutePath() + DIR);
            outputDir.mkdirs();
        } catch (SecurityException e) {
            log.warn("Could not create directory for output, you might need rights to create directories.", e);
        }

        File outPutFile = new File(outputDir,
                 String.format(FILE_NAME, dataStoreHandler.getDataStore().getDeviceType(),
                         dataStoreHandler.getDataStore().getSimpleName()));

        YangModelBuilder yangModelBuilder = new YangModelBuilder();

        try {
            Files.write(outPutFile.toPath(), Collections.singleton(yangModelBuilder
                    .getYangModel(dataStoreHandler, context)));
        } catch (IOException e) {
            log.warn("Writing to file failed. Wont generate documentation for this unit", e);
        }
        return outPutFile;
    }
}
