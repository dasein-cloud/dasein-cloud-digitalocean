package org.dasein.cloud.digitalocean.network;

import org.dasein.cloud.CloudException;
import org.dasein.cloud.InternalException;
import org.dasein.cloud.OperationNotSupportedException;
import org.dasein.cloud.ResourceStatus;
import org.dasein.cloud.digitalocean.DigitalOcean;
import org.dasein.cloud.digitalocean.compute.DOImageCapabilities;
import org.dasein.cloud.digitalocean.models.Action;
import org.dasein.cloud.digitalocean.models.Actions;
import org.dasein.cloud.digitalocean.models.FloatingIp;
import org.dasein.cloud.digitalocean.models.FloatingIps;
import org.dasein.cloud.digitalocean.models.actions.floatingIp.Assign;
import org.dasein.cloud.digitalocean.models.actions.floatingIp.Create;
import org.dasein.cloud.digitalocean.models.actions.floatingIp.Delete;
import org.dasein.cloud.digitalocean.models.actions.floatingIp.Unassign;
import org.dasein.cloud.digitalocean.models.rest.DigitalOceanModelFactory;
import org.dasein.cloud.network.*;
import org.dasein.cloud.util.APITrace;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.dasein.cloud.digitalocean.models.rest.DigitalOceanModelFactory.getDropletByInstance;
import static org.dasein.cloud.digitalocean.models.rest.DigitalOceanModelFactory.getEventById;
import static org.dasein.cloud.digitalocean.models.rest.DigitalOceanModelFactory.getModelById;

/**
 * Created by mariapavlova on 13/11/2015.
 */
public class DOFloatingIP extends AbstractIpAddressSupport<DigitalOcean> {

    private transient volatile DOFloatingIPCapabilities capabilities;

    protected DOFloatingIP(DigitalOcean provider) {
        super(provider);
    }

    @Override
    public void assign(@Nonnull String addressId, @Nonnull String serverId) throws InternalException, CloudException {
        APITrace.begin(getProvider(), "IpAddress.assign");
        try {
            // Droplets cannot be assigned a floating ip address while there are pending events on a droplet
            getProvider().getComputeServices().getVirtualMachineSupport().waitForAllDropletEventsToComplete(serverId, 5);

            Action action = DigitalOceanModelFactory.performAction(getProvider(), new Assign(serverId), addressId);
            while( action != null && !"completed".equalsIgnoreCase(action.getStatus())) {
                try {
                    Thread.sleep(15000);
                }
                catch( InterruptedException ignore ) {
                }
                action = getEventById(getProvider(), action.getId());
            }
        }
        finally{
            APITrace.end();
        }
    }

    @Override
    public void assignToNetworkInterface(@Nonnull String addressId, @Nonnull String nicId) throws InternalException, CloudException {
        throw new OperationNotSupportedException("Assigning to a NIC is not supported in " + getProvider().getCloudName());
    }

    @Override
    public @Nonnull String forward(@Nonnull String addressId, int publicPort, @Nonnull Protocol protocol, int privatePort, @Nonnull String onServerId) throws InternalException, CloudException {
        throw new OperationNotSupportedException("Forwarding is not supported in " + getProvider().getCloudName());
    }

    @Override
    public @Nonnull IPAddressCapabilities getCapabilities() throws CloudException, InternalException {
        if (capabilities == null) {
            capabilities = new DOFloatingIPCapabilities(getProvider());
        }
        return capabilities;
    }

    @Override
    public @Nullable IpAddress getIpAddress(@Nonnull String addressId) throws InternalException, CloudException {
        APITrace.begin(getProvider(), "IpAddress.getIpAddress");
        try {
            FloatingIp floatingIp = (FloatingIp) getModelById(getProvider(), org.dasein.cloud.digitalocean.models.rest.DigitalOcean.FLOATING_IP, addressId);
            return toIpAddress(floatingIp);
        }
        catch( CloudException e ) {
            if( e.getHttpCode() == 404 ) {
                return null;
            }
            throw e;
        }
        finally {
            APITrace.end();
        }
    }

    private IpAddress toIpAddress(FloatingIp floatingIp) {
        IpAddress ipAddress = new IpAddress();
        ipAddress.setVersion(IPVersion.IPV4);
        ipAddress.setIpAddressId(floatingIp.getIp());
        ipAddress.setAddress(floatingIp.getIp());
        if( floatingIp.getDroplet() != null ) {
            ipAddress.setServerId(floatingIp.getDroplet().getId());
        }
        ipAddress.setRegionId(floatingIp.getRegion().getId());
        ipAddress.setAddressType(AddressType.PUBLIC);
        return ipAddress;
    }

