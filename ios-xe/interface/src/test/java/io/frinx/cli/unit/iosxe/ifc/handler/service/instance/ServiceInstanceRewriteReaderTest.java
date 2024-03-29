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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceRewrite.Operation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.ServiceInstanceRewrite.Type;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Rewrite;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.RewriteBuilder;

public class ServiceInstanceRewriteReaderTest {

    public static final String OUTPUT = """
            service instance 200 ethernet EVC
              encapsulation untagged , dot1q 1-3,5-10
              rewrite ingress tag pop 1 symmetric
              bridge-domain 200 split-horizon group 3
            """;

    public static final String OUTPUT_2 = """
            service instance 200 ethernet EVC
              encapsulation untagged , dot1q 1-3,5-10
              rewrite ingress tag pop 2 symmetric
              bridge-domain 200 split-horizon group 3
            """;

    private static Rewrite rewriteExpected = new RewriteBuilder()
            .setOperation(Operation.Pop)
            .setType(Type.Ingress)
            .build();

    @Test
    void testRewriteRead() {
        final RewriteBuilder builder = new RewriteBuilder();
        ServiceInstanceRewriteReader.parseRewrite(OUTPUT, builder);
        assertEquals(rewriteExpected, builder.build());
    }

    @Test
    void testRewriteUnsupportedOptionRead() {
        final RewriteBuilder builder = new RewriteBuilder();
        ServiceInstanceRewriteReader.parseRewrite(OUTPUT_2, builder);
        assertEquals(new RewriteBuilder().build(), builder.build());
    }
}
