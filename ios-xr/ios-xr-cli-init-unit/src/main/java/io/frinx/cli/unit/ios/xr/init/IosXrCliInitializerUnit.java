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
import io.frinx.cli.io.SessionInitializationStrategy;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * Translate unit that does not actually translate anything.
 *
 * This translate unit's only responsibility is to properly initialize IOS-XR cli
 * session. That is, upon establishing connection to IOS-XR device, enter Privileged
 * EXEC mode by issuing the 'enable' command.
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
    private IosXrCliInitializer initializer;
    private RemoteDeviceId deviceId;

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
        this.deviceId = id;
        this.initializer = new IosXrCliInitializer(cliNodeConfiguration, id);
        return initializer;
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull final Context context) {
        // NO-OP
    }

    @Override
    public PreCommitHook getPreCommitHook(Context context) {
        return () -> {
            try {
                Cli cli = context.getTransport();
                initializer.tryToEnterConfigurationMode(cli);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOG.warn("{}: Unable to enter configuration mode", deviceId, e);
                throw new IllegalStateException(deviceId + ": Unable to enter configuration mode", e);
            }
        };
    }

    @Override
    public PostCommitHook getCommitHook(Context context) {
        return () -> {
            String CONFIG_FAILED_PREFIX = "% Failed";
            try {
                Cli cli = context.getTransport();
                String s = cli.executeAndRead("commit").toCompletableFuture().get();
                if (s.contains(CONFIG_FAILED_PREFIX)) {
                    LOG.warn("Commit failed. Reason: {}", s);
                    throw new CommitFailedException(s);
                } else {
                    LOG.info("Commit successful");
                    try {
                        initializer.tryToExitConfigurationMode(cli);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        LOG.warn("{}: Unable to exit configuration mode", deviceId, e);
                        throw new IllegalStateException(deviceId + ": Unable to exit configuration mode", e);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Sending commit failed. Reason: {}", e.getMessage(), e);
            }
        };
    }

    @Override
    public PostFailedHook getPostFailedHook(Context context)  {
        return () -> {
            Cli cli = context.getTransport();
            try {
                String s = cli.executeAndRead("show configuration failed inheritance").toCompletableFuture().get();
                LOG.warn("Configuration failed: {}", s);

                // if this execution fails, the return to config mode was unsuccessful
                // the check if we are again in config mode is done automatically, so if no exception
                // is thrown, consider this as a success
                cli.executeAndSwitchPrompt("abort", IosXrCliInitializer.IS_PRIVELEGE_PROMPT)
                        .toCompletableFuture().get();
                LOG.info("Reverting configuration on device successful.");
                throw new WriterRegistry.Reverter.RevertSuccessException(s);
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Failed to abort commit. Reason: {}", e.getMessage(), e);
                throw new WriterRegistry.Reverter.RevertFailedException(e);
            }
        };
    }

    @Override
    public Set<Pattern> getErrorPatterns() {
        return Sets.newLinkedHashSet(Arrays.asList(
            Pattern.compile("\\s*\\^.*", Pattern.DOTALL),
            Pattern.compile("\\% (?i)invalid input(?-i).*", Pattern.DOTALL),
            Pattern.compile("\\% (?i)Incomplete command(?-i).*", Pattern.DOTALL)
        ));
    }

    @Override
    public String toString() {
        return "IOS XR cli init (FRINX) translate unit";
    }

}

