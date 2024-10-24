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

package io.frinx.cli.unit.sros.init;

import io.frinx.cli.io.PromptResolutionStrategy;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface SrosPromptResolutionStrategy extends PromptResolutionStrategy {
    Logger LOG = LoggerFactory.getLogger(SrosPromptResolutionStrategy.class);

    int INITIAL_TIME_TO_WAIT = 1;
    String TRANSACTION_PROMPT_SUFFIX = "[/]";

    /**
     * Simple resolution strategy writing newline with space at head and expecting a prompt to be printed.<br>
     * Connecting to SROS with ssh has a problem that SROS returns only one prompt<br>
     * when even if ODL writes two line feeds continuously.<br>
     * This problem does not occur when connecting via telnet.
     */
    @SuppressWarnings({"IllegalCatch", "ConstantName"})
    SrosPromptResolutionStrategy ENTER_AND_READ = (session, newLine) -> {
        int waitTime = INITIAL_TIME_TO_WAIT;
        String lastRead = "";

        try {
            while (true) {
                session.write(" " + newLine + " " + newLine).toCompletableFuture().get();
                lastRead = session.readUntilTimeout(waitTime);
                List<String> split = Arrays.stream(lastRead.split(newLine))
                        .map(String::trim)
                        .filter(SrosPromptResolutionStrategy::isValidPromptLine)
                        .toList();

                if (split.size() == 2 && split.get(0).equals(split.get(1))) {
                    return split.get(0);
                }

                waitTime++;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException | RuntimeException e) {
            LOG.warn("{}: Unable to perform prompt resolution in {} attempts, last output from device: {}",
                    session, waitTime, lastRead, e);

            throw new IllegalStateException(session + ": Unable to parse prompt in " + waitTime
                    + " attempts, last output from device: " + lastRead, e);
        }
    };

    private static boolean isValidPromptLine(final String line) {
        return !line.isEmpty()
                // SROS returns additionally [/] on a new line when CLI is in the transaction mode
                && !TRANSACTION_PROMPT_SUFFIX.equals(line)
                // SROS can return ISO-8601 timestamp after the command is executed
                && !isTimestamp(line);
    }

    private static boolean isTimestamp(String line) {
        try {
            Instant.parse(line);
            return true;
        } catch (DateTimeParseException e) {
            // not a timestamp
            return false;
        }
    }
}
