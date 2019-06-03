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

package io.frinx.cli.unit.dasan.ifc.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherInterfaceConfigReader
        implements CliConfigReader<Config, ConfigBuilder>, CompositeReader.Child<Config, ConfigBuilder> {

    private Cli cli;

    public BundleEtherInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
            @Nonnull final ConfigBuilder builder, @Nonnull final ReadContext ctx) throws ReadFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();

        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifName);
        if (!matcher.matches()) {
            return;
        }

        builder.setName(BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PREFIX + matcher.group("number"));
        builder.setType(Ieee8023adLag.class);
        builder.setEnabled(Boolean.TRUE);
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
