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


import com.x5.template.Chunk;
import com.x5.template.Theme;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;


/**
 * It manages all operation related to the Chunk template and is responsible for putting all information it in right
 * format for chunk engine.
 */
public final class YangModelBuilder {

    private static final String YANG_TEMPLATE = "module frinx-openconfig-{% $class_name %}-docs {\n"
            + "  yang-version \"1\";\n"
            + "  namespace \"http://frinx.openconfig.net/yang/{% $class_name %}:docs\";\n"
            + "  prefix \"frinx-oc-{% $devicetype|join(-) %}-docs\";\n"
            + "  import frinx-openconfig-docs { prefix oc-docs; } \n"
            + "  {% loop in $imports as $import %}\n"
            + "  {$import}                         \n"
            + "  {% onEmpty %}\n"
            + "  {% endonEmpty%}\n"
            + "  {% endloop %}\n"
            + "  description \"frinx {% $class_name %} documentation module\";\n"
            + "  revision \"{% $date %}\" {\n"
            + "    description \"Fixes to Ethernet interfaces model\";\n"
            + "  }\n"
            + "  {% loop in $augment as $aug %}\n"
            + "  augment \"{% $aug.key %}\" {\n"
            + "    container frinx-documentation {\n"
            + "    oc-docs:frinx-docs-deviceType \"{% $devicetype|join(-) %}\";\n"
            + "    oc-docs:frinx-docs-deviceVersion \"{% $deviceversions|join(,) %}\";\n"
            + "    {% if ($aug.value.reader != \"\") %} \toc-docs:frinx-docs-reader \"{% $aug.value.reader %}\"; \n"
            + "    {% if ($aug.value.rdetails != \"\") %} \t\t oc-docs:frinx-docs-reader-detail "
            + "\"{% $aug.value.rdetails %}\"; \n {% endif %}\n"
            + "      {% endif %}\n"
            + "    {% if ($aug.value.writer != \"\") %} \toc-docs:frinx-docs-writer \"{% $aug.value.writer %}\"; \n"
            + "    {% if ($aug.value.wdetails != \"\") %} \t\t oc-docs:frinx-docs-writer-detail "
            + "\"{% $aug.value.wdetails %}\"; \n {% endif %}\n"
            + " {% endif %}"
            + "   }\n"
            + "  }\n"
            + "  {% onEmpty %}\n"
            + "  {% endonEmpty%}\n"
            + "  {% endloop %}\n"
            + "}\n";

    private static final String IMPORT_SUBSTRING = "import %s { prefix %s; }";

    public YangModelBuilder() {}

    public String getYangModel(TranslationUnitMetadataHandler dataStoreHandler, SchemaContext context) {

        Chunk yangChunk;
        Theme theme = new Theme();
        yangChunk = theme.makeChunk();
        yangChunk.append(YANG_TEMPLATE);
        yangChunk.set("imports", getImports(dataStoreHandler, context));
        yangChunk.set("augment", new ArrayList<>(getMapForAugmentation(dataStoreHandler).entrySet()));
        yangChunk.set("class_name", dataStoreHandler.getDataStore().getSimpleName().toLowerCase());
        yangChunk.set("devicetype", dataStoreHandler.getDataStore().getDeviceType());
        yangChunk.set("deviceversions", dataStoreHandler.getDataStore().getDevicesVersion());
        yangChunk.set("date", LocalDate.now());

        return yangChunk.toString() ;
    }

    private List<String> getImports(TranslationUnitMetadataHandler dataStoreHandler, SchemaContext context) {

        Set<String> usedImports = new HashSet<>();
        List<String> unitedReadersWriters = dataStoreHandler.getDataStore().getReadersAsStrings()
                .collect(Collectors.toList());
        unitedReadersWriters.addAll(dataStoreHandler.getDataStore().getWritersAsStrings()
                .collect(Collectors.toList()));

        for (Module module : context.getModules()) {
            for (String oneReaderWriter : unitedReadersWriters) {

                if (oneReaderWriter.contains(module.getPrefix())) {

                    usedImports.add(String.format(IMPORT_SUBSTRING, module.getName(), module.getPrefix()));
                }
            }
        }
        return usedImports.stream().collect(Collectors.toList());
    }

    /**
     * As there are complex variables we want to implement into our yang template, this method creates a map
     * with the key of reader/writer path and then iterates all information we have for that exact reader/writer and
     * puts them into map.
     */
    private Map<String, ChunkDataHandler> getMapForAugmentation(TranslationUnitMetadataHandler dataStoreHandler) {
        Map<String, ChunkDataHandler> resultMap = new HashMap<>();

        Map<String, String> readersMap = dataStoreHandler.getReadersCustomizerMap();
        Map<String, String> writersMap = dataStoreHandler.getWritersCustomizerMap();

        Map<String, Map<String, String>> writersDetails = dataStoreHandler.getWriterConstantsMap();
        Map<String, Map<String, String>> readersDetails = dataStoreHandler.getReaderConstantsMap();

        readersDetails.putAll(dataStoreHandler.getReaderPatternsMap());

        Set<String> keySet = new HashSet<>();
        keySet.addAll(readersMap.keySet());
        keySet.addAll(writersMap.keySet());

        ChunkDataHandler dataHandler;
        for (String key : keySet) {
            dataHandler = new ChunkDataHandler(readersMap.getOrDefault(key, ""),
                    writersMap.getOrDefault(key, ""),
                    readersDetails.get(readersMap.get(key)),
                    writersDetails.get(writersMap.get(key)));
            resultMap.put(key, dataHandler);
        }
        return resultMap;
    }

    public static final class ChunkDataHandler implements java.io.Serializable {

        private final String reader;
        private final String writer;
        private final Map<String, String> readerDetailsMap;
        private final Map<String, String> writerDetailsMap;


        ChunkDataHandler(String reader, String writer, Map<String, String> readerDetailsMap,
                         Map<String, String> writerDetailsMap) {

            this.writer = writer;
            this.reader = reader;
            this.readerDetailsMap = readerDetailsMap;
            this.writerDetailsMap = writerDetailsMap;
        }

        public String getReader() {
            return reader;
        }

        public String getWriter() {
            return writer;
        }

        public String getRdetails() {

            if (readerDetailsMap == null) {
                return "";
            } else {
                return readerDetailsMap.keySet().stream().map(x -> StringEscapeUtils.escapeJava(
                        String.format(" %s:\n %s\n", x, readerDetailsMap.get(x)))).collect(Collectors.joining());
            }
        }

        public String getWdetails() {
            if (writerDetailsMap == null) {
                return "";
            } else {
                return writerDetailsMap.keySet().stream().map(x -> StringEscapeUtils.escapeJava(
                        String.format(" %s:\n %s\n", x, writerDetailsMap.get(x)))).collect(Collectors.joining());
            }
        }
    }
}
