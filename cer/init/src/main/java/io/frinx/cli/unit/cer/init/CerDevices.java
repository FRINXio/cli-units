/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.init;

import java.util.Set;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;

public final class CerDevices {

    private CerDevices() {

    }

    public static final Device CER_1 = new DeviceIdBuilder()
            .setDeviceType("cer")
            .setDeviceVersion("10.*")
            .build();

    public static final Device CER_2 = new DeviceIdBuilder()
            .setDeviceType("cer")
            .setDeviceVersion("11.*")
            .build();

    public static final Device CER_3 = new DeviceIdBuilder()
            .setDeviceType("cer")
            .setDeviceVersion("12.*")
            .build();

    public static final Device CER_4 = new DeviceIdBuilder()
            .setDeviceType("cer")
            .setDeviceVersion("13.*")
            .build();

    public static final Device CER_GENERIC = new DeviceIdBuilder()
            .setDeviceType("cer")
            .setDeviceVersion("*")
            .build();

    public static final Set<Device> CER_ALL = Set.of(CER_1, CER_2, CER_3, CER_4);
}
