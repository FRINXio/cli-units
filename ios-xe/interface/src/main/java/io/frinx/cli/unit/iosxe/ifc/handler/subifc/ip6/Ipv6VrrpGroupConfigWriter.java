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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv6.vrrp.group.config.TrackedObjects;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6VrrpGroupConfigWriter implements CliWriter<Config> {

    private static final String PRIMARY_ADDRESS_COMMAND = "address %s primary\n";
    private static final String ADDRESS_COMMAND = "address %s\n";
    private static final String TRACK_SHUTDOWN_COMMAND = "track %s shutdown\n";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            interface {$interface}
            no vrrp {$vrrp} address-family ipv6
            end
            """;

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            configure terminal
            interface {$interface}
            vrrp {$vrrp} address-family ipv6
            {% if ($track) %}{$track}{% endif %}{% if ($addressPrimary) %}{$addressPrimary}{% endif %}{% if ($address) %}{$address}{% endif %}end
            """;

    private final Cli cli;

    public Ipv6VrrpGroupConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        this.updateCurrentAttributes(id, null, config, writeContext);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String interfaceName = id.firstKeyOf(Interface.class).getName();
        final Short vrrpGroupName = id.firstKeyOf(VrrpGroup.class).getVirtualRouterId();

        blockingWriteAndRead(getWriteTemplate(dataBefore, dataAfter, interfaceName, vrrpGroupName), cli, id, dataAfter);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final String interfaceName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final Short vrrpGroupName = instanceIdentifier.firstKeyOf(VrrpGroup.class).getVirtualRouterId();

        blockingWriteAndRead(getDeleteTemplate(interfaceName, vrrpGroupName), cli, instanceIdentifier, config);
    }

    @VisibleForTesting
    String getDeleteTemplate(String interfaceName, Short vrrpGroupName) {
        return fT(DELETE_TEMPLATE,
                "interface", interfaceName,
                "vrrp", vrrpGroupName);
    }

    @VisibleForTesting
    String getWriteTemplate(Config dataBefore, Config dataAfter, String interfaceName, Short vrrpGroupName) {
        return fT(WRITE_TEMPLATE,
                "interface", interfaceName,
                "vrrp", vrrpGroupName,
                "track", getTrack(dataBefore, dataAfter),
                "addressPrimary", getAddressPrimary(dataBefore, dataAfter),
                "address", getAddress(dataBefore, dataAfter));
    }

    private String getAddress(Config dataBefore, Config dataAfter) {
        List<Ipv6Prefix> addressesOld = getAddresses(dataBefore);
        List<Ipv6Prefix> addresses = getAddresses(dataAfter);
        StringBuilder command = new StringBuilder();
        command.append(getOldAddress(addressesOld, addresses));
        command.append(getNewAddress(addresses, ADDRESS_COMMAND));
        return command.toString();
    }

    private List<Ipv6Prefix> getAddresses(Config dataBefore) {
        if (dataBefore != null && dataBefore.getAugmentation(Config2.class) != null
                && dataBefore.getAugmentation(Config2.class).getAddresses() != null) {
            return dataBefore.getAugmentation(Config2.class).getAddresses();
        }
        return Collections.<Ipv6Prefix>emptyList();
    }

    private String getOldAddress(List<Ipv6Prefix> addressesOld, List<Ipv6Prefix> addresses) {
        List<Ipv6Prefix> diff = new LinkedList<>(addressesOld);
        if (addresses != null) {
            diff.removeAll(addresses);
        }
        return getNewAddress(diff, "no " + ADDRESS_COMMAND);
    }

    private String getNewAddress(List<Ipv6Prefix> addresses, String template) {
        if (addresses == null) {
            return "";
        }

        StringBuilder command = new StringBuilder();
        for (Ipv6Prefix address : addresses) {
            command.append(f(template, address.getValue()));
        }
        return command.toString();
    }

    private Object getAddressPrimary(Config dataBefore, Config dataAfter) {
        List<IpAddress> virtualAddress = dataAfter.getVirtualAddress();
        if (virtualAddress == null) {
            if (dataBefore != null && dataBefore.getVirtualAddress() != null) {
                return f("no " + PRIMARY_ADDRESS_COMMAND, dataBefore.getVirtualAddress().get(0).getIpv6Address()
                        .getValue());
            }
            return "";
        }

        Preconditions.checkState(virtualAddress.size() < 2, "Only one primary virtual address is allowed!");
        return f(PRIMARY_ADDRESS_COMMAND, virtualAddress.get(0).getIpv6Address().getValue());
    }

    private Object getTrack(Config dataBefore, Config dataAfter) {
        List<TrackedObjects> trackedObjectsOld = getTrackedObjects(dataBefore);
        List<TrackedObjects> trackedObjects = getTrackedObjects(dataAfter);

        StringBuilder command = new StringBuilder();
        command.append(getOldTrackedCommand(trackedObjectsOld, trackedObjects));
        command.append(getTrackedCommand(trackedObjects, ""));
        return command.toString();
    }

    private Object getOldTrackedCommand(List<TrackedObjects> trackedObjectsOld, List<TrackedObjects> trackedObjects) {
        List<TrackedObjects> diff = new LinkedList<>(trackedObjectsOld);
        diff.removeAll(trackedObjects);
        return getTrackedCommand(diff, "no ");
    }

    private Object getTrackedCommand(List<TrackedObjects> trackedObjects, String prefix) {
        StringBuilder command = new StringBuilder();
        for (TrackedObjects trackedObject : trackedObjects) {
            if (trackedObject.isShutdown() != null) {
                if (prefix.isEmpty()) {
                    if (trackedObject.isShutdown()) {
                        command.append(f(TRACK_SHUTDOWN_COMMAND, trackedObject.getTrackedObjectId()));
                    } else {
                        command.append(f("no " + TRACK_SHUTDOWN_COMMAND, trackedObject.getTrackedObjectId()));
                    }
                } else {
                    command.append(f(prefix + TRACK_SHUTDOWN_COMMAND, trackedObject.getTrackedObjectId()));
                }
            }
        }
        return command.toString();
    }

    private List<TrackedObjects> getTrackedObjects(Config dataBefore) {
        if (dataBefore != null && dataBefore.getAugmentation(Config2.class) != null) {
            return dataBefore.getAugmentation(Config2.class).getTrackedObjects();
        }
        return Collections.<TrackedObjects>emptyList();
    }
}