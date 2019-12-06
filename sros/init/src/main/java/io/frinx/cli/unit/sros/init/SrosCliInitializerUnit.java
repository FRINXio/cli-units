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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.spi.write.CommitFailedException;
import io.fd.honeycomb.translate.spi.write.PostCommitHook;
import io.fd.honeycomb.translate.spi.write.PostFailedHook;
import io.fd.honeycomb.translate.spi.write.PreCommitHook;
import io.fd.honeycomb.translate.write.registry.WriterRegistry;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.io.PromptResolutionStrategy;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SrosCliInitializerUnit extends AbstractUnit {

    private static final Logger LOG = LoggerFactory.getLogger(SrosCliInitializerUnit.class);
    private static final Command SH_CONF_FAILED = Command.showCommandNoCaching("candidate view");

    private static SrosCliInitializationStrategy INITIALIZER = new SrosCliInitializationStrategy();

    public SrosCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(SrosDevices.SROS_GENERIC);
    }

    @Override
    protected String getUnitName() {
        return "Sros cli init (FRINX) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final Context context) {
        readRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
        writeRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
    }

    @Override
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return INITIALIZER;
    }

    @Override
    public PreCommitHook getPreCommitHook(Context context) {
        return () -> {
            Cli cli = context.getTransport();
            try {
                SrosCliInitializationStrategy.tryToEnterConfigurationMode(cli);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to enter configuration mode", cli, e);
                throw new IllegalStateException(cli + ": Unable to enter configuration mode", e);
            }
        };
    }

    @Override
    @SuppressWarnings({"IllegalCatch", "AvoidHidingCauseException"})
    public PostCommitHook getCommitHook(Context context, Set<Pattern> errorCommitPatterns) {
        return () -> {
            Cli cli = null;
            try {
                cli = context.getTransport();
                SrosCliInitializationStrategy.tryToCommitAndSave(cli, errorCommitPatterns);
                LOG.debug("{}: Commit successful", cli);
            } catch (Exception e) {
                LOG.warn("{}: Commit failed", cli, e);

                String reason = "";
                try {
                    reason = cli.executeAndRead(SH_CONF_FAILED)
                        .toCompletableFuture()
                        .get();
                    LOG.warn("{}: Reason of commit failure - {}", cli, reason);
                } catch (InterruptedException | ExecutionException e1) {
                    LOG.warn("{}: Candidate view command failed", cli);
                }
                try {
                    SrosCliInitializationStrategy.tryToAbort(cli);
                    LOG.debug("{}: Exit configuration mode successful", cli);
                } catch (Exception e1) {
                    LOG.warn("{}: Unable to exit configuration mode", cli, e1);
                }
                throw new CommitFailedException(cli + ": Commit failed - " + reason);
            }
        };
    }

    @Override
    public PostFailedHook getPostFailedHook(Context context) {
        return (causingException) -> {
            Cli cli = context.getTransport();
            String message = Optional.ofNullable(causingException)
                    .map(SrosCliInitializerUnit::getBottomErrorMessage)
                    .orElse("Unknown reason.");
            LOG.warn("{}: Configuration failed: {}", cli, message);

            try {
                SrosCliInitializationStrategy.tryToAbort(cli);

                LOG.debug("{}: Revert successful.", cli);
                throw new WriterRegistry.Reverter.RevertSuccessException(message);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Failed to abort commit. Reason: {}", cli, e.getMessage(), e);
                throw new WriterRegistry.Reverter.RevertFailedException(e);
            }
        };
    }

    @Nullable
    private static String getBottomErrorMessage(Throwable throwable) {
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
                Pattern.compile("(^|\\n)(?i)MINOR:(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)(?i)Error:(?-i).*", Pattern.DOTALL)
        ));
    }

    @Override
    public Set<Pattern> getCommitErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile(".*(?i)Commit failed(?-i).*", Pattern.DOTALL)
            ));
    }

    @Override
    public PromptResolutionStrategy getPromptResolver() {
        return SrosPromptResolutionStrategy.ENTER_AND_READ;
    }
}
