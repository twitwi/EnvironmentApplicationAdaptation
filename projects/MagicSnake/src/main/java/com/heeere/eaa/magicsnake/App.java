package com.heeere.eaa.magicsnake;

import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) throws IOException {
        new MagicSnakeService(new ServiceFactoryImpl());
    }
}
