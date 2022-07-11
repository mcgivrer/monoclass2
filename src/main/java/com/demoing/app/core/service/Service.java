package com.demoing.app.core.service;

import com.demoing.app.core.Application;

public interface Service {

    String getName();

    void start(Application app);

    void dispose(Application app);

    String[] getDependencies();
}
