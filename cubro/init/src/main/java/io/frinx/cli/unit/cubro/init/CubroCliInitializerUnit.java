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

package io.frinx.cli.unit.cubro.init;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.CliFlavour;
import io.frinx.cli.io.OutputFunction;
import io.frinx.cli.io.PromptResolutionStrategy;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.cli.unit.utils.AbstractUnit;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CubroCliInitializerUnit extends AbstractUnit {

    private static final Logger LOG = LoggerFactory.getLogger(CubroCliInitializerUnit.class);

    public CubroCliInitializerUnit(@NotNull final TranslationUnitCollector registry) {
        super(registry);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(CubroDevices.CUBRO_GENERIC);
    }

    @Override
    protected String getUnitName() {
        return "CUBRO cli init (FRINX) translate unit";
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@NotNull final RemoteDeviceId id,
                                                        @NotNull final CliNode cliNodeConfiguration) {
        return new CubroCliInitializer(id);
    }

    @Override
    public void provideHandlers(@NotNull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @NotNull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @NotNull final TranslateUnit.Context context) {
        readRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
        writeRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile("(^|\\n)^\\s+\\^.*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% (?i)Unknown command.(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)% (?i)Command incomplete(?-i).*", Pattern.DOTALL),
                Pattern.compile("(^|\\n)Overlapping networks observed for(?-i).*")
        ));
    }

    @Override
    public CliFlavour getCliFlavour() {
        return new CliFlavour(Pattern.compile("^UNSUPPORTED"),
                " ", "",
                "!",
                Pattern.compile("\\|"),
                OutputFunction.ALL,
                "!",
                ImmutableList.of("end", "Building configuration", "Current configuration"),
                "",
                "\n",
                "",
                null,
                "show running-config");
    }

    /**
     * Initialize IOS CLI session to be usable by various CRUD and RPC handlers.
     */
    public static class CubroCliInitializer implements SessionInitializationStrategy {
        private static final String LINUX_PROMPT_SUFFIX = ":~#";
        private static final String VTYSH_COMMAND = "vtysh";
        private static final int READ_TIMEOUT_SECONDS = 1;

        private final RemoteDeviceId id;

        public CubroCliInitializer(RemoteDeviceId id) {
            this.id = id;
        }

        @Override
        public void accept(@NotNull Session session, @NotNull String newline) {
            try {
                final String initialPrompt =
                        PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim();

                LOG.debug("{}: {} cli session initialized output: {}", id, getOsNameForLogging(), initialPrompt);

                // to enter the integrated shell
                tryToEnterIntegratedShell(session, newline);

                // Check if we are actually in privileged mode
                String prompt =
                        PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim();

                // If not, fail
                Preconditions.checkState(!prompt.endsWith(LINUX_PROMPT_SUFFIX),
                        "%s: %s cli session initialization failed to enter integrated shell. Current prompt: %s",
                        id, getOsNameForLogging(), prompt);

                LOG.info("{}: {} cli session initialized successfully", id, getOsNameForLogging());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (SessionException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to initialize device", id, e);
                throw new IllegalStateException(id + ": Unable to initialize device", e);
            }
        }

        protected String getOsNameForLogging() {
            return "CUBRO";
        }

        private void tryToEnterIntegratedShell(@NotNull Session session, @NotNull String newline)
                throws InterruptedException, ExecutionException, TimeoutException {

            write(session, newline, VTYSH_COMMAND);
            String vtyshCommandOutput = session.readUntilTimeout(READ_TIMEOUT_SECONDS).trim();

            LOG.debug("{}: Entering integrated shell resulted in output: {}", id, vtyshCommandOutput);
        }
    }
}