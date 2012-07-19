/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework;

import com.heeere.omiscidgui.eaa.adapterframework.interf.KillableServiceTask;
import fr.prima.omiscidgui.browser.interf.AbstractOmiscidSelector;
import fr.prima.omiscidgui.browser.interf.Service;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author emonet
 */
public class KillableServiceSelector extends AbstractOmiscidSelector<Service> {

    public KillableServiceSelector() {
        super(Service.class);
    }

    @Override
    protected void getTasks(ArrayList result, Collection<Service> selection) {
        for (Service service : selection) {
            if (service.getServiceProxy().getVariables().contains("killService")) {
                result.add(new KillableServiceTask(service.getServiceProxy()));
            }
        }
    }

}
