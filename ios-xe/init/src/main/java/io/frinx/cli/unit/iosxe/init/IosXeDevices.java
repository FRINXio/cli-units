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

package io.frinx.cli.unit.iosxe.init;

import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;

public final class IosXeDevices {

    private IosXeDevices() {
    }

    public static final Device IOS_XE_GENERIC = new DeviceIdBuilder()
            .setDeviceType("ios xe")
            .setDeviceVersion("*")
            .build();

    public static final Device IOS_XE_15 = new DeviceIdBuilder()
            .setDeviceType("ios xe")
            .setDeviceVersion("15.*")
            .build();

    public static final Device IOS_XE_16 = new DeviceIdBuilder()
            .setDeviceType("ios xe")
            .setDeviceVersion("16.*")
            .build();

    public static final Device IOS_XE_17 = new DeviceIdBuilder()
            .setDeviceType("ios xe")
            .setDeviceVersion("17.*")
            .build();

    public static final Set<Device> IOS_XE_ALL = new HashSet<Device>() {
        {
            add(IOS_XE_15);
            add(IOS_XE_16);
            add(IOS_XE_17);
        }
    };
}