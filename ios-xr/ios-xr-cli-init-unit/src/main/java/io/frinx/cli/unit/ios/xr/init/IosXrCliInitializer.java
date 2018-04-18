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

package io.frinx.cli.unit.ios.xr.init;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.io.impl.cli.PromptResolutionStrategy;
import io.frinx.cli.topology.RemoteDeviceId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.PrivilegedModeCredentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.privileged.mode.credentials.IosEnablePassword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * Initialize IOS-XR CLI session to be usable by various CRUD and RPC handlers
 */
public final class IosXrCliInitializer implements SessionInitializationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(IosXrCliInitializer.class);
    private static final String PASSWORD_PROMPT = "Password:";
    private static final String PRIVILEGED_PROMPT_SUFFIX = "#";
    private static final String ENABLE_COMMAND = "enable";
    private static final String CONFIG_PROMPT_SUFFIX = "(config)#";
    private static final String CONFIG_COMMAND = "configure terminal";
    private static final String END_COMMAND = "end";
    private static final String SET_TERMINAL_LENGTH_COMMAND = "terminal length 0";
    private static final String SET_TERMINAL_WIDTH_COMMAND = "terminal width 0";
    private static final int WRITE_TIMEOUT_SECONDS = 10;
    private static final int READ_TIMEOUT_SECONDS = 1;
    public static final Predicate<String> IS_CONFIGURATION_PROMPT = s -> s.endsWith(CONFIG_PROMPT_SUFFIX);
    public static final Predicate<String> IS_PRIVELEGE_PROMPT = s -> s.endsWith(PRIVILEGED_PROMPT_SUFFIX) && !s.endsWith(CONFIG_PROMPT_SUFFIX);

    private final CliNode context;
    private final RemoteDeviceId id;

    public IosXrCliInitializer(CliNode context, RemoteDeviceId id) {
        this.context = context;
        this.id = id;
    }

    @Override
    public void accept(@Nonnull Session session, @Nonnull String newline) {
        try {

            // Set terminal length to 0 to prevent "--More--" situation
            LOG.debug("{}: Setting terminal length to 0 to prevent \"--More--\" situation", id);
            write(session, newline, SET_TERMINAL_LENGTH_COMMAND);
            session.readUntilTimeout(READ_TIMEOUT_SECONDS);

            // Set terminal width to 0 to prevent command shortening
            LOG.debug("{}: Setting terminal width to 0", id);
            write(session, newline, SET_TERMINAL_WIDTH_COMMAND);
            session.readUntilTimeout(READ_TIMEOUT_SECONDS);

            // If already in privileged mode, don't do anything else
            if (PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim()
                    .endsWith(PRIVILEGED_PROMPT_SUFFIX)) {
                LOG.info("{}: IOS-XR cli session initialized successfully", id);
                return;
            }

            // Enable privileged mode
            tryToEnterPrivilegedMode(session, newline);

            // Check if we are actually in privileged mode
            String prompt = PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim();

            // If not, fail
            Preconditions.checkState(prompt.endsWith(PRIVILEGED_PROMPT_SUFFIX),
                    "%s: IOS cli session initialization failed to enter privileged mode. Current prompt: %s", id, prompt);

            LOG.info("{}: IOS-XR cli session initialized successfully", id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (SessionException | ExecutionException | TimeoutException e) {
            LOG.warn("{}: Unable to initialize device", id, e);
            throw new IllegalStateException(id + ": Unable to initialize device", e);
        }
    }

    private void tryToEnterPrivilegedMode(@Nonnull Session session, @Nonnull String newline)
            throws InterruptedException, ExecutionException, TimeoutException {

        write(session, newline, ENABLE_COMMAND);
        String enableCommandOutput = session.readUntilTimeout(READ_TIMEOUT_SECONDS).trim();

        // password is requested
        // TODO When reading from session, we can see all previously
        // unread output, that is also previous commands, prompt, etc.
        // That's why we have to use String#endWith method to check
        // if command's output matches something.
        // Can we hide this in session's API?
        if (enableCommandOutput.endsWith(PASSWORD_PROMPT)) {
            String password = getEnablePasswordFromCliNode();
            write(session, newline, password);

            String output = session.readUntilTimeout(READ_TIMEOUT_SECONDS).trim();
            if (output.endsWith(PASSWORD_PROMPT)) {
                LOG.warn("{}: Specified enable password is not correct", id);

                // We have entered incorrect password and we can be asked for
                // correct one multiple times. Just skip those requests.
                while (output.endsWith(PASSWORD_PROMPT)) {
                    session.write(newline).toCompletableFuture().get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    output = session.readUntilTimeout(READ_TIMEOUT_SECONDS).trim();
                }

            } else {
                LOG.debug("{}: Entering enable password resulted in output: {}", id, output);
            }
        } else {
            LOG.debug("{}: enable command did not resulted in password prompt, enable command output: {}",
                    id, enableCommandOutput);
        }
    }

    void tryToEnterConfigurationMode(Cli cli)
            throws InterruptedException, ExecutionException, TimeoutException {
        LOG.debug("Entering configuration mode on {}.", id);
        cli.executeAndSwitchPrompt(CONFIG_COMMAND, IS_CONFIGURATION_PROMPT)
                .toCompletableFuture().get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    void tryToExitConfigurationMode(Cli cli) throws InterruptedException, ExecutionException, TimeoutException {
        LOG.debug("Exiting configuration mode on {}.", id);
        cli.executeAndSwitchPrompt(END_COMMAND, IS_PRIVELEGE_PROMPT)
                .toCompletableFuture().get(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private String getEnablePasswordFromCliNode() {

        PrivilegedModeCredentials privilegedModeCredentials = context.getPrivilegedModeCredentials();
        if (privilegedModeCredentials != null) {
            if (context.getPrivilegedModeCredentials() instanceof IosEnablePassword) {
                return ((IosEnablePassword) privilegedModeCredentials).getSecret();
            }
        }

        LOG.debug("{}: Secret not set, using session password as enable password", id);

        checkArgument(context.getCredentials() instanceof LoginPassword,
                "%s: Unable to handle credentials type of: %s",
                id, context.getCredentials());

        return ((LoginPassword) context.getCredentials()).getPassword();
    }
}
