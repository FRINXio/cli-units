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

package io.frinx.cli.unit.iosxr.oam.handler.domain;

import static org.hamcrest.MatcherAssert.assertThat;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.oam.IIDs;
import java.util.Optional;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.DomainKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.MaKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.ma.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.ma.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.types.rev190619.CcmInterval;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.types.rev190619.DomainLevel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class CfmMaConfigReaderTest {
    private static final String SH_RUN_DOMAIN_SERVICE =
        "show running-config ethernet cfm domain DML1 level 3 service MA-002 down-meps";
    private static final String SH_RUN_DOMAIN_SERVICE_OUTPUT = """
            ethernet cfm
             domain DML1
              service MA-002 down-meps
               continuity-check interval 1m loss-threshold 3
               mep crosscheck
                mep-id 2
                mep-id 4
               !
               efd
              !
             !
            !""";

    private static final String SH_RUN_CROSSCHECK =
        "show running-config ethernet cfm domain DML1 level 3 service MA-002 down-meps mep crosscheck";
    private static final String SH_RUN_CROSSCHECK_OUTPUT = """
            ethernet cfm
             domain DML1
              service MA-002 down-meps
               mep crosscheck
                mep-id 2
                mep-id 4
               !
              !
             !
            !""";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private CfmMaConfigReader target;
    private static final String DOMAIN_NAME = "DML1";
    private static final String MA_NAME = "MA-002";
    private static final DomainKey DOMAIN_KEY = new DomainKey(DOMAIN_NAME);
    private static final MaKey MA_KEY = new MaKey(MA_NAME);

    private static final InstanceIdentifier<Config> IID = IidUtils.createIid(IIDs.OA_CF_DO_DO_MA_MA_CONFIG,
        DOMAIN_KEY, MA_KEY);

    private static final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm
        .domains.domain.Config DOMAIN_CONFIG =
        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain
        .ConfigBuilder()
            .setDomainName(DOMAIN_NAME)
            .setLevel(new DomainLevel((short) 3))
            .build();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new CfmMaConfigReader(cli));
    }

    @Test
    void testReadCurrentAttributes() throws ReadFailedException {
        Mockito.doReturn(Optional.of(DOMAIN_CONFIG)).when(ctx).read(Mockito.any());
        Mockito.doReturn(SH_RUN_DOMAIN_SERVICE_OUTPUT).when(target)
            .blockingRead(SH_RUN_DOMAIN_SERVICE, cli, IID, ctx);
        Mockito.doReturn(SH_RUN_CROSSCHECK_OUTPUT).when(target)
        .blockingRead(SH_RUN_CROSSCHECK, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        assertThat(builder.getMaName(), CoreMatchers.equalTo(MA_NAME));
        assertThat(builder.getContinuityCheckInterval(), CoreMatchers.equalTo(CcmInterval._1m));
        assertThat(builder.getContinuityCheckLossThreshold(), CoreMatchers.equalTo(3L));
        assertThat(builder.getMepCrosscheck(), Matchers.containsInAnyOrder(2, 4));
    }
}
