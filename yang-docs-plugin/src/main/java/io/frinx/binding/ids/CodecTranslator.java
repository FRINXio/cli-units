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

package io.frinx.binding.ids;

import java.net.URI;
import java.util.Set;
import javassist.ClassPool;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.opendaylight.controller.md.sal.binding.impl.BindingToNormalizedNodeCodec;
import org.opendaylight.mdsal.binding.generator.impl.ModuleInfoBackedContext;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.DataObjectSerializerGenerator;
import org.opendaylight.yangtools.binding.data.codec.gen.impl.StreamWriterGenerator;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingNormalizedNodeCodecRegistry;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;


/**
 * This class is responsible for translating InstanceIdentifier to the YangInstanceIdentifier.
 */
public class CodecTranslator {

    private static final JavassistUtils UTILS = JavassistUtils.forClassPool(ClassPool.getDefault());
    private final IdCodec codec;
    private final BindingToNormalizedNodeCodec bindingCodec;
    private final Log log = new SystemStreamLog();

    CodecTranslator(BindingToNormalizedNodeCodec bindingCodec, SchemaContext context) {

        this.bindingCodec = bindingCodec;
        this.codec = new IdCodec(context);

    }

    public static BindingToNormalizedNodeCodec getCodec(ModuleInfoBackedContext mibCtx, SchemaContext schemaContext) {
        BindingRuntimeContext ctx = BindingRuntimeContext.create(mibCtx, schemaContext);
        DataObjectSerializerGenerator gener = StreamWriterGenerator.create(UTILS);
        BindingNormalizedNodeCodecRegistry cr = new BindingNormalizedNodeCodecRegistry(gener);
        cr.onBindingRuntimeContextUpdated(ctx);
        BindingToNormalizedNodeCodec codec = new BindingToNormalizedNodeCodec(mibCtx, cr);
        codec.onGlobalContextUpdated(mibCtx.getSchemaContext());
        return codec;
    }

    public static ModuleInfoBackedContext getModuleInfoBackedContext(Set<YangModuleInfo> yangModuleInfos) {
        ModuleInfoBackedContext moduleInfoBackedContext = ModuleInfoBackedContext.create();
        moduleInfoBackedContext.addModuleInfos(yangModuleInfos);
        return moduleInfoBackedContext;
    }

    public String toStringId(YangInstanceIdentifier yangInstanceIdentifier) {
        return codec.serialize(yangInstanceIdentifier);
    }

    public YangInstanceIdentifier toBindingIndependent(InstanceIdentifier<?> instanceIdentifier) {

        try {
            return bindingCodec.toYangInstanceIdentifier(instanceIdentifier);
        } catch (IllegalStateException e) {
            log.warn("Could not find codec, won't generate documentation for this unit", e);
            return null;
        }
    }


    private static final class IdCodec extends AbstractModuleStringInstanceIdentifierCodec {

        private final SchemaContext context;
        private final DataSchemaContextTree contextTree;

        IdCodec(SchemaContext context) {
            this.context = context;
            contextTree = DataSchemaContextTree.from(this.context);
        }

        @Override
        protected Module moduleForPrefix(@Nonnull String prefix) {
            return context.getModules().stream()
                    .filter(m -> m.getPrefix().equals(prefix))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unable to find module for " + prefix));
        }

        @Nonnull
        @Override
        protected DataSchemaContextTree getDataContextTree() {
            return contextTree;
        }

        @Nullable
        @Override
        protected String prefixForNamespace(@Nonnull URI namespace) {
            return context.getModules().stream()
                    .filter(m -> m.getNamespace().equals(namespace))
                    .map(Module::getPrefix)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Unable to find prefix for " + namespace));
        }

    }
}
