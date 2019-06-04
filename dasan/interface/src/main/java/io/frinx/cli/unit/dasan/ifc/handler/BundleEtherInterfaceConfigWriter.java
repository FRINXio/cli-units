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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleEtherInterfaceConfigWriter implements CliWriter<Config>, CompositeWriter.Child<Config> {

    private Cli cli;

    @SuppressWarnings("serial")
    private static final Set<Class<? extends InterfaceType>> SUPPORTED_INTERFACE_TYPES = Collections
            .unmodifiableSet(new HashSet<Class<? extends InterfaceType>>() {
                {
                    add(Ieee8023adLag.class);
                }
            });

    public BundleEtherInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifName);
        if (!matcher.matches()) {
            return false;
        }

        checkSupportedInterface(data,
                "Cannot create interface of type: " + data.getType());
        validateIfcNameAgainstType(data);
        validateIfcConfiguration(data);
        writeOrUpdateInterface(id, data);
        return true;
    }

    private static void checkSupportedInterface(Config config, String exceptionMsg) {
        if (!SUPPORTED_INTERFACE_TYPES.contains(config.getType())) {
            throw new IllegalArgumentException(exceptionMsg);
        }
    }

    @VisibleForTesting
    public void writeOrUpdateInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.CreateFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifcName);

        if (matcher.matches()) {
            blockingWriteAndRead(cli, id, data, "configure terminal", "bridge",
                    f("lacp aggregator %s", matcher.group("number")), "end");
        }
    }

    @VisibleForTesting
    static void validateIfcConfiguration(Config data) {
        if (data.getType() == Ieee8023adLag.class) {
            Preconditions.checkArgument(BooleanUtils.isNotFalse(data.isEnabled()),
                    "Cannot be disabled for interface %s of type  Bundle-Ether(Ieee8023adLag).", data.getName());
        }
    }

    private static void validateIfcNameAgainstType(Config data) {

        if (data.getType() == Ieee8023adLag.class) {
            Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(data.getName());
            Preconditions.checkArgument(matcher.matches(),
                    "Lacp interface name must be in format: Bundle-Ether8, not: %s", data.getName());
        } else {
            throw new IllegalArgumentException("Cannot create interface of type: " + data.getType());
        }
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifName);
        if (!matcher.matches()) {
            return false;
        }

        try {
            Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                    "Changing interface type is not permitted. Before: %s, After: %s", dataBefore.getType(),
                    dataAfter.getType());
        } catch (IllegalArgumentException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }

        checkSupportedInterface(dataAfter, "Unknown interface type: " + dataAfter.getType());
        validateIfcConfiguration(dataAfter);
        writeOrUpdateInterface(id, dataAfter);
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifName);
        if (!matcher.matches()) {
            return false;
        }

        checkSupportedInterface(dataBefore, "Unexpected interface type: " + dataBefore.getType());
        deleteInterface(id, dataBefore);
        return true;
    }

    @VisibleForTesting
    public void deleteInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.DeleteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = BundleEtherInterfaceReader.BUNDLE_ETHER_IF_NAME_PATTERN.matcher(ifcName);

        if (matcher.matches()) {
            blockingDeleteAndRead(cli, id, "configure terminal",
                    "bridge", f("no lacp aggregator %s", matcher.group("number")),
                    "end");
        }
    }
}
