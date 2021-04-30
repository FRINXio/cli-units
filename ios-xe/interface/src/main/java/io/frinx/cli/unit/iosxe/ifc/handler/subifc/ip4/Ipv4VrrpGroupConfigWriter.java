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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv4.vrrp.group.config.TrackedObjects;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.VrrpGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4VrrpGroupConfigWriter implements CliWriter<Config> {

    private static final String SECONDARY_ADDRESS_COMMAND = "address %s secondary\n";
    private static final String PRIMARY_ADDRESS_COMMAND = "address %s primary\n";
    private static final String TRACK_COMMAND = "track %s decrement %s\n";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
        + "interface {$interface}\n"
        + "no vrrp {$vrrp} address-family ipv4\n"
        + "end\n";
    private static final String WRITE_TEMPLATE = "configure terminal\n"
        + "interface {$interface}\n"
        + "vrrp {$vrrp} address-family ipv4\n"
        + "{% if ($priority) %}priority {$priority}\n"
        + "{% else %}no priority\n{% endif %}"
        + "{% if ($preempt) %}preempt delay minimum {$preempt}\n"
        + "{% else %}no preempt delay\n{% endif %}"
        + "{% if ($track) %}{$track}{% endif %}"
        + "{% if ($addressPrimary) %}{$addressPrimary}{% endif %}"
        + "{% if ($addressSecondary) %}{$addressSecondary}{% endif %}"
        + "end\n";

    private final Cli cli;

    public Ipv4VrrpGroupConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        this.updateCurrentAttributes(id, null, config, writeContext);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String interfaceName = id.firstKeyOf(Interface.class).getName();
        final Short vrrpGroupName = id.firstKeyOf(VrrpGroup.class).getVirtualRouterId();

        blockingWriteAndRead(getWriteTemplate(dataBefore, dataAfter, interfaceName, vrrpGroupName), cli, id, dataAfter);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
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
            "priority", dataAfter.getPriority(),
            "preempt", dataAfter.getPreemptDelay(),
            "track", getTrack(dataBefore, dataAfter),
            "addressPrimary", getAddressPrimary(dataBefore, dataAfter),
            "addressSecondary", getAddressSecondary(dataBefore, dataAfter));
    }

    private String getAddressSecondary(Config dataBefore, Config dataAfter) {
        List<IpAddress> virtualAddressesOld = getVirtualSecondaryAddresses(dataBefore);
        List<IpAddress> virtualAddresses = getVirtualSecondaryAddresses(dataAfter);
        StringBuilder command = new StringBuilder();
        command.append(getOldAddressSecondary(virtualAddressesOld, virtualAddresses));
        command.append(getAddress(virtualAddresses, SECONDARY_ADDRESS_COMMAND));
        return command.toString();
    }

    private List<IpAddress> getVirtualSecondaryAddresses(Config dataBefore) {
        if (dataBefore != null && dataBefore.getAugmentation(Config1.class) != null) {
            return dataBefore.getAugmentation(Config1.class).getVirtualSecondaryAddresses();
        }
        return Collections.<IpAddress>emptyList();
    }

    private String getOldAddressSecondary(List<IpAddress> virtualAddressesOld, List<IpAddress> virtualAddresses) {
        List<IpAddress> diff = new LinkedList<>(virtualAddressesOld);
        if (virtualAddresses != null) {
            diff.removeAll(virtualAddresses);
        }
        return getAddress(diff, "no " + SECONDARY_ADDRESS_COMMAND);
    }

    private Object getAddressPrimary(Config dataBefore, Config dataAfter) {
        List<IpAddress> virtualAddress = dataAfter.getVirtualAddress();
        if (virtualAddress == null) {
            if (dataBefore != null && dataBefore.getVirtualAddress() != null) {
                return f("no " + PRIMARY_ADDRESS_COMMAND, dataBefore.getVirtualAddress().get(0).getIpv4Address()
                        .getValue());
            }
            return "";
        }

        Preconditions.checkState(virtualAddress.size() < 2, "Only one primary virtual address is allowed!");
        return f(PRIMARY_ADDRESS_COMMAND, virtualAddress.get(0).getIpv4Address().getValue());
    }

    private String getAddress(List<IpAddress> virtualAddresses, String template) {
        if (virtualAddresses == null) {
            return "";
        }

        StringBuilder command = new StringBuilder();
        for (IpAddress virtualAddress : virtualAddresses) {
            command.append(f(template, virtualAddress.getIpv4Address().getValue()));
        }
        return command.toString();
    }

    private Object getTrack(Config dataBefore, Config dataAfter) {
        List<TrackedObjects> trackedObjectsOld = getTrackedObjects(dataBefore);
        List<TrackedObjects> trackedObjects = getTrackedObjects(dataAfter);

        StringBuilder command = new StringBuilder();
        command.append(getOldTrackedCommand(trackedObjectsOld, trackedObjects));
        command.append(getTrackedCommand(trackedObjects, TRACK_COMMAND));
        return command.toString();
    }

    private Object getOldTrackedCommand(List<TrackedObjects> trackedObjectsOld, List<TrackedObjects> trackedObjects) {
        List<TrackedObjects> diff = new LinkedList<>(trackedObjectsOld);
        diff.removeAll(trackedObjects);
        return getTrackedCommand(diff, "no " + TRACK_COMMAND);
    }

    private Object getTrackedCommand(List<TrackedObjects> trackedObjects, String template) {
        StringBuilder command = new StringBuilder();
        for (TrackedObjects trackedObject : trackedObjects) {
            command.append(f(template, trackedObject.getTrackedObjectId(), trackedObject.getPriorityDecrement()));
        }
        return command.toString();
    }

    private List<TrackedObjects> getTrackedObjects(Config dataBefore) {
        if (dataBefore != null && dataBefore.getAugmentation(Config1.class) != null) {
            return dataBefore.getAugmentation(Config1.class).getTrackedObjects();
        }
        return Collections.<TrackedObjects>emptyList();
    }
}
