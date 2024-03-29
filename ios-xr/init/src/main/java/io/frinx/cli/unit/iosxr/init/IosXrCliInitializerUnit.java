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

package io.frinx.cli.unit.iosxr.init;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.spi.write.CommitFailedException;
import io.fd.honeycomb.translate.spi.write.PostCommitHook;
import io.fd.honeycomb.translate.spi.write.PostFailedHook;
import io.fd.honeycomb.translate.spi.write.PreCommitHook;
import io.fd.honeycomb.translate.write.RevertFailedException;
import io.fd.honeycomb.translate.write.RevertSuccessException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.utils.AbstractUnitWithNativeSupport;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translate unit that does not actually translate anything.
 *
 * <p>
 * This translate unit's only responsibility is to properly initialize IOS-XR cli
 * session. That is, upon establishing connection to IOS-XR device, enter Privileged
 * EXEC mode by issuing the 'enable' command.
 */
public class IosXrCliInitializerUnit extends AbstractUnitWithNativeSupport {

    private static final Command CONFIG_COMMAND = Command.writeCommand("configure terminal");
    private static final String CONFIG_PROMPT_SUFFIX = "(config)#";
    private static final Predicate<String> IS_CONFIGURATION_PROMPT = s -> s.endsWith(CONFIG_PROMPT_SUFFIX);
    private static final Logger LOG = LoggerFactory.getLogger(IosXrCliInitializerUnit.class);

    private static final Command ABORT = Command.writeCommand("abort");
    private static final Command END_COMMAND = Command.writeCommand("end");
    private static final Predicate<String> IS_PRIVELEGE_PROMPT =
        s -> s.endsWith(IosXrCliInitializer.PRIVILEGED_PROMPT_SUFFIX) && !s
        .endsWith(CONFIG_PROMPT_SUFFIX);

    @VisibleForTesting
    static final Command SH_CONF_FAILED = Command.showCommandNoCaching("show configuration failed inheritance");

    public IosXrCliInitializerUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(IosXrDevices.IOS_XR_GENERIC);
    }

    @Override
    protected String getUnitName() {
        return "IOS XR cli init (FRINX) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@NotNull final RemoteDeviceId id,
                                                        @NotNull final CliNode cliNodeConfiguration) {
        return new IosXrCliInitializer(cliNodeConfiguration, id);
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final Context context) {
        readRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
        writeRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
    }

    @Override
    public PreCommitHook getPreCommitHook(Context context) {
        return () -> {
            Cli cli = context.getTransport();
            try {
                tryToEnterConfigurationMode(cli, cli.getDeviceId());
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to enter configuration mode", cli.getDeviceId(), e);
                throw new IllegalStateException(cli.getDeviceId() + ": Unable to enter configuration mode", e);
            }
        };
    }

    @Override
    @SuppressWarnings({"IllegalCatch", "AvoidHidingCauseException"})
    public PostCommitHook getCommitHook(Context context, Set<Pattern> errorCommitPatterns) {
        return () -> {
            Cli cli = context.getTransport();
            try {
                try {
                    final Command commit = Command.writeCommandCustomChecks("commit", errorCommitPatterns);
                    cli.executeAndRead(commit)
                            .toCompletableFuture()
                            .get();
                    LOG.debug("{}: Commit successful", cli.getDeviceId());
                    try {
                        tryToExitConfigurationMode(cli, cli.getDeviceId());
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        LOG.warn("{}: Unable to exit configuration mode", cli.getDeviceId(), e);
                        throw new IllegalStateException(cli.getDeviceId() + ": Unable to exit configuration mode", e);
                    }
                } catch (Exception e) {
                    LOG.warn("{}: Commit failed", cli.getDeviceId());
                    String reason = cli.executeAndRead(SH_CONF_FAILED)
                            .toCompletableFuture()
                            .get();
                    LOG.warn("{}: Reason of commit failure - {}", cli.getDeviceId(), reason);
                    throw new CommitFailedException(reason);
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("{}: Sending commit failed. Reason: {}", cli.getDeviceId(), e.getMessage(), e);
            }
        };
    }

    @Override
    public PostFailedHook getPostFailedHook(Context context) {
        return (causingException) -> {
            Cli cli = context.getTransport();
            String message = Optional.ofNullable(causingException)
                    .map(this::getBottomErrorMessage)
                    .orElse("Unknown reason.");
            LOG.warn("{}: Configuration failed: {}", cli.getDeviceId(), message);

            try {
                // if this execution fails, the return to config mode was unsuccessful
                // the check if we are again in config mode is done automatically, so if no exception
                // is thrown, consider this as a success
                cli.executeAndSwitchPrompt(ABORT, IS_PRIVELEGE_PROMPT)
                        .toCompletableFuture()
                        .get();
                LOG.debug("{}: Revert successful.", cli.getDeviceId());
                throw new RevertSuccessException(message);
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("{}: Failed to abort commit. Reason: {}", cli.getDeviceId(), e.getMessage(), e);
                throw new RevertFailedException(e);
            }
        };
    }


    private static void tryToEnterConfigurationMode(Cli cli, final String id)
            throws InterruptedException, ExecutionException, TimeoutException {
        LOG.debug("Entering configuration mode on {}.", id);
        cli.executeAndSwitchPrompt(CONFIG_COMMAND, IS_CONFIGURATION_PROMPT)
                .toCompletableFuture()
                .get(IosXrCliInitializer.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static void tryToExitConfigurationMode(Cli cli, final String id)
            throws InterruptedException, ExecutionException, TimeoutException {
        LOG.debug("Exiting configuration mode on {}.", id);
        cli.executeAndSwitchPrompt(END_COMMAND, IS_PRIVELEGE_PROMPT)
                .toCompletableFuture()
                .get(IosXrCliInitializer.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private @Nullable
        String getBottomErrorMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return Strings.emptyToNull(cause.getMessage());
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile("(^|\\n)\\s+\\^.*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% (?i)invalid input(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% (?i)Incomplete command(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% (?i)Ambiguous command(?-i).*", Pattern.DOTALL)
        ));
    }

    @Override
    public Set<Pattern> getCommitErrorPatterns() {
        return Sets.newLinkedHashSet(Collections.singletonList(
                Pattern.compile("(^|\\n)% (?i)Failed(?-i).*")
        ));
    }
}