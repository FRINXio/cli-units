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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.spi.write.CommitFailedException;
import io.fd.honeycomb.translate.spi.write.PostCommitHook;
import io.fd.honeycomb.translate.spi.write.PostFailedHook;
import io.fd.honeycomb.translate.spi.write.PreCommitHook;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.WriterRegistry;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.PromptResolutionStrategy;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JunosCliInitializerUnit implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(JunosCliInitializerUnit.class);

    private static final Device JUNOS = new DeviceIdBuilder()
            .setDeviceType("junos")
            .setDeviceVersion("*")
            .build();

    private static final PromptResolutionStrategy PROMPT_RESOLUTION_STRATEGY =
        JunosPromptResolutionStrategy.getInstance();

    private static JunosCliInitializationStrategy INITIALIZER = new JunosCliInitializationStrategy();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public JunosCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(JUNOS, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final TranslateUnit.Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder readRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder writeRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        // NO-OP
    }

    @Override
    public String toString() {
        return "Junos cli init (FRINX) translate unit";
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
                JunosCliInitializationStrategy.tryToEnterConfigurationMode(cli);
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
                JunosCliInitializationStrategy.tryToCommit(cli, errorCommitPatterns);
                LOG.debug("{}: Commit successful", cli);
            } catch (Exception e) {
                LOG.warn("{}: Commit failed", cli, e);
                throw new CommitFailedException(cli + ": Commit failed");
            }

            try {
                JunosCliInitializationStrategy.tryToExitConfigurationMode(cli, false);
                LOG.debug("{}: Exit configuration mode successful", cli);
            } catch (Exception e) {
                LOG.warn("{}: Unable to exit configuration mode", cli, e);
                throw new IllegalStateException(cli + ": Unable to exit configuration mode", e);
            }
        };
    }

    @Override
    public PostFailedHook getPostFailedHook(Context context) {
        return (causingException) -> {
            Cli cli = context.getTransport();
            String message = Optional.ofNullable(causingException)
                    .map(JunosCliInitializerUnit::getBottomErrorMessage)
                    .orElse("Unknown reason.");
            LOG.warn("{}: Configuration failed: {}", cli, message);

            try {
                JunosCliInitializationStrategy.tryToExitConfigurationMode(cli, true);

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
                Pattern.compile("(^|\\n)(?i)unknown command\\.(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)(?i)syntax error,(?-i) .*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)(?i)'.+' is ambiguous\\.(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)(?i)error:(?-i) .*", Pattern.DOTALL)
        ));
    }

    @Override
    public Set<Pattern> getCommitErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile("(^|\\n)(?i)error:(?-i) .*", Pattern.DOTALL)
            ));
    }

    @Override
    public PromptResolutionStrategy getPromptResolver() {
        return PROMPT_RESOLUTION_STRATEGY;
    }
}
