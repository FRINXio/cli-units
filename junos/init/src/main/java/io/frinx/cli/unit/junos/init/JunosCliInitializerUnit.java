/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.junos.init;

import com.google.common.base.Preconditions;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.topology.rev170520.CliNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class JunosCliInitializerUnit implements TranslateUnit {

    private static final Logger LOG = LoggerFactory.getLogger(JunosCliInitializerUnit.class);

    private static final Device JUNOS = new DeviceIdBuilder()
            .setDeviceType("junos")
            .setDeviceVersion("*")
            .build();

    private TranslationUnitCollector registry;
    private TranslationUnitCollector.Registration reg;

    public JunosCliInitializerUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init()
    {
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
    public SessionInitializationStrategy getInitializer(@Nonnull final RemoteDeviceId id,
                                                        @Nonnull final CliNode cliNodeConfiguration) {
        return new SessionInitializationStrategy() {

            private static final String PRIVILEGED_PROMPT_SUFFIX = ">";
            private static final String CLI_COMMAND = "cli";
            private static final String DISABLE_COMPLETE_ON_SPACE = "set cli complete-on-space off";
            private static final String SET_TERMINAL_LENGTH_COMMAND = "set cli screen-length 0";
            private static final String SET_TERMINAL_WIDTH_COMMAND = "set cli screen-width 0";
            private static final int READ_TIMEOUT_SECONDS = 1;

            @Override
            public void accept(Session session, String newline) {
                try {
                    // enable cli mode
                    tryToEnterCliMode(session, newline);

                    // Check if we are actually in cli mode
                    String prompt = PromptResolutionStrategy.ENTER_AND_READ.resolvePrompt(session, newline).trim();
                    // If not, fail
                    Preconditions.checkState(prompt.endsWith(PRIVILEGED_PROMPT_SUFFIX),
                            "%s: Junos cli session initialization failed to enter cli mode. Current prompt: %s", id, prompt);

                    LOG.debug("{}: Setting terminal complete command on space to off", id);
                    write(session, newline, DISABLE_COMPLETE_ON_SPACE);
                    session.readUntilTimeout(READ_TIMEOUT_SECONDS);

                    LOG.debug("{}: Setting terminal length to 0 to prevent \"--More--\" situation", id);
                    write(session, newline, SET_TERMINAL_LENGTH_COMMAND);
                    session.readUntilTimeout(READ_TIMEOUT_SECONDS);

                    LOG.debug("{}: Setting terminal width to 0", id);
                    write(session, newline, SET_TERMINAL_WIDTH_COMMAND);
                    session.readUntilTimeout(READ_TIMEOUT_SECONDS);

                    LOG.info("{}: Junos cli session initialized successfully", id);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                } catch (SessionException | ExecutionException | TimeoutException e) {
                    LOG.warn("{}: Unable to initialize device", id, e);
                    throw new IllegalStateException(id + ": Unable to initialize device", e);
                }

            }

            private void tryToEnterCliMode(@Nonnull Session session, @Nonnull String newline)
                    throws InterruptedException, ExecutionException, TimeoutException {

                write(session, newline, CLI_COMMAND);
                String cliCommandOutput = session.readUntilTimeout(READ_TIMEOUT_SECONDS).trim();
                LOG.debug("{}: Entering cli mode resulted in output: {}", id, cliCommandOutput);
            }
        };
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
        return "Junos cli init (FRINX) translate unit";
    }
}

