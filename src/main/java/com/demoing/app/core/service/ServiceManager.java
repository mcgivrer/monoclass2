package com.demoing.app.core.service;

import com.demoing.app.core.Application;
import com.demoing.app.core.service.render.Render;
import com.demoing.app.core.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ServiceManager {
    private final Application app;
    Map<String, Service> services = new ConcurrentHashMap<>();
    private List<Service> servicesList = new CopyOnWriteArrayList<>();

    public ServiceManager(Application app) {
        this.app = app;
    }

    public void start() {
        servicesList.forEach(e -> {
            Logger.log(Logger.INFO, ServiceManager.class, "Service %s starting (dependencies:%s)", e.getName(), Arrays.stream(e.getDependencies()).toList());
            if (Arrays.stream(
                            e.getDependencies())
                    .allMatch(s -> Optional.ofNullable(services.get(s)).isPresent())) {
                e.start(this.app);
            } else {
                Collection<String> missingDependency = Arrays.stream(
                                e.getDependencies())
                        .filter(s -> Optional.ofNullable(services.get(s)).isEmpty())
                        .collect(Collectors.toList());
                Logger.log(Logger.ERROR, ServiceManager.class, "Unable to start service %s because %s dependency is missing", e.getName(), missingDependency);
            }
        });
    }

    public void dispose() {
        services.entrySet().forEach(e -> {
            e.getValue().dispose(this.app);
        });
    }

    public Service get(String name) {
        return services.get(name);
    }

    public void add(Service service) {
        this.services.put(service.getName(), service);
        this.servicesList.add(service);
    }
}
