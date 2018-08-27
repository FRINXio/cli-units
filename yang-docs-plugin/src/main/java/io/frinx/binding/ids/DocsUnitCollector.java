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

import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;


/**
 * This class records all ids we have registered.
 */
public class DocsUnitCollector implements TranslationUnitCollector {

    private static final Registration REGISTRATION = () -> { };
    private Set<Device> ids;
    private TranslateUnit unit;

    @Override
    public Registration registerTranslateUnit(@Nonnull Device id, @Nonnull TranslateUnit unit) {

        this.ids = Stream.of(id).collect(Collectors.toSet());
        this.unit = unit;
        return REGISTRATION;
    }

    @Override
    public Registration registerTranslateUnit(@Nonnull Set<Device> ids, @Nonnull TranslateUnit unit) {
        this.ids = ids;
        this.unit = unit;
        return REGISTRATION;
    }

    public Set<Device> getDevicesIds() {
        return ids;
    }

    public TranslateUnit getUnit() {
        return unit;
    }
}
