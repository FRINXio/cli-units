/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler;

import static io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.DampingBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.damping.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.damping.rev171024.damping.top.damping.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceDampingConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    // default dampening values
    public static final long DEFAULT_HALF_LIFE = 1L;
    public static final long DEFAULT_REUSE = 750L;
    public static final long DEFAULT_SUPRESS = 2000;
    public static final long DEFAULT_MAX_SUPRESS_TIME = 4L;

    private Cli cli;

    public InterfaceDampingConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        parseDamping(blockingRead(String.format(SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx), builder);
    }

    private static Pattern DAMPING_ENABLED = Pattern.compile("\\s*dampening.*");
    private static Pattern DAMPING_REUSE = Pattern.compile("\\s*dampening (?<halfLife>\\d+).*");
    private static Pattern FULL_DAMPING_CONFIG = Pattern.compile("\\s*dampening (?<halfLife>\\d+) (?<reuse>\\d+) " +
            "(?<suppress>\\d+) (?<maxSuppress>\\d+).*");

    @VisibleForTesting
    static void parseDamping(String output, ConfigBuilder builder) {

        // damping is not enabled, until proven otherwise
        builder.setEnabled(false);
        ParsingUtils.parseField(output, 0,
                DAMPING_ENABLED::matcher,
                matcher -> true,
                builder::setEnabled);

        // if not enabled, don't even try to set other values
        if (!builder.isEnabled()) {
            return;
        }

        // set default damping values, if the are configured
        // they will be overwritten later in this method
        setDefaultValues(builder);

        ParsingUtils.parseField(output, 0,
                DAMPING_REUSE::matcher,
                matcher -> Long.valueOf(matcher.group("halfLife")),
                builder::setHalfLife);

        ParsingUtils.parseField(output, 0,
                FULL_DAMPING_CONFIG::matcher,
                matcher -> Long.valueOf(matcher.group("reuse")),
                builder::setReuse);

        ParsingUtils.parseField(output, 0,
                FULL_DAMPING_CONFIG::matcher,
                matcher -> Long.valueOf(matcher.group("suppress")),
                builder::setSuppress);

        ParsingUtils.parseField(output, 0,
                FULL_DAMPING_CONFIG::matcher,
                matcher -> Long.valueOf(matcher.group("maxSuppress")),
                builder::setMaxSuppress);
    }

    private static void setDefaultValues(ConfigBuilder builder) {
        Preconditions.checkState(builder.isEnabled());

        builder.setHalfLife(DEFAULT_HALF_LIFE)
                .setMaxSuppress(DEFAULT_MAX_SUPRESS_TIME)
                .setReuse(DEFAULT_REUSE)
                .setSuppress(DEFAULT_SUPRESS);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((DampingBuilder) parentBuilder).setConfig(readValue);
    }
}
