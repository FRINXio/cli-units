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

package io.frinx.cli.unit.dasan.init;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareReadRegistryBuilder;
import io.fd.honeycomb.translate.spi.builder.CustomizerAwareWriteRegistryBuilder;
import io.frinx.cli.io.PromptResolutionStrategy;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.PrivilegedModeCredentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.privileged.mode.credentials.IosEnablePassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translate unit that does not actually translate anything.<br>
 * This translate unit's only responsibility is to properly initialize Dasan NOS cli
 * session. That is, upon establishing connection to NOS device, enter privileged
 * EXEC mode by issuing the 'enable' command and filling in the secret.
 */
public class NosCliInitializerUnit  implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(NosCliInitializerUnit.class);

    private static final Device NOS = new DeviceIdBuilder()
            .setDeviceType("nos")
            .setDeviceVersion("*")
            .build();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration nosReg;


    public NosCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        nosReg = registry.registerTranslateUnit(NOS, this);
    }

    public void close() {
        if (nosReg != null) {
            nosReg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return new NosCliInitializer(cliNodeConfiguration, id);
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final TranslateUnit.Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final CustomizerAwareReadRegistryBuilder readRegistry,
                                @Nonnull final CustomizerAwareWriteRegistryBuilder writeRegistry,
                                @Nonnull final TranslateUnit.Context context) {
        readRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
        writeRegistry.addCheckRegistry(ChecksMap.OPENCONFIG_REGISTRY);
    }

    @Override
    public String toString() {
        return "NOS cli init (FRINX) translate unit";
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
                Pattern.compile("(^|\\n)\\s+\\^.*", Pattern.DOTALL),
                Pattern.compile("% (?i)Invalid input detected at (?-i).*", Pattern.DOTALL),
                Pattern.compile("% (?i)Incomplete command\\.(?-i).*", Pattern.DOTALL),
                Pattern.compile("% (?i)Ambiguous command:(?-i).*", Pattern.DOTALL),
                Pattern.compile("%(?i)Interface is still up(?-i).*", Pattern.DOTALL),
                Pattern.compile("%(?i)Invalid(?-i).*", Pattern.DOTALL)
       ));
    }

    /**
     * Initialize NOS CLI session to be usable by various CRUD and RPC handlers.
     */
    public static final class NosCliInitializer implements SessionInitializationStrategy {
        private static final String PASSWORD_PROMPT = "Password:";
        private static final String PRIVILEGED_PROMPT_SUFFIX = "#";
        private static final String ENABLE_COMMAND = "enable";
        private static final String SET_TERMINAL_LENGTH_COMMAND = "terminal length 0";
        private static final int WRITE_TIMEOUT_SECONDS = 10;
        private static final int READ_TIMEOUT_SECONDS = 1;

        private final CliNode context;
        private final RemoteDeviceId id;

        public NosCliInitializer(CliNode context, RemoteDeviceId id) {
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

                // If already in privileged mode, don't do anything else
                if (PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim()
                        .endsWith(PRIVILEGED_PROMPT_SUFFIX)) {
                    LOG.info("{}: NOS cli session initialized successfully", id);
                    return;
                }

                // Enable privileged mode
                tryToEnterPrivilegedMode(session, newline);

                // Check if we are actually in privileged mode
                String prompt = PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim();

                // If not, fail
                Preconditions.checkState(prompt.endsWith(PRIVILEGED_PROMPT_SUFFIX),
                        "%s: NOS cli session initialization failed to enter privileged mode. Current prompt: %s",
                        id, prompt);

                LOG.info("{}: NOS cli session initialized successfully", id);
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

        private String getEnablePasswordFromCliNode() {

            PrivilegedModeCredentials privilegedModeCredentials = context.getPrivilegedModeCredentials();
            if (privilegedModeCredentials != null) {
                if (IosEnablePassword.class.isAssignableFrom(privilegedModeCredentials.getClass())) {
                    return ((IosEnablePassword) privilegedModeCredentials).getSecret();
                }
            }

            LOG.debug("{}: Secret not set, using session password as enable password", id);

            Preconditions.checkArgument(context.getCredentials() instanceof LoginPassword,
                    "%s: Unable to handle credentials type of: %s",
                    id, context.getCredentials());

            return ((LoginPassword) context.getCredentials()).getPassword();
        }
    }
}
