package org.dasein.cloud.digitalocean.network;

import org.dasein.cloud.AbstractCapabilities;
import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.Requirement;
import org.dasein.cloud.compute.VmState;
import org.dasein.cloud.digitalocean.DigitalOcean;
import org.dasein.cloud.network.IPAddressCapabilities;
import org.dasein.cloud.network.IPVersion;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

/**
 * Created by mariapavlova on 13/11/2015.
 */
public class DOFloatingIPCapabilities extends AbstractCapabilities<DigitalOcean> implements IPAddressCapabilities {
    public DOFloatingIPCapabilities(@Nonnull DigitalOcean provider) {
        super(provider);
    }

    @Nonnull @Override public String getProviderTermForIpAddress(@Nonnull Locale locale) {
        return "floating ip";
    }

    @Nonnull @Override public Requirement identifyVlanForVlanIPRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Nonnull @Override public Requirement identifyVlanForIPRequirement() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Nonnull @Override public Requirement identifyVMForPortForwarding() throws CloudException, InternalException {
        return Requirement.NONE;
    }

    @Override public boolean isAssigned(@Nonnull IPVersion ipVersion) throws CloudException, InternalException {
        return IPVersion.IPV4.equals(ipVersion);
    }

    @Override public boolean canBeAssigned(@Nonnull VmState vmState) throws CloudException, InternalException {
        return false;
    }

    @Override public boolean isAssignablePostLaunch(@Nonnull IPVersion ipVersion) throws CloudException, InternalException {
        return true;
    }

    @Override public boolean isForwarding(IPVersion ipVersion) throws CloudException, InternalException {
        return false;
    }

    @Override public boolean isRequestable(@Nonnull IPVersion ipVersion) throws CloudException, InternalException {
        return IPVersion.IPV4.equals(ipVersion);
    }

    @Nonnull @Override public Iterable<IPVersion> listSupportedIPVersions() throws CloudException, InternalException {
        return Collections.singleton(IPVersion.IPV4);
    }

    @Override public boolean supportsVLANAddresses(@Nonnull IPVersion ipVersion) throws InternalException, CloudException {
        return false;
    }
}
