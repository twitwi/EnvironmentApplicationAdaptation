/**
 *
 * Software written by Remi Emonet.
 *
 */
package com.heeere.eaa.adaptertools;

import fr.prima.omiscid.user.exception.InvalidDescriptionException;
import fr.prima.omiscid.user.service.ServiceFactory;
import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * A version of the launcher that just starts an omiscid service from xml
 * (without fancy interpretation)
 */
public class XMLServiceLauncherPlain {

    public static void main(String[] args) throws FileNotFoundException, InvalidDescriptionException {
        if (args.length != 1) {
            System.err.println("This program takes only one parameter: the input file (xml).");
            return;
        }
        ServiceFactory f = new ServiceFactoryImpl();
        f.createFromXML(new FileInputStream(args[0])).start();
    }
}
