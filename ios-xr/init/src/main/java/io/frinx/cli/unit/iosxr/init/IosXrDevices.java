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

package io.frinx.cli.unit.iosxr.init;

import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;

public final class IosXrDevices {

    private IosXrDevices() {

    }

    public static final Device IOS_XR_GENERIC = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("*")
            .build();

    public static final Device IOS_4 = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("4.*")
            .build();

    public static final Device IOS_5 = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("5.*")
            .build();

    public static final Device IOS_6 = new DeviceIdBuilder()
            .setDeviceType("ios xr")
            .setDeviceVersion("6.*")
            .build();

    public static final Set<Device> IOS_XR_ALL = Set.of(
        IOS_4,
        IOS_5,
        IOS_6
    );
}
