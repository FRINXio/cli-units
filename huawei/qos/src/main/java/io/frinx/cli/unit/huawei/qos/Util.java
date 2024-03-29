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
package io.frinx.cli.unit.huawei.qos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Util {

    private Util() {

    }

    public static String deleteBrackets(final String output) {
        return output.replaceAll("\\([^()]*\\)", "");
    }

    public static String extractClass(final String className, final String output) {
        String string = String.format("(?<=Class %s\\r?\\n)(.*?)(?=\\s*Class|\\s*$)", className);
        Pattern pattern = Pattern.compile(string, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(output);
        return matcher.find() ? matcher.group() : "";
    }
}
