/*
 * Copyright © 2020 Frinx and others.
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
package io.frinx.cli.cnative.iosxr5;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.frinx.cli.cnative.handlers.GenericCliNativeUnit;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.unit.iosxr.init.IosXrDevices;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.mdsal.dom.api.DOMSchemaService;
import org.opendaylight.yang.gen.v1.http.frinx.io.yang._native.xr5._interface.rev200312.interfaces.top.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.io.yang._native.xr5.acl.rev200310.acl4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.io.yang._native.xr5.acl.rev200310.acl6.top.Ipv6;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class NativeCliUnit extends GenericCliNativeUnit {

    /**
     * Creation of new ios-xr-5 native-cli unit.
     *
     * @param registry         Translation units registry.
     * @param domSchemaService Global schema context provider.
     * @param mappingCodec     Codec used for translation between BI and BA entities.
     */
    public NativeCliUnit(final TranslationUnitCollector registry, final DOMSchemaService domSchemaService,
                         final BindingNormalizedNodeSerializer mappingCodec) {
        super(registry, domSchemaService, mappingCodec);
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return ImmutableSet.of(org.opendaylight.yang.gen.v1.http.frinx.io.yang._native.xr5.acl.rev200310
                        .$YangModuleInfoImpl.getInstance(),
                org.opendaylight.yang.gen.v1.http.frinx.io.yang._native.xr5._interface.rev200312
                        .$YangModuleInfoImpl.getInstance());
    }

    @Override
    protected List<Class<? extends DataObject>> getRootInterfaces() {
        return ImmutableList.of(Ipv4.class, Ipv6.class, Interface.class);
    }

    @Override
    protected Set<Device> getSupportedVersions() {
        return Collections.singleton(IosXrDevices.IOS_5);
    }

    @Override
    protected String getUnitName() {
        return "IOR XR 5.* native-cli unit";
    }
}