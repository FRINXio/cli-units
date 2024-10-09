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

package io.frinx.cli.unit.brocade.ifc.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X88A8;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class TpIdInterfaceWriter implements CliWriter<Config1> {

    private static final Pattern TAG1_TAG2_PATTERN = Pattern.compile("\\s+tag-value\\s+.*tag1, tag2.*");

    private static final String WRITE_TEMPLATE = """
            configure terminal
            {% if ($delete) %}no {% endif %}tag-type {$tpId} {$typeOnDevice} {$ifcNumber}
            end""";

    private Cli cli;

    public TpIdInterfaceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config1> id,
                                       @NotNull Config1 dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = Util.parseType(name);
        String typeOnDevice = Util.getTypeOnDevice(ifcType);
        String ifcNumber = Util.getIfcNumber(name);
        boolean useTag1Tag2 = isUsingTagAliases(id, dataAfter);

        if (useTag1Tag2) {
            blockingWriteAndRead(cli, id, dataAfter, fT(WRITE_TEMPLATE, "tpId", getTpIdTag(dataAfter),
                    "typeOnDevice", typeOnDevice, "ifcNumber", ifcNumber));
        } else {
            String tpIdForDevice = getTpIdForDevice(dataAfter);
            blockingWriteAndRead(cli, id, dataAfter, fT(WRITE_TEMPLATE,
                    "tpId", tpIdForDevice, "typeOnDevice", typeOnDevice, "ifcNumber", ifcNumber));
        }
    }

    private boolean isUsingTagAliases(@NotNull InstanceIdentifier<Config1> id,
                                      @NotNull Config1 data) throws WriteFailedException.CreateFailedException {
        // Determine what syntax to use to set tagging
        try {
            cli.execute(Command.writeCommand("configure terminal")).toCompletableFuture().get();
            String helpOutput = cli.executeAndRead(Command.writeCommand("?"), "<cr>").toCompletableFuture().get();
            String checkOutput = blockingWriteAndRead(cli, id, data, "end");
            return TAG1_TAG2_PATTERN.matcher(helpOutput + checkOutput).find();
        } catch (InterruptedException var6) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted", var6);
        } catch (ExecutionException var7) {
            LOG.warn("{}: Unable to write: {}, {} by executing: {}", cli, id, data, "configure terminal\n?", var7);
            throw new WriteFailedException.CreateFailedException(id, data, var7);
        }

    }

    @VisibleForTesting
    static String getTpIdForDevice(@NotNull Config1 dataAfter) {
        String simpleTpIdClassName = dataAfter.getTpid().getSimpleName().toLowerCase(Locale.ROOT);
        return simpleTpIdClassName.substring(simpleTpIdClassName.indexOf('x') + 1).toUpperCase(Locale.ROOT);
    }

    @VisibleForTesting
    static String getTpIdTag(@NotNull Config1 dataAfter) {
        if (dataAfter.getTpid() == TPID0X88A8.class) {
            return "tag2";
        } else if (dataAfter.getTpid() == TPID0X8100.class) {
            return "tag1";
        } else {
            throw new IllegalArgumentException(String.format("This TPID [%s] is not supported.", dataAfter.getTpid()));
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config1> id, @NotNull Config1 dataBefore,
                                        @NotNull Config1 dataAfter, @NotNull WriteContext writeContext)
            throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }


    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config1> id,
                                        @NotNull Config1 dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = Util.parseType(name);
        String typeOnDevice = Util.getTypeOnDevice(ifcType);
        String ifcNumber = Util.getIfcNumber(name);

        boolean useTag1Tag2 = isUsingTagAliases(id, dataBefore);

        if (useTag1Tag2) {
            blockingDeleteAndRead(cli, id, fT(WRITE_TEMPLATE,
                    "tpId", getTpIdTag(dataBefore),
                    "typeOnDevice", typeOnDevice,
                    "ifcNumber", ifcNumber,
                    "delete", true));
        } else {
            String tpIdForDevice = getTpIdForDevice(dataBefore);
            blockingDeleteAndRead(cli, id, fT(WRITE_TEMPLATE,
                    "tpId", tpIdForDevice,
                    "typeOnDevice", typeOnDevice,
                    "ifcNumber", ifcNumber,
                    "delete", true));
        }
    }
}