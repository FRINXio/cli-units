/*
 * Copyright © 2021 Frinx and others.
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
package io.frinx.cli.unit.huawei.qos.handler.classifier;

public enum ClassMapType {

    MATCH_ALL("all"),
    MATCH_ANY("any");

    private String value;

    ClassMapType(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    public static ClassMapType parseOutput(String output) {
        return MATCH_ALL.value.equals(output) ? MATCH_ALL : MATCH_ANY;
    }
}
