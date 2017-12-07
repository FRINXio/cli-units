/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.init;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Session;
import io.frinx.cli.io.SessionException;
import io.frinx.cli.io.SessionInitializationStrategy;
import io.frinx.cli.io.impl.cli.PromptResolutionStrategy;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.topology.RemoteDeviceId;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.PrivilegedModeCredentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.privileged.mode.credentials.IosEnablePassword;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.cli.node.credentials.credentials.LoginPassword;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Translate unit that does not actually translate anything.
 *
 * This translate unit's only responsibility is to properly initialize IOS cli
 * session. That is, upon establishing connection to IOS device, enter privileged
 * EXEC mode by issuing the 'enable' command and filling in the secret.
 */
// TODO Once the IosCliInitializer class is fixed and can work also in a setting
// where no secret is actually required (that is also the case, when we are in
// the privileged mode already), refactor this into abstract class implementing
// TranslateLogicProvider interface. Let other units extend this interface,
// instead of creating a standalone unit just for cli initialization purposes.
public class IosCliInitializerUnit  implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(IosCliInitializerUnit.class);

    // TODO This is reused all over the units. Move this to som Util class so
    // we can reuse it.
    // TODO XR devices does not actually have privileged exec mode, nor 'enable'
    // command. For them, it should be sufficient to just configure terminal to
    // length 0.
    private static final Device IOS_XR = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("*")
            .build();

    private static final Device IOS = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration iosXrReg;
    private TranslationUnitCollector.Registration iosReg;


    public IosCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init()
    {
        iosXrReg = registry.registerTranslateUnit(IOS_XR, this);
        iosReg = registry.registerTranslateUnit(IOS, this);
    }

    public void close() {
        if (iosXrReg != null) {
            iosXrReg.close();
        }

        if (iosReg != null) {
            iosReg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Collections.emptySet();
    }

    @Override
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return new IosCliInitializer(cliNodeConfiguration, id);
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
    public String toString() {
        return "IOS cli init (FRINX) translate unit";
    }

    /**
     * Initialize IOS CLI session to be usable by various CRUD and RPC handlers
     */
    private static final class IosCliInitializer implements SessionInitializationStrategy {
        private static final String PASSWORD_PROMPT = "Password:";
        private static final String PRIVILEGED_PROMPT_SUFFIX = "#";
        private static final String ENABLE_COMMAND = "enable";
        private static final String SET_TERMINAL_LENGTH_COMMAND = "terminal length 0";
        private static final String SET_TERMINAL_WIDTH_COMMAND = "terminal width 0";
        private static final int WRITE_TIMEOUT_SECONDS = 10;
        private static final int READ_TIMEOUT_SECONDS = 1;

        private final CliNode context;
        private final RemoteDeviceId id;

        IosCliInitializer(CliNode context, RemoteDeviceId id) {
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
                    LOG.info("{}: IOS cli session already initialized", id);
                    return;
                }

                // Enable privileged mode
                tryToEnterPrivilegedMode(session, newline);

                // Check if we are actually in privileged mode
                String prompt = PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim();

                if (!prompt.endsWith(PRIVILEGED_PROMPT_SUFFIX)) {
                    LOG.warn("{}: IOS cli session initialization failed to enter privileged mode. Current prompt {}",
                            id, prompt);
                } else {
                    LOG.info("{}: IOS cli session initialized successfully", id);
                }

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
}