    @Override
    public boolean isSubscribed() throws CloudException, InternalException {
        try {
            DigitalOceanModelFactory.getModel(getProvider(), org.dasein.cloud.digitalocean.models.rest.DigitalOcean.FLOATING_IPS);
            return true;
           //return (DigitalOceanModelFactory.checkAction(getProvider(), "floatingIps") == 200);
        } catch (CloudException e) {
            return false;
        }
    }

    @Override
    public @Nonnull Iterable<IpAddress> listIpPool(@Nonnull IPVersion ipVersion, boolean b) throws InternalException, CloudException {
        APITrace.begin(getProvider(), "IpAddress.listIpPool");
        try {
            if( IPVersion.IPV6.equals(ipVersion) ) {
                return Collections.EMPTY_LIST;
            }
            FloatingIps ips = ( FloatingIps ) DigitalOceanModelFactory.getModel(getProvider(), org.dasein.cloud.digitalocean.models.rest.DigitalOcean.FLOATING_IPS);
            List<IpAddress> results = new ArrayList<IpAddress>();
            for( FloatingIp fi : ips.getFloatingIps() ) {
                IpAddress addr = toIpAddress(fi);
                if( getContext().getRegionId().equalsIgnoreCase(addr.getRegionId()) ) {
                    results.add(addr);
                }
            }
            return results;
        }
        finally {
            APITrace.end();
        }
    }

    @Override
    public @Nonnull Iterable<ResourceStatus> listIpPoolStatus(@Nonnull IPVersion ipVersion) throws InternalException, CloudException {
        APITrace.begin(getProvider(), "IpAddress.listIpPool");
        try {
            if( IPVersion.IPV6.equals(ipVersion) ) {
                return Collections.EMPTY_LIST;
            }
            FloatingIps ips = (FloatingIps) DigitalOceanModelFactory.getModel(getProvider(), org.dasein.cloud.digitalocean.models.rest.DigitalOcean.FLOATING_IPS);
            List<ResourceStatus> statuses = new ArrayList<ResourceStatus>();
            for (FloatingIp fi : ips.getFloatingIps()) {
                statuses.add(new ResourceStatus(fi.getIp(), !fi.isLocked()));
            }
            return statuses;
        }
        finally {
            APITrace.end();
        }
    }

    @Override
    public void releaseFromPool(@Nonnull String addressId) throws InternalException, CloudException {
        APITrace.begin(getProvider(), "IpAddress.releaseFromPool");
        try {
            DigitalOceanModelFactory.performAction(getProvider(), new Delete(), addressId);
        }
        finally{
            APITrace.end();
        }
    }

    @Override
    public void releaseFromServer(@Nonnull String addressId) throws InternalException, CloudException {
        APITrace.begin(getProvider(), "IpAddress.releaseFromServer");
        try {
            Action action = DigitalOceanModelFactory.performAction(getProvider(), new Unassign(), addressId);
            while( action != null && !"completed".equalsIgnoreCase(action.getStatus())) {
                try {
                    Thread.sleep(15000);
                }
                catch( InterruptedException ignore ) {
                }
                action = getEventById(getProvider(), action.getId());
            }
        }
        finally{
            APITrace.end();
        }
    }

    @Override
    public @Nonnull String request(@Nonnull IPVersion ipVersion) throws InternalException, CloudException {
        APITrace.begin(getProvider(), "IPVersion.request");
        try {
            if( IPVersion.IPV6.equals(ipVersion) ) {
                throw new OperationNotSupportedException("IPV6 is not supported by " + getProvider().getCloudName());
            }
            String regionId = getContext().getRegionId();
            if( regionId == null ) {
                throw new CloudException("No region was set for this request.");
            }

            Create action = new Create(getContext().getRegionId());
            FloatingIp ip = ( FloatingIp ) DigitalOceanModelFactory.performAction(getProvider(), action, org.dasein.cloud.digitalocean.models.rest.DigitalOcean.FLOATING_IP);
            IpAddress ipAddress = toIpAddress(ip);

            if( ip != null ) {
                return ipAddress.getProviderIpAddressId();
            }
            else {
                throw new CloudException("Unable to create floating IP for this IP version "+ipVersion);
            }
        }
        finally{
            APITrace.end();
        }
    }

    @Override
    public @Nonnull String requestForVLAN(@Nonnull IPVersion version) throws InternalException, CloudException {
        throw new OperationNotSupportedException("Requesting for VLAN is not supported in " + getProvider().getCloudName());
    }

    @Override
    public @Nonnull String requestForVLAN(@Nonnull IPVersion version, @Nonnull String vlanId) throws InternalException, CloudException {
        throw new OperationNotSupportedException("Requesting for VLAN is not supported in " + getProvider().getCloudName());
    }

}
