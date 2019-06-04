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

package io.frinx.cli.unit.junos.init;

import com.google.common.base.Preconditions;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.io.PromptResolutionStrategy;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JunosCliInitializationStrategy implements SessionInitializationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(JunosCliInitializationStrategy.class);

    private static final Pattern SHELL_PROMPT_PATTERN = Pattern.compile(".*@.*:.+#");//include ':'(collron)
    private static final String PRIVILEGED_PROMPT_SUFFIX = ">";
    private static final String CONFIG_PROMPT_SUFFIX = "#";

    private static final String CLI_COMMAND = "cli";

    private static final String DISABLE_COMPLETE_ON_SPACE = "set cli complete-on-space off";
    private static final String SET_TERMINAL_LENGTH_COMMAND = "set cli screen-length 0";
    private static final String SET_TERMINAL_WIDTH_COMMAND = "set cli screen-width 0";

    private static final int READ_TIMEOUT_SECONDS = 1;
    private static final int WRITE_TIMEOUT_SECONDS = 10;

    private static final Command CONFIG_COMMAND = Command.writeCommand("configure");
    private static final Command END_COMMAND = Command.writeCommand("exit configuration-mode");
    private static final Command ABORT_COMMAND = Command.writeCommand("rollback 0");
    private static final String COMMIT = "commit";

    private static final Predicate<String> IS_SHELL_PROMPT = s -> SHELL_PROMPT_PATTERN.matcher(s).matches();
    private static final Predicate<String> IS_PRIVELEGE_PROMPT = s -> s.endsWith(PRIVILEGED_PROMPT_SUFFIX);
    private static final Predicate<String> IS_CONFIGURATION_PROMPT =
        s -> s.endsWith(CONFIG_PROMPT_SUFFIX) && !IS_SHELL_PROMPT.test(s);

    private static final PromptResolutionStrategy PROMPT_RESOLUTION_STRATEGY =
        JunosPromptResolutionStrategy.getInstance();

    @Override
    public void accept(Session session, String newline) {
        try {
            String initialPrompt = PROMPT_RESOLUTION_STRATEGY.resolvePrompt(session, newline).trim();

            if (IS_SHELL_PROMPT.test(initialPrompt)) {
                // enable cli mode
                tryToEnterCliMode(session, newline);
            }

            String prompt = PROMPT_RESOLUTION_STRATEGY.resolvePrompt(session, newline).trim();

            Preconditions.checkState(IS_PRIVELEGE_PROMPT.test(prompt),
                    "%s: Junos cli session initialization failed to enter cli mode. Current prompt: %s",
                    session, prompt);

            LOG.debug("{}: Setting terminal complete command on space to off", session);
            write(session, newline, DISABLE_COMPLETE_ON_SPACE);
            session.readUntilTimeout(READ_TIMEOUT_SECONDS);

            LOG.debug("{}: Setting terminal length to 0 to prevent \"--More--\" situation", session);
            write(session, newline, SET_TERMINAL_LENGTH_COMMAND);
            session.readUntilTimeout(READ_TIMEOUT_SECONDS);

            LOG.debug("{}: Setting terminal width to 0", session);
            write(session, newline, SET_TERMINAL_WIDTH_COMMAND);
            session.readUntilTimeout(READ_TIMEOUT_SECONDS);

            LOG.info("{}: Junos cli session initialized successfully", session);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (SessionException | ExecutionException | TimeoutException e) {
            LOG.warn("{}: Unable to initialize device", session, e);
            throw new IllegalStateException(session + ": Unable to initialize device", e);
        }
    }

    private void tryToEnterCliMode(@Nonnull Session session, @Nonnull String newline)
            throws InterruptedException, ExecutionException, TimeoutException {

        LOG.debug("{}: Entering cli mode.", session);
        write(session, newline, CLI_COMMAND);
        String cliCommandOutput = session.readUntilTimeout(READ_TIMEOUT_SECONDS).trim();
        LOG.debug("{}: Entering cli mode resulted in output: {}", session, cliCommandOutput);
    }

    static void tryToEnterConfigurationMode(Cli cli)
            throws InterruptedException, ExecutionException, TimeoutException {

        LOG.debug("{}: Entering configuration mode.", cli);
        cli.executeAndSwitchPrompt(CONFIG_COMMAND, IS_CONFIGURATION_PROMPT)
                .toCompletableFuture()
                .get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    static void tryToExitConfigurationMode(Cli cli, boolean abort)
            throws InterruptedException, ExecutionException, TimeoutException {

        if (abort) {
            tryToAbort(cli);
        }

        LOG.debug("{}: Exiting configuration mode.", cli);
        cli.executeAndSwitchPrompt(END_COMMAND, IS_PRIVELEGE_PROMPT)
                .toCompletableFuture()
                .get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static void tryToAbort(Cli cli)
            throws InterruptedException, ExecutionException, TimeoutException {

        LOG.debug("{}: Executing rollback command.", cli);
        cli.executeAndRead(ABORT_COMMAND)
                .toCompletableFuture()
                .get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    static void tryToCommit(Cli cli, Set<Pattern> errorCommitPatterns)
            throws InterruptedException, ExecutionException {

        LOG.debug("{}: Executing commit command.", cli);
        Command commit = Command.writeCommandCustomChecks(COMMIT, errorCommitPatterns);
        cli.executeAndRead(commit)
                .toCompletableFuture()
                .get();
    }
}