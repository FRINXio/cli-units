/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.handlers.def;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.types.rev170228.DEFAULTINSTANCE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultConfigWriter implements CliWriter<Config> {

    private static final IllegalArgumentException EX = new IllegalArgumentException("Default network instance cannot be manipulated");

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        if(config.getType().equals(DEFAULTINSTANCE.class)) {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config, EX);
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore, @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {

        if(dataAfter.getType().equals(DEFAULTINSTANCE.class)) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, EX);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {

        if(config.getType().equals(DEFAULTINSTANCE.class)) {
            throw new WriteFailedException.DeleteFailedException(instanceIdentifier, EX);
        }
    }
}
