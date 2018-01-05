/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import com.google.common.annotations.VisibleForTesting;

import java.util.regex.Pattern;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class GlobalStateReader implements BgpReader.BgpOperReader<State, StateBuilder> {

    private Cli cli;
    static final String SH_BGP= "sh bgp summ";
    static final Pattern CONFIG_LINE = Pattern.compile("BGP router identifier (?<id>.+), local AS number (?<as>.+)");

    public GlobalStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                             @Nonnull StateBuilder stateBuilder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        parseGlobal(blockingRead(SH_BGP, cli, instanceIdentifier, ctx), stateBuilder);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State config) {
        ((GlobalBuilder) builder).setState(config);
    }

    @VisibleForTesting
    public static void parseGlobal(String output, StateBuilder sBuilder) {
        ParsingUtils.parseField(output, 0,
                CONFIG_LINE::matcher,
                matcher -> matcher.group("id"),
                value -> sBuilder.setRouterId(new DottedQuad(value)));

        ParsingUtils.parseField(output, 0,
                CONFIG_LINE::matcher,
                matcher -> matcher.group("as"),
                value -> sBuilder.setAs(new AsNumber(Long.valueOf(value))));
    }
}
