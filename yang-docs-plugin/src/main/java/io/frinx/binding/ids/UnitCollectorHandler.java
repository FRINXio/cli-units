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


public class UnitCollectorHandler {

    private CliUnitCollector cliUnitCollector;
    private UnitopoTranslationUnitCollector unitopoUnitCollector;


    public UnitCollectorHandler(final CliUnitCollector cliUnitCollector) {
        this.cliUnitCollector = cliUnitCollector;
    }

    public UnitCollectorHandler(final UnitopoTranslationUnitCollector unitopoUnitCollector) {
        this.unitopoUnitCollector = unitopoUnitCollector;
    }

    public boolean isUnitopo() {
        return unitopoUnitCollector != null;
    }

    public UnitopoTranslationUnitCollector getUnitopoUnitCollector() {
        return unitopoUnitCollector;
    }

    public CliUnitCollector getCliUnitCollector() {
        return cliUnitCollector;
    }
}
