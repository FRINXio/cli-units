/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.init;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.fd.honeycomb.translate.spi.write.CommitFailedException;
import io.fd.honeycomb.translate.spi.write.PostCommitHook;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.CliFlavour;
import io.frinx.cli.io.Command;
import io.frinx.cli.io.PromptResolutionStrategy;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
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
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaosCliInitializerUnit extends AbstractUnit {

    private static final Logger LOG = LoggerFactory.getLogger(SaosCliInitializerUnit.class);

    private static final int READ_TIMEOUT_SECONDS = 1;

    private static final String PRIVILEGED_PROMPT_SUFFIX = ">";
    private static final String SET_TERMINAL_PAGER_OFF_COMMAND = "system shell session set more off";
    private static final String COMMIT = "configuration save";
    private static final Pattern CONFIG_PROMPT_SUFFIX_PATTERN = Pattern.compile("^.*\\(configuration\\)>.*$");

    private static final Predicate<String> IS_CONFIGURATION_PROMPT =
        s -> CONFIG_PROMPT_SUFFIX_PATTERN.matcher(s).matches();
    private static final Predicate<String> IS_PRIVELEGE_PROMPT =
        s -> s.endsWith(PRIVILEGED_PROMPT_SUFFIX) && !IS_CONFIGURATION_PROMPT.test(s);

    private static SaosCliInitializationStrategy INITIALIZER = new SaosCliInitializationStrategy();

    public SaosCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(SaosDevices.SAOS_GENERIC);
    }

    @Override
    protected String getUnitName() {
        return "SAOS cli init (FRINX) translate unit";
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
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile("(^|\\n)\\s+\\^.*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)(?i)ERROR:(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)(?i)SHELL PARSER FAILURE: (incomplete command:)?(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)(?i)SHELL AUTOCOMPLETION FAILURE: (?-i).*", Pattern.DOTALL)
        ));
    }

    @Override
    public PromptResolutionStrategy getPromptResolver() {
        return PromptResolutionStrategy.ENTER_AND_READ;
    }

    public CliFlavour getCliFlavour() {
        return new CliFlavour(
                Pattern.compile("configuration (search string|show brief)"),
                "",
                "\"",
                "\"",
                null,
                null,
                null,
                ImmutableList.of(),
                "",
                Cli.NEWLINE,
                "",
                Optional.of("configuration show brief"),
                Stream.of('^', '*').collect(Collectors.toSet()));
    }

    public static class SaosCliInitializationStrategy implements SessionInitializationStrategy {

        @Override
        public void accept(Session session, String newline) {
            try {
                LOG.debug("{}: Disable pager to prevent \"[more x%]\" situation", session);
                write(session, newline, SET_TERMINAL_PAGER_OFF_COMMAND);
                write(session, newline, COMMIT);
                LOG.info("{}: SAOS cli session initialized successfully", session);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (SessionException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to initialize device", session, e);
                throw new IllegalStateException(session + ": Unable to initialize device", e);
            }
        }
    }

    @Override
    @SuppressWarnings({"IllegalCatch", "AvoidHidingCauseException"})
    public PostCommitHook getCommitHook(Context context, Set<Pattern> errorCommitPatterns) {
        return () -> {
            Cli cli = null;
            try {
                cli = context.getTransport();
                tryToCommitAndSave(cli, errorCommitPatterns);
                LOG.debug("{}: Commit successful", cli);
            } catch (Exception e) {
                LOG.warn("{}: Commit failed", cli, e);
                throw new CommitFailedException(cli + ": Commit failed");
            }
        };
    }

    private static void tryToCommitAndSave(Cli cli, Set<Pattern> errorCommitPatterns)
            throws InterruptedException, ExecutionException {

        LOG.debug("{}: Executing commit command.", cli);
        Command commit = Command.writeCommandCustomChecks(COMMIT, errorCommitPatterns);
        cli.executeAndSwitchPrompt(commit, IS_PRIVELEGE_PROMPT)
                .toCompletableFuture()
                .get();
    }
}
