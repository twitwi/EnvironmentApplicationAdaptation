/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework;

import com.heeere.omiscidgui.eaa.adapterframework.model.AdapterFrameworkModel;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscidgui.browser.ServiceClient;
import fr.prima.omiscidgui.browser.interf.AbstractContextAwareAction;
import fr.prima.omiscidgui.browser.interf.Service;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author emonet
 */
public class TestModelAction extends AbstractContextAwareAction<Service> {

    public TestModelAction() {
        this(Utilities.actionsGlobalContext());
    }
    
    public Action createContextAwareInstance(Lookup context) {
        return new TestModelAction(context);
    }

    public TestModelAction(Lookup context) {
        super(Service.class, context);
        this.putValue(NAME, NbBundle.getMessage(TestModelAction.class, "CTL_TestModelAction"));
    }

    @Override
    protected void updateAction(Lookup context) {
        this.setEnabled(context.lookupAll(Service.class).size() > 0);
    }

    @Override
    public void actionPerformed(Lookup context, ActionEvent event) {
        final ServiceRepository repo = context.lookup(ServiceRepository.class);
        final ServiceClient serviceClient = context.lookup(ServiceClient.class);
        final AdapterFrameworkModel model = new AdapterFrameworkModel();

        new OmiscidModelController(model, repo);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        new ProposalModelController(model).createProposalWithMaximumDepth(5).createExpectedLinks();

        BindingPanelTopComponent.getDefault().setModel(model, serviceClient);
    }

}
