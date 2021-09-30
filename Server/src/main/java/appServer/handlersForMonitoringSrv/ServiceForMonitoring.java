package appServer.handlersForMonitoringSrv;

import appServer.serviceApp.EntityUser;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceForMonitoring {
    @Getter
    private final AtomicInteger countConnection=new AtomicInteger(0);
    @Getter
    private final ConcurrentHashMap<EntityUser, LocalDateTime > users = new ConcurrentHashMap<>() ;

}
