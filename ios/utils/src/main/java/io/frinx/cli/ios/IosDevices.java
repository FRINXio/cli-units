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

package io.frinx.cli.ios;

import java.util.HashSet;
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

    public static final Set<Device> IOS_ALL = new HashSet<Device>() {{
            add(IOS_12);
            add(IOS_15);
        }
    };
}