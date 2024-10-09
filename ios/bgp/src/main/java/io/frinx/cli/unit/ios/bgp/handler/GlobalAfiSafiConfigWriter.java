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

package io.frinx.cli.unit.ios.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.global.afi.safi.config.extension.RedistributeConnected;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.global.afi.safi.config.extension.RedistributeStatic;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalAfiSafiConfigWriter implements CliWriter<Config> {

    @SuppressWarnings("checkstyle:linelength")
    private static final String WRITE_TEMPLATE = """
            configure terminal
            router bgp {$as}
            {% if ($vrfName) %}address-family {$afiSafiName} vrf {$vrfName}
            {% else %}address-family {$afiSafiName}
            {% endif %}{% if ($routerId) %}bgp router-id {$routerId}
            {% endif %}{% if ($autoSummary) %}auto-summary
            {% endif %}{% if ($redistCon) %}redistribute connected{% if ($redistConRouteMap) %} route-map {$redistConRouteMap}{% endif %}
            {% endif %}{% if ($redistStat) %}redistribute static{% if ($redistStatRouteMap) %} route-map {$redistStatRouteMap}{% endif %}
            {% endif %}{% if ($defaultInf) %}default-information originate
            {% endif %}{% if ($sync) %}synchronization
            {% endif %}end""";

    private static final String UPDATE_TEMPLATE = "configure terminal\n"
            + "router bgp {$as}\n"
            + "{% if ($vrfName) %}address-family {$afiSafiName} vrf {$vrfName}\n"
            + "{% else %}address-family {$afiSafiName}\n"
            + "{% endif %}"
            // auto-summary
            + "{% if ($autoSummary == TRUE) %}auto-summary\n"
            + "{% elseIf ($autoSummary == FALSE) %}no auto-summary\n"
            + "{% endif %}"
            // redistribute-connected
            + "{% if ($redistCon) %}{$redistCon}{% endif %}"
            // redistribute-static
            + "{% if ($redistStat) %}{$redistStat}{% endif %}"
            // default-information originate
            + "{% if ($defaultInf == TRUE) %}default-information originate\n"
            + "{% elseIf ($defaultInf == FALSE) %}no default-information originate\n"
            + "{% endif %}"
            // synchronization
            + "{% if ($sync == TRUE) %}synchronization\n"
            + "{% elseIf ($sync == FALSE) %}no synchronization\n"
            + "{% endif %}"
            + "end";

    private static final String GLOBAL_BGP_AFI_SAFI_DELETE = """
            configure terminal
            router bgp %s
            no address-family %s
            end""";

    private static final String VRF_BGP_AFI_SAFI_DELETE = """
            configure terminal
            router bgp %s
            no address-family %s vrf %s
            end""";

    static final String VRF_BGP_AFI_SAFI_ROUTER_ID = """
            configure terminal
            router bgp %s
            address-family %s vrf %s
            bgp router-id %s
            end""";

    private Cli cli;

    public GlobalAfiSafiConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();
        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).get();
        final Long as = bgp.getGlobal().getConfig().getAs().getValue();
        BgpAfiSafiChecks.checkAddressFamilies(vrfKey, bgp);

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            blockingWriteAndRead(cli, id, config, writeTemplate(as, null, null, config));
        } else {
            Preconditions.checkArgument(writeContext.readAfter(RWUtils.cutId(id, NetworkInstance.class)).get()
                            .getConfig().getRouteDistinguisher() != null,
                    "Route distinguisher missing for VRF: %s. Cannot configure BGP afi/safi", vrfName);

            DottedQuad routerId = bgp.getGlobal().getConfig().getRouterId();
            if (routerId == null) {
                blockingWriteAndRead(cli, id, config, writeTemplate(as, vrfName, null, config));
            } else {
                blockingWriteAndRead(cli, id, config, writeTemplate(as, vrfName, routerId, config));
            }
        }
    }

    @VisibleForTesting
    String writeTemplate(Long as, @Nullable String vrfName, @Nullable DottedQuad routerId, Config config) {
        GlobalAfiSafiConfigAug aug = config.getAugmentation(GlobalAfiSafiConfigAug.class);
        boolean isIpv4 = config.getAfiSafiName().equals(IPV4UNICAST.class);

        return fT(WRITE_TEMPLATE, "data", config,
                "as", as,
                "vrfName", vrfName,
                "routerId", routerId == null ? null : routerId.getValue(),
                "afiSafiName", toDeviceAddressFamily(config.getAfiSafiName()),
                "autoSummary", (vrfName == null && aug != null && aug.isAutoSummary() != null
                        && aug.isAutoSummary() && isIpv4) ? true : null,
                "redistCon", (vrfName != null && aug != null && aug.getRedistributeConnected() != null
                        && aug.getRedistributeConnected().isEnabled() != null
                        && aug.getRedistributeConnected().isEnabled() && isIpv4) ? true : null,
                "redistConRouteMap", (vrfName != null && aug != null && aug.getRedistributeConnected() != null
                        && isIpv4) ? aug.getRedistributeConnected().getRouteMap() : null,
                "redistStat", (vrfName != null && aug != null && aug.getRedistributeStatic() != null
                        && aug.getRedistributeStatic().isEnabled() != null
                        && aug.getRedistributeStatic().isEnabled() && isIpv4) ? true : null,
                "redistStatRouteMap", (vrfName != null && aug != null && aug.getRedistributeStatic() != null
                        && isIpv4) ? aug.getRedistributeStatic().getRouteMap() : null,
                "defaultInf", (vrfName != null && aug != null && aug.isDefaultInformationOriginate() != null
                        && aug.isDefaultInformationOriginate() && isIpv4) ? true : null,
                "sync", (vrfName != null && aug != null && aug.isSynchronization() != null
                        && aug.isSynchronization() && isIpv4) ? true : null
        );
    }

    public static String toDeviceAddressFamily(Class<? extends AFISAFITYPE> afiSafiName) {
        if (afiSafiName.equals(IPV4UNICAST.class)) {
            return "ipv4";
        } else if (afiSafiName.equals(IPV6UNICAST.class)) {
            return "ipv6";
        } else if (afiSafiName.equals(L3VPNIPV4UNICAST.class)) {
            return "vpnv4";
        } else if (afiSafiName.equals(L3VPNIPV6UNICAST.class)) {
            return "vpnv6";
        } else {
            return afiSafiName.getSimpleName();
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // No point in updating a single command
        // FIXME Update here is dangerous, deleting and readding address-family is not an atomic operation
        // on IOS and the deletion is performed in background without freezing the delete command
        // then the subsequent "add afi command" fails. So not updating the address family here is safer for now
        // The downside is that we set router-id here under address-family if we are under a VRF. This means that
        // updates to router-id for VRFs bgp configuration does not work properly

        // updating auto-summary
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();

        final Bgp bgp = writeContext.readAfter(RWUtils.cutId(id, Bgp.class)).get();
        final Long as = bgp.getGlobal().getConfig().getAs().getValue();

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)
                && dataAfter.getAfiSafiName().equals(IPV4UNICAST.class)) {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(as, null, dataBefore, dataAfter));
        } else if (dataAfter.getAfiSafiName().equals(IPV4UNICAST.class)) {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(as, vrfName, dataBefore, dataAfter));
        }
    }

    @VisibleForTesting
    String updateTemplate(Long as, @Nullable String vrfName, Config dataBefore, Config dataAfter) {
        GlobalAfiSafiConfigAug augBefore = dataBefore.getAugmentation(GlobalAfiSafiConfigAug.class);
        GlobalAfiSafiConfigAug augAfter = dataAfter.getAugmentation(GlobalAfiSafiConfigAug.class);

        return fT(UPDATE_TEMPLATE, "before", dataBefore, "data", dataAfter,
                "as", as,
                "vrfName", vrfName,
                "afiSafiName", toDeviceAddressFamily(dataAfter.getAfiSafiName()),
                "autoSummary", vrfName == null ? updateAutoSummary(augBefore, augAfter) : null,
                "redistCon", vrfName != null ? updateRedistributeConnected(augBefore, augAfter) : null,
                "redistStat", vrfName != null ? updateRedistributeStatic(augBefore, augAfter) : null,
                "defaultInf", vrfName != null ? updateDefaultInformation(augBefore, augAfter) : null,
                "sync", vrfName != null ? updateSynchronization(augBefore, augAfter) : null
        );
    }

    private String updateAutoSummary(GlobalAfiSafiConfigAug dataBefore, GlobalAfiSafiConfigAug dataAfter) {
        Boolean autoSummaryBefore = dataBefore != null ? dataBefore.isAutoSummary() : null;
        Boolean autoSummaryAfter = dataAfter != null ? dataAfter.isAutoSummary() : null;

        if (!Objects.equals(autoSummaryAfter, autoSummaryBefore)) {
            return autoSummaryAfter == null || !autoSummaryAfter ? "FALSE" : Chunk.TRUE;
        }
        return null;
    }

    private String updateRedistributeConnected(GlobalAfiSafiConfigAug dataBefore, GlobalAfiSafiConfigAug dataAfter) {
        RedistributeConnected redistributeConBefore = dataBefore != null ? dataBefore.getRedistributeConnected() : null;
        RedistributeConnected redistributeConAfter = dataAfter != null ? dataAfter.getRedistributeConnected() : null;

        if (!Objects.equals(redistributeConBefore, redistributeConAfter)) {
            final StringBuilder stringBuilder = new StringBuilder();

            if (redistributeConBefore != null) {
                stringBuilder.append("no redistribute connected\n");
            }
            if (redistributeConAfter != null
                && redistributeConAfter.isEnabled() != null
                && redistributeConAfter.isEnabled()) {
                stringBuilder.append("redistribute connected");
                if (redistributeConAfter.getRouteMap() != null) {
                    stringBuilder.append(" route-map ").append(redistributeConAfter.getRouteMap());
                }
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        }

        return null;
    }

    private String updateRedistributeStatic(GlobalAfiSafiConfigAug dataBefore, GlobalAfiSafiConfigAug dataAfter) {
        RedistributeStatic redistributeStatBefore = dataBefore != null ? dataBefore.getRedistributeStatic() : null;
        RedistributeStatic redistributeStatAfter = dataAfter != null ? dataAfter.getRedistributeStatic() : null;

        if (!Objects.equals(redistributeStatAfter, redistributeStatBefore)) {
            final StringBuilder stringBuilder = new StringBuilder();

            if (redistributeStatBefore != null) {
                stringBuilder.append("no redistribute static\n");
            }
            if (redistributeStatAfter != null
                && redistributeStatAfter.isEnabled() != null
                && redistributeStatAfter.isEnabled()) {
                stringBuilder.append("redistribute static");
                if (redistributeStatAfter.getRouteMap() != null) {
                    stringBuilder.append(" route-map ").append(redistributeStatAfter.getRouteMap());
                }
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        }

        return null;
    }

    private String updateDefaultInformation(GlobalAfiSafiConfigAug dataBefore, GlobalAfiSafiConfigAug dataAfter) {
        Boolean defaultInformationBefore = dataBefore != null ? dataBefore.isDefaultInformationOriginate() : null;
        Boolean defaultInformationAfter = dataAfter != null ? dataAfter.isDefaultInformationOriginate() : null;

        if (!Objects.equals(defaultInformationBefore, defaultInformationAfter)) {
            return defaultInformationAfter == null || !defaultInformationAfter ? "FALSE" : Chunk.TRUE;
        }
        return null;
    }

    private String updateSynchronization(GlobalAfiSafiConfigAug dataBefore, GlobalAfiSafiConfigAug dataAfter) {
        Boolean synchronizationBefore = dataBefore != null ? dataBefore.isSynchronization() : null;
        Boolean synchronizationAfter = dataAfter != null ? dataAfter.isSynchronization() : null;

        if (!Objects.equals(synchronizationBefore, synchronizationAfter)) {
            return synchronizationAfter == null || !synchronizationAfter ? "FALSE" : Chunk.TRUE;
        }
        return null;
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();
        Long as = writeContext.readBefore(RWUtils.cutId(id, Global.class)).get().getConfig().getAs().getValue();
        final Bgp bgp = writeContext.readBefore(RWUtils.cutId(id, Bgp.class)).get();
        BgpAfiSafiChecks.checkAddressFamilies(vrfKey, bgp);

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            blockingWriteAndRead(f(GLOBAL_BGP_AFI_SAFI_DELETE,
                    as, toDeviceAddressFamily(config.getAfiSafiName())),
                    cli, id, config);
        } else {
            blockingWriteAndRead(f(VRF_BGP_AFI_SAFI_DELETE,
                    as, toDeviceAddressFamily(config.getAfiSafiName()), vrfName),
                    cli, id, config);
        }
    }
}