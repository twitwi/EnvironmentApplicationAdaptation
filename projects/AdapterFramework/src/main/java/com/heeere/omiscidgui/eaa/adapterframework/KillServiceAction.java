/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework;

import com.heeere.omiscidgui.eaa.adapterframework.interf.KillableServiceTask;
import fr.prima.omiscidgui.browser.interf.AbstractContextAwareAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author emonet
 */
public class KillServiceAction extends AbstractContextAwareAction<KillableServiceTask> {

    public KillServiceAction() {
        this(Utilities.actionsGlobalContext());
    }
    
    public Action createContextAwareInstance(Lookup context) {
        return new KillServiceAction(context);
    }

    public KillServiceAction(Lookup context) {
        super(KillableServiceTask.class, context);
        this.putValue(NAME, NbBundle.getMessage(KillServiceAction.class, "CTL_KillServiceAction"));
    }

    @Override
    protected void updateAction(Lookup context) {
        this.setEnabled(context.lookupAll(KillableServiceTask.class).size() > 0);
    }

    @Override
    public void actionPerformed(Lookup context, ActionEvent event) {
        for (final KillableServiceTask task : context.lookupAll(KillableServiceTask.class)) {
            Runnable killTask = new Runnable() {
                public void run() {
                    task.getServiceProxy().setVariableValue(task.getVariableName(), task.getVariableValue());
                }
            };
            new Thread(killTask).start();
        }
    }

}
