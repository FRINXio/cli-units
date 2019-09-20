/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.sros.utils;

import io.frinx.translate.unit.commons.handler.spi.ChunkFormatter;

public final class InfoCmdUtil {

    private static final ChunkFormatter FORMAT = new ChunkFormatter(){};

    private InfoCmdUtil() {

    }

    private static final String INFO_COMMAND_TEMPLATE = "/configure\n"
        + "{% if ($location) %}"
        + "{$location}\n"
        + "{% endif %}"
        + "info"
        + "{% if ($pipe) %} | {$pipe}{% endif %}"
        + "\n"
        + "exit all";

    public static String genInfoCommand(String location, String pipe) {
        return FORMAT.fT(INFO_COMMAND_TEMPLATE,
            "location", location,
            "pipe", pipe);
    }

    public static String genInfoCommand(Settings settings) {
        return genInfoCommand(settings.location, settings.pipe);
    }

    public static class Settings {
        private String location;
        private String pipe;

        public Settings(String location, String pipe) {
            this.location = location;
            this.pipe = pipe;
        }

        public Settings(String location) {
            this.location = location;
            this.pipe = null;
        }
    }
}
