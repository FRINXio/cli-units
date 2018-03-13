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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.spi.write.CommitFailedException;
import io.fd.honeycomb.translate.spi.write.PostCommitHook;
import io.fd.honeycomb.translate.spi.write.PostFailedHook;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.WriterRegistry;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.io.impl.cli.PromptResolutionStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translate unit that does not actually translate anything.
 *
 * This translate unit's only responsibility is to properly initialize IOS-XR cli
 * session. That is, upon establishing connection to IOS-XR device, enter configuration
 * EXEC mode by issuing the 'configure terminal' command.
 */
public class IosXrCliInitializerUnit implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(IosXrCliInitializerUnit.class);

    // TODO This is reused all over the units. Move this to som Util class so
    // we can reuse it.
    private static final Device IOS_XR = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("*")
            .build();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration iosXrReg;

    public IosXrCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init()
    {
        iosXrReg = registry.registerTranslateUnit(IOS_XR, this);
    }

    public void close() {
        if (iosXrReg != null) {
            iosXrReg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return new IosXrCliInitializer(id);
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final TranslateUnit.Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        // NO-OP
    }

    @Override
    public PostCommitHook getCommitHook(TranslateUnit.Context context) {
        return () -> {
            String CONFIG_FAILED_PREFIX = "% Failed";
            try {
                String s = context.getTransport().executeAndRead("commit").toCompletableFuture().get();
                if (s.contains(CONFIG_FAILED_PREFIX)) {
                    LOG.warn("Commit failed. Reason: {}", s);
                    throw new CommitFailedException(s);
                } else {
                    LOG.info("Commit successful");
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Sending commit failed. Reason: {}", e.getMessage(), e);
            }
        };
    }

    @Override
    public PostFailedHook getPostFailedHook(TranslateUnit.Context context)  {
        return () -> {
            Cli cli = context.getTransport();
            try {
                String s = cli.executeAndRead("show configuration failed inheritance").toCompletableFuture().get();
                LOG.warn("Configuration failed: {}", s);

                // if this execution fails, the return to config mode was unsuccessful
                // the check if we are again in config mode is done automatically, so if no exception
                // is thrown, consider this as a success
                cli.executeAndRead("exit\nno\nconfig").toCompletableFuture().get();
                LOG.info("Reverting configuration on device successful.");
                throw new WriterRegistry.Reverter.RevertSuccessException(s);
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Failed to get the reason for commit failure. Reason: {}", e.getMessage(), e);
                throw new WriterRegistry.Reverter.RevertFailedException(e);
            }
        };
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
            Pattern.compile("\\s*\\^.*", Pattern.DOTALL),
            Pattern.compile("\\% (?i)invalid input(?-i).*", Pattern.DOTALL)
        ));
    }

    @Override
    public String toString() {
        return "IOS XR cli init (FRINX) translate unit";
    }

    /**
     * Initialize IOS CLI session to be usable by various CRUD and RPC handlers
     */
    public static final class IosXrCliInitializer implements SessionInitializationStrategy {
        private static final String CONFIG_PROMPT_SUFFIX = "(config)#";
        private static final String CONFIG_COMMAND = "configure terminal";
        private static final String SET_TERMINAL_LENGTH_COMMAND = "terminal length 0";
        private static final String SET_TERMINAL_WIDTH_COMMAND = "terminal width 0";
        private static final int READ_TIMEOUT_SECONDS = 1;

        private final RemoteDeviceId id;

        IosXrCliInitializer(RemoteDeviceId id) {
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
                        .endsWith(CONFIG_PROMPT_SUFFIX)) {
                    LOG.info("{}: IOS XR cli session initialized successfully", id);
                    return;
                }

                // Enter configured mode
                tryToEnterConfiguredMode(session, newline);

                // Check if we are actually in configured mode
                String prompt = PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim();

                // If not, fail
                Preconditions.checkState(prompt.endsWith(CONFIG_PROMPT_SUFFIX),
                        "%s: IOS XR cli session initialization failed to enter privileged mode. Current prompt: %s", id, prompt);

                LOG.info("{}: IOS XR cli session initialized successfully", id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (SessionException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to initialize device", id, e);
                throw new IllegalStateException(id + ": Unable to initialize device", e);
            }
        }

        private void tryToEnterConfiguredMode(@Nonnull Session session, @Nonnull String newline)
                throws InterruptedException, ExecutionException, TimeoutException {

            write(session, newline, CONFIG_COMMAND);
            String configCommandOutput = session.readUntilTimeout(READ_TIMEOUT_SECONDS).trim();

            if (configCommandOutput.endsWith(CONFIG_PROMPT_SUFFIX)) {
                LOG.debug("Entering configuration mode on {}.", id);
            } else {
                LOG.warn("{}: 'configure terminal' command did not result in enabling configuration mode, but in: {}",
                        id, configCommandOutput);
            }
        }
    }
}

