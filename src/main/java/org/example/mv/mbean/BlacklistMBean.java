package org.example.mv.mbean;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@ManagedResource(objectName = "sample:name=blacklist",description = "Blacklist of IP address")
public class BlacklistMBean {
    private Set<String> ips = new HashSet<>();

    @ManagedAttribute(description = "Get IP address in blacklist")
    public String[] getBlacklist(){
        return  ips.toArray(String[]::new);
    }

    @ManagedOperation
    @ManagedOperationParameter(name = "ip", description = "Target IP address that will be added to blacklist")
    public void addBlacklist(String ip){
        ips.add(ip);
    }
    @ManagedOperation
    @ManagedOperationParameter(name = "ip",description = "Target IP address that will be removed from blacklist")
    public void remove(String ip){
        ips.remove(ip);
    }

    public boolean shouldBlock(String ip){
        return ips.contains(ip);
    }
}
