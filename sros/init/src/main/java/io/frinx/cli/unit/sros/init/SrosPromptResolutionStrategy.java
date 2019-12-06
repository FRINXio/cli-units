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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface SrosPromptResolutionStrategy extends PromptResolutionStrategy {
    Logger LOG = LoggerFactory.getLogger(SrosPromptResolutionStrategy.class);

    int INITIAL_TIME_TO_WAIT = 1;

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
                        .filter(line -> !"".equals(line))
                        .collect(Collectors.toList());

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
}
