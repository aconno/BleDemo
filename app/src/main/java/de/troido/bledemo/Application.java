package de.troido.bledemo;

import com.karumi.dexter.Dexter;

import java.util.UUID;

public class Application extends android.app.Application {

    public static final UUID UUID = java.util.UUID.randomUUID();

    @Override
    public void onCreate() {
        super.onCreate();
        Dexter.initialize(this);
    }
}
