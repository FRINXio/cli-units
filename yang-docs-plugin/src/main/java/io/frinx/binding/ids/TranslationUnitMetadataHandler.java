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

import io.fd.honeycomb.translate.impl.read.GenericReader;
import io.fd.honeycomb.translate.impl.write.GenericListWriter;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.Reader;
import io.fd.honeycomb.translate.write.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;


/**
 * It is responsible for complex data operations on the data that has been collected from the Unit.
 * It methods return data in specific format that can be used later by chunk engine.
 */
public final class TranslationUnitMetadataHandler {

    private final Log log = new SystemStreamLog();
    private final TranslationUnitMetadata dataStore;

    public TranslationUnitMetadataHandler(TranslationUnitMetadata dataStore) {

        this.dataStore = dataStore;

    }

    public TranslationUnitMetadata getDataStore() {
        return dataStore;
    }

    public Map<String, Map<String, String>> getWriterConstantsMap() {

        Map<String, Map<String, String>> resultMap = new HashMap<>();

        for (Writer<? extends DataObject> writer : dataStore.getWriterRegistryBuilder().getWritersMap().values()) {

            Map<String, String> valuesMap = new HashMap<>();
            Field[] fields;
            if (writer instanceof GenericWriter) {
                fields = getCustomizerFields(writer, GenericWriter.class);
            } else {
                fields = getCustomizerFields(writer, GenericListWriter.class);
            }

            for (Field field : fields) {
                field.setAccessible(true);

                if (field.getType() == String.class) {
                    try {
                        /*Replace is used to "escape" chunk template which is contained in string as it interferes with
                         parent template when creating YangModel.*/
                        valuesMap.put(field.getName(), field.get(null).toString().replace("%", "/%/"));
                    } catch (IllegalAccessException e) {
                        log.warn("Could not access constant strings for this unit. Ignoring this reader and resuming");
                    }
                }
            }
            if (valuesMap.size() > 0) {
                resultMap.put(getWriterCustomizerName(writer), valuesMap);
            }
        }
        return resultMap;
    }

    public Map<String, Map<String, String>> getReaderPatternAndConstantsMap() {

        Map<String, Map<String, String>> resultMap = new HashMap<>();

        for (Reader<? extends DataObject, ? extends Builder<?>> reader : dataStore.getReaderRegistryBuilder()
                .getReadersMap().values()) {

            Map<String, String> valuesMap = new HashMap<>();
            Field[] fields = getCustomizerFields(reader, GenericReader.class);

            for (Field field : fields) {
                field.setAccessible(true);

                if (field.getType() == Pattern.class || field.getType() == String.class) {
                    try {
                        valuesMap.put(field.getName(), field.get(null).toString());
                    } catch (IllegalAccessException e) {
                        log.warn("Could not access pattern for this unit. Ignoring this reader and resuming");
                    }
                }
            }
            if (valuesMap.size() > 0) {
                resultMap.put(getReaderCustomizerName(reader), valuesMap);
            }
        }
        return resultMap;
    }

    public Map<String, String> getReadersCustomizerMap() {

        Stream<String> readersName = dataStore.getReaderRegistryBuilder().getReadersMap()
                .values()
                .stream()
                .map(this::getReaderCustomizerName);

        return combineIntoMap(dataStore.getReadersAsStrings(), readersName);
    }

    public Map<String, String> getWritersCustomizerMap() {

        Stream<String> writersName = dataStore.getWriterRegistryBuilder().getWritersMap()
                .values()
                .stream()
                .map(this::getWriterCustomizerName);

        return combineIntoMap(dataStore.getWritersAsStrings(), writersName);
    }

    public Field[] getCustomizerFields(Object handler, Class<?> handlerClass) {
        if (handlerClass.isInstance(handler)) {
            try {
                Field customizer = handlerClass.getDeclaredField("customizer");
                customizer.setAccessible(true);

                return customizer.get(handler).getClass().getDeclaredFields();
            } catch (IllegalAccessException | NoSuchFieldException e) {

                log.warn("Could not get customizer field. Using simple name.");
                return new Field[]{};
            }
        }
        return new Field[]{};
    }

    private String getWriterCustomizerName(Writer<? extends DataObject> name) {
        if (name instanceof GenericWriter) {
            return getCustomizerField(name, GenericWriter.class);
        } else {
            return getCustomizerField(name, GenericListWriter.class);
        }
    }

    private Map<String, String> combineIntoMap(Stream<String> paths, Stream<String> names) {

        Map<String, String> resultMap = new HashMap<>();
        List<String> listPaths = paths.collect(Collectors.toList());
        List<String> listNames = names.collect(Collectors.toList());

        for (int i = 0; i < listPaths.size(); i++) {
            resultMap.put(listPaths.get(i), listNames.get(i));
        }
        return resultMap;
    }

    private String getReaderCustomizerName(Reader<? extends DataObject, ? extends Builder<?>> name) {
        return getCustomizerField(name, GenericReader.class);
    }

    private String getCustomizerField(Object handler, Class<?> handlerClass) {
        if (handlerClass.isInstance(handler)) {
            try {
                Field customizer = handlerClass.getDeclaredField("customizer");
                customizer.setAccessible(true);
                return customizer.get(handler).getClass().getName();
            } catch (IllegalAccessException | NoSuchFieldException e) {
                log.warn("Could not get customizer field. Ignoring this unit.");
                return dataStore.getName().getClass().getSimpleName();
            }
        } else {
            return dataStore.getName().getClass().getSimpleName();
        }
    }

}
