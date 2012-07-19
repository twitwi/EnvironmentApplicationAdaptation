/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework;

import com.heeere.omiscidgui.eaa.adapterframework.model.Adapter;
import com.heeere.omiscidgui.eaa.adapterframework.model.AdapterFrameworkModel;
import com.heeere.omiscidgui.eaa.adapterframework.model.ExpectedRequirementAutoLink;
import com.heeere.omiscidgui.eaa.adapterframework.model.Parameter;
import com.heeere.omiscidgui.eaa.adapterframework.model.ProvidedSlot;
import com.heeere.omiscidgui.eaa.adapterframework.model.RequiredSlot;
import com.heeere.omiscidgui.eaa.adapterframework.model.Requirement;
import com.heeere.omiscidgui.eaa.adapterframework.model.ServiceProperty;
import com.heeere.omiscidgui.eaa.adapterframework.model.ServiceSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author twilight
 */
public class ProposalModelController {

    private AdapterFrameworkModel model;

    public ProposalModelController(AdapterFrameworkModel model) {
        this.model = model;
    }

    public static final ProvidedSlot MARK = new ProvidedSlot(null, null, null) {
        @Override
        public String toString() {
            return "MARK";
        }
    };
    public ProposalModelController createExpectedLinks() {
        for (Requirement req : model.getRequirements()) {
            for (Adapter adapter : model.getAdapters()) {
                if (adapter.getMode() == Adapter.Mode.TEMPLATE) {
                    continue;
                }
                if (specificationMatches(adapter.getProvided().getSpecification(), req.getSpecification())) {
                    req.addToExpectedLinks(new ExpectedRequirementAutoLink(adapter.getProvided()));
                }
            }
        }
        return this;
    }
    public ProposalModelController createProposalWithMaximumDepth(int maxDepth) {
        List<ProvidedSlot> open = list();
        for (ProvidedSlot providedSlot : model.getProviders()) {
            open.add(providedSlot);
        }
        open.add(MARK);
        int depth = 0;
        while (!open.isEmpty()) {
            ProvidedSlot current = open.remove(0);
            if (current == MARK) {
                depth ++;
                if (depth >= maxDepth) {
                    break;
                } else {
                    open.add(MARK);
                    continue;
                }
            }
            // process current
            for (Adapter adapter : new ArrayList<Adapter>(model.getAdapters())) {
                if (adapter.getMode() != Adapter.Mode.TEMPLATE) {
                    continue;
                }
                String from = adapter.getRequired().getSpecification().getName();
                if (!from.equals(current.getSpecification().getName())) {
                    continue;
                }
                // TODO more verif on from? (parameters ...)
                Map<String, String> parameterValue = new HashMap<String, String>();
                for (Parameter p : adapter.getParameters()) {
                    if (p.getDefaultValue() != null) {
                        parameterValue.put(p.getName(), p.getDefaultValue());
                    }
                }
                if (countNumberOfSomeRequirement(adapter.getParameters()) > 0) {
                    if (countNumberOfSomeRequirement(adapter.getParameters()) > 1) {
                        throw new UnsupportedOperationException("TODO (or use only one #(someRequirement ...) until its done)");
                    }
                    Parameter parameterWithSomeRequirement = null;
                    List<String> someRequirementElements = null;
                    for (Parameter p : adapter.getParameters()) {
                        if (p.getSpecialValue() != null && "someRequirement".equals(p.getSpecialValue().getParameters().get(0))) {
                            someRequirementElements = p.getSpecialValue().getParameters();
                            parameterWithSomeRequirement = p;
                            break;
                        }
                    }
                    String reqName = someRequirementElements.get(1);
                    String reqProp = someRequirementElements.get(2);
                    // for each requirement with the right property
                    for (Requirement req : model.getRequirements()) {
                        if (reqName.equals(req.getSpecification().getName())) {
                            parameterValue.put(parameterWithSomeRequirement.getName(), findProperty(req.getSpecification(), reqProp).getValue());
                            Adapter newAdapter = createProposedAdapterFrom(current, adapter, parameterValue);
                            parameterValue.remove(parameterWithSomeRequirement.getName());
                            model.addToAdapters(newAdapter);
                            open.add(newAdapter.getProvided());
                        }
                    }
                } else {
                    Adapter newAdapter = createProposedAdapterFrom(current, adapter, null);
                    model.addToAdapters(newAdapter);
                    open.add(newAdapter.getProvided());
                }
            }
        }
        return this;
    }
    private Adapter createProposedAdapterFrom(ProvidedSlot current, Adapter adapter, Map<String, String> parameterValue) {
        RequiredSlot req = new RequiredSlot(current.getSpecification(), current);
        String to = adapter.getProvided().getSpecification().getName();
        ProvidedSlot pro = new ProvidedSlot(new ServiceSpecification(to), null, null);
        for (ServiceProperty sp : adapter.getProvided().getSpecification().getServiceProperties()) {
            String value = sp.getValue();
            if (value.startsWith("${")) {
                value = parameterValue.get(value.replaceFirst("^[$][{]", "").replaceAll("[}]$", ""));
                // TODO factor code with existing js implementation + handle more complex cases
            }
            pro.getSpecification().getServiceProperties().add(new ServiceProperty(sp.getName(), value));
        }
        List<Parameter> affectedParameters = new ArrayList<Parameter>();
        for (Parameter p : adapter.getParameters()) {
            String value = parameterValue != null ? parameterValue.get(p.getName()) : null;
            value = value != null ? value : p.getDefaultValue();
            value = value != null ? value : "";
            affectedParameters.add(new Parameter(p.getName(), null, null, null, null, value));
        }
        Adapter res = new Adapter(Adapter.Mode.PROPOSED, req, pro, affectedParameters, adapter.getPossibleServiceProxy());
        res.getProvided().setPossibleParentAdapter(res);
        return res;

    }

    private <T> List<T> list() {
        return new ArrayList<T>();
    }

    private int countNumberOfSomeRequirement(List<Parameter> parameters) {
        int res = 0;
        for (Parameter p : parameters) {
            if (p.getSpecialValue() != null && "someRequirement".equals(p.getSpecialValue().getParameters().get(0))) {
                res++;
            }
        }
        return res;
    }

    private ServiceProperty findProperty(ServiceSpecification specification, String prop) {
        for (ServiceProperty serviceProperty : specification.getServiceProperties()) {
            if (prop.equals(serviceProperty.getName())) {
                return serviceProperty;
            }
        }
        throw new IllegalArgumentException("Property not present “"+prop+"”");
    }

    private boolean specificationMatches(ServiceSpecification sPro, ServiceSpecification sReq) {
        if (!sPro.getName().equals(sReq.getName())) {
            return false;
        }
        for (ServiceProperty reqProp : sReq.getServiceProperties()) {
            boolean found = false;
            for (ServiceProperty proProp : sPro.getServiceProperties()) {
                if (proProp.getName().equals(reqProp.getName())) {
                    if (proProp.getValue().equals(reqProp.getValue())) {
                        found = true;
                    }
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }


}
