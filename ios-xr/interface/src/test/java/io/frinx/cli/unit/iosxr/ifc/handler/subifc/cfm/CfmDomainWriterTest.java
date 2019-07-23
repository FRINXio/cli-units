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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.oam.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.DomainBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.DomainKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.domain.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.domains.domain.MepBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmDomainWriterTest {
    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;
    private CfmDomainWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private static final String WRITE_INPUT = "interface Bundle-Ether1000.100\n"
        + "ethernet cfm\n"
        + "mep domain DML1 service MA-001 mep-id 10\n"
        + "root\n";
    private static final String DELETE_INPUT = "interface Bundle-Ether1000.100\n"
        + "ethernet cfm\n"
        + "no mep domain DML1\n"
        + "root\n";

    private static final String INTERFACE_NAME = "Bundle-Ether1000";
    private static final Long SUBIFC_INDEX = Long.valueOf(100L);

    private static final InterfaceKey INTERFACE_KEY = new InterfaceKey(INTERFACE_NAME);
    private static final SubinterfaceKey SUBIFC_KEY = new SubinterfaceKey(SUBIFC_INDEX);

    private static final String DOMAIN_NAME = "DML1";
    private static final DomainKey DOMAIN_KEY = new DomainKey(DOMAIN_NAME);
    private static final String MA_NAME = "MA-001";
    private static final Integer MEP_ID = Integer.valueOf(10);

    private static final InstanceIdentifier<Domain> IID =
        IidUtils.createIid(IIDs.IN_IN_SU_SU_AUG_IFSUBIFCFMAUG_CF_DO_DOMAIN, INTERFACE_KEY, SUBIFC_KEY, DOMAIN_KEY);

    private static final Domain DATA_ENABLED = new DomainBuilder()
        .setDomainName(DOMAIN_NAME)
        .setConfig(new ConfigBuilder()
            .setDomainName(DOMAIN_NAME)
            .build())
        .setMep(new MepBuilder()
            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm
                ._interface.cfm.domains.domain.mep.ConfigBuilder()
                .setMaName(MA_NAME)
                .setMepId(MEP_ID)
                .build())
            .build())
        .build();

    private static final Domain DATA_DISABLED = new DomainBuilder(DATA_ENABLED)
        .setMep(null)
        .build();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        target = new CfmDomainWriter(cli);
    }

    @Test
    public void testWriteCurrentAttributesEnabled() throws WriteFailedException {
        target.writeCurrentAttributes(IID, DATA_ENABLED, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testWriteCurrentAttributesDisabled() throws WriteFailedException {
        target.writeCurrentAttributes(IID, DATA_DISABLED, writeContext);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testUpdateCurrentAttributesDisabled() throws WriteFailedException {
        target.updateCurrentAttributes(IID, DATA_ENABLED, DATA_DISABLED, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testUpdateCurrentAttributesEnabled() throws WriteFailedException {
        target.updateCurrentAttributes(IID, DATA_DISABLED, DATA_ENABLED, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributesEnabled() throws WriteFailedException {
        target.deleteCurrentAttributes(IID, DATA_ENABLED, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributesDisabled() throws WriteFailedException {
        target.deleteCurrentAttributes(IID, DATA_DISABLED, writeContext);
        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }
}
