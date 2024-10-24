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

package io.frinx.cli.unit.iosxe.system.handler.license;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.license.rev221202.licenses.top.licenses.License;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.license.rev221202.licenses.top.licenses.LicenseBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.license.rev221202.licenses.top.licenses.LicenseKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LicenseReader implements CliOperListReader<License, LicenseKey, LicenseBuilder>  {

    static final String SH_LICENSES = "show license usage";
    static final Pattern LINE = Pattern.compile(".+ \\((?<name>.+)\\): *.*");

    private Cli cli;

    public LicenseReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<License> id,
                                      @NotNull LicenseBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        builder.setLicenseId(id.firstKeyOf(License.class).getLicenseId());
    }

    @NotNull
    @Override
    public List<LicenseKey> getAllIds(@NotNull InstanceIdentifier<License> id,
                                      @NotNull ReadContext context) throws ReadFailedException {
        var licenseKeys = parseAllKeyIds(blockingRead(SH_LICENSES, cli, id, context));
        licenseKeys.add(new LicenseKey("Smart Licensing"));
        licenseKeys.add(new LicenseKey("Product Information"));
        licenseKeys.add(new LicenseKey("Reservation Info"));
        return licenseKeys;
    }

    @VisibleForTesting
    static List<LicenseKey> parseAllKeyIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                LINE::matcher,
                matcher -> matcher.group("name"),
                LicenseKey::new);
    }
}