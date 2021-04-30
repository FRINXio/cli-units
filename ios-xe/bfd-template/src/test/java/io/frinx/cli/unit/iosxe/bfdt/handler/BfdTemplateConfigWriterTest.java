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

package io.frinx.cli.unit.iosxe.bfdt.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.BfdTempAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.BfdTempAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.BfdTemplateConfig.Type;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.BfdTemplates;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.BfdTemplatesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.BfdTemplateKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.IntervalBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class BfdTemplateConfigWriterTest {

    private static final List<BfdTemplate> WRITE_BFD_TEMPLATES =
            Arrays.asList(
                    new BfdTemplateBuilder()
                        .setName("test")
                        .setKey(new BfdTemplateKey("test"))
                        .setConfig(new ConfigBuilder()
                            .setName("test")
                            .setType(Type.SingleHop)
                            .build())
                        .setInterval(new IntervalBuilder()
                            .setMinTx("7")
                            .setMinRx("5")
                            .setMultiplier("3")
                            .build())
                        .build(),
                    new BfdTemplateBuilder()
                        .setName("aaa")
                        .setConfig(new ConfigBuilder()
                            .setName("aaa")
                            .setType(Type.MultiHop)
                            .build())
                        .setInterval(new IntervalBuilder()
                            .setMinTx("5")
                            .setMinRx("6")
                            .setMultiplier("3")
                            .build())
                        .build()
            );

    private static final List<BfdTemplate> UPDATE_BFD_TEMPLATE =
            Collections.singletonList(
                    new BfdTemplateBuilder()
                            .setName("test")
                            .setKey(new BfdTemplateKey("test"))
                            .setConfig(new ConfigBuilder()
                                    .setName("test")
                                    .setType(Type.MultiHop)
                                    .build())
                            .setInterval(new IntervalBuilder()
                                    .setMinTx("70")
                                    .setMinRx("500")
                                    .setMultiplier("3")
                                    .build())
                            .build()
            );


    private static final String WRITE_INPUT = "configure terminal\n"
            + "bfd-template single-hop test\n"
            + "interval min-tx 7 min-rx 5 multiplier 3\n"
            + "exit\n"
            + "bfd-template multi-hop aaa\n"
            + "interval min-tx 5 min-rx 6 multiplier 3\n"
            + "exit\n"
            + "end\n";

    private static final String UPDATE_INPUT = "configure terminal\n"
            + "no bfd-template single-hop test\n"
            + "bfd-template multi-hop test\n"
            + "interval min-tx 70 min-rx 500 multiplier 3\n"
            + "exit\n"
            + "end\n";

    private static final String DELETE_INPUT = "configure terminal\n"
            + "no bfd-template multi-hop test\n"
            + "end\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private BfdTemplateConfigWriter writer;
    private final ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);
    private final InstanceIdentifier iid = KeyedInstanceIdentifier.create(BfdTemplates.class)
            .child(BfdTemplate.class, new BfdTemplateKey("test"));

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new BfdTemplateConfigWriter(cli);
    }

    @Test
    public void write() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, getAug(WRITE_BFD_TEMPLATES), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void update() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, getAug(WRITE_BFD_TEMPLATES), getAug(UPDATE_BFD_TEMPLATE),
                context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    public void delete() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, getAug(UPDATE_BFD_TEMPLATE), context);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    private BfdTempAug getAug(final List<BfdTemplate> bfdTemplates) {
        return new BfdTempAugBuilder()
                .setBfdTemplates(new BfdTemplatesBuilder()
                        .setBfdTemplate(bfdTemplates)
                        .build())
                .build();
    }

}
