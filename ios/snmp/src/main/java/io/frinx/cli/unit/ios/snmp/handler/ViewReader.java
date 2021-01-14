/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.snmp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.View;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.ViewBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.ViewKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ViewReader implements CliConfigListReader<View, ViewKey, ViewBuilder> {

    public static final String SHOW_SNMP_VIEWS = "show running-config | include snmp-server view";
    public static final String SHOW_SNMP_VIEW = "show running-config | include snmp-server view %s";

    public static final Pattern VIEW_LINE =
            Pattern.compile("snmp-server view (?<name>\\S+) (?<mib>\\S+) (?<inclusion>included|excluded)");

    private final Cli cli;

    public ViewReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ViewKey> getAllIds(@Nonnull InstanceIdentifier<View> instanceIdentifier,
                                   @Nonnull ReadContext readContext) throws ReadFailedException {
        final String output = blockingRead(SHOW_SNMP_VIEWS, cli, instanceIdentifier, readContext);
        return new ArrayList<>(getViewKeys(output));
    }

    @VisibleForTesting
    public static List<ViewKey> getViewKeys(final String output) {
        return ParsingUtils.parseFields(output, 0, VIEW_LINE::matcher,
            matcher -> matcher.group("name"), ViewKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<View> instanceIdentifier,
                                      @Nonnull ViewBuilder builder,
                                      @Nonnull ReadContext readContext) {
        final String viewName = instanceIdentifier.firstKeyOf(View.class).getName();
        builder.setName(viewName);
    }

}