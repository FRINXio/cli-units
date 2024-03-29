/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ios.init;

import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;

public final class IosDevices {

    private IosDevices() {

    }

    public static final Device IOS_12 = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("12.*")
            .build();

    public static final Device IOS_GENERIC = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("*")
            .build();

    public static final Device IOS_15 = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("15.*")
            .build();

    public static final Device IOS_16 = new DeviceIdBuilder()
            .setDeviceType("ios")
            .setDeviceVersion("16.*")
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

    public static final Set<Device> IOS_ALL = Set.of(
        IOS_12,
        IOS_15,
        IOS_16,
        IOS_XE_15,
        IOS_XE_16,
        IOS_XE_17
    );

    public static final Set<Device> IOS_ONLY = Set.of(
        IOS_12,
        IOS_15,
        IOS_16
    );
}
