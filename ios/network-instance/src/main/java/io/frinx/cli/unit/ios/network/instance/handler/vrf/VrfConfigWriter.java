package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public VrfConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = "configure terminal\n" +
            "ip vrf %s\n" +
            "description %s\n" +
            "exit\n" +
            "exit";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        if(config.getType().equals(L3VRF.class)) {

            blockingWriteAndRead(cli, instanceIdentifier, config,
                    f(WRITE_TEMPLATE,
                            config.getName(),
                            config.getDescription()));
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore, @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    private static final String DELETE_TEMPLATE = "configure terminal\n" +
            "no ip vrf %s\n" +
            "exit\n" +
            "exit";

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {

        if(config.getType().equals(L3VRF.class)) {

            blockingDeleteAndRead(cli, instanceIdentifier,
                    f(DELETE_TEMPLATE,
                            config.getName()));
        }
    }
}
