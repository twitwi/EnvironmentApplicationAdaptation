/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework;

import fr.prima.omiscid.user.exception.UnknownVariable;
import fr.prima.omiscid.user.service.ServiceFilter;
import fr.prima.omiscid.user.service.ServiceProxy;
import fr.prima.omiscid.user.service.ServiceRepository;
import fr.prima.omiscid.user.service.ServiceRepositoryListener;
import com.heeere.omiscidgui.eaa.adapterframework.model.Adapter;
import com.heeere.omiscidgui.eaa.adapterframework.model.AdapterFrameworkModel;
import com.heeere.omiscidgui.eaa.adapterframework.model.Parameter;
import com.heeere.omiscidgui.eaa.adapterframework.model.ProvidedSlot;
import com.heeere.omiscidgui.eaa.adapterframework.model.RequiredSlot;
import com.heeere.omiscidgui.eaa.adapterframework.model.Requirement;
import com.heeere.omiscidgui.eaa.adapterframework.model.ServiceProperty;
import com.heeere.omiscidgui.eaa.adapterframework.model.ServiceSpecification;
import com.heeere.omiscidgui.eaa.adapterframework.model.SpecialValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static fr.prima.omiscid.user.service.ServiceFilters.*;

/**
 *
 * @author emonet
 */
public class OmiscidModelController {

    private AdapterFrameworkModel model;
    private Map<ServiceProxy, List<Requirement>> omiscidRequirements = new Hashtable<ServiceProxy, List<Requirement>>();
    private Map<ServiceProxy, List<ProvidedSlot>> omiscidProviders = new Hashtable<ServiceProxy, List<ProvidedSlot>>();
    private Map<ServiceProxy, List<Adapter>> omiscidAdapters = new Hashtable<ServiceProxy, List<Adapter>>();

    public OmiscidModelController(AdapterFrameworkModel model, ServiceRepository repo) {
        this.model = model;
        // TODO handle cleaning!
        repo.addListener(new ServiceRepositoryListener() {
            public void serviceAdded(ServiceProxy proxy) {
                addRequirementsFor(proxy);
            }
            public void serviceRemoved(ServiceProxy proxy) {
                removeRequirementsFor(proxy);
            }
        }, and(hasVariable("requires") /*, hasVariable("name", null, "^.*Game$")*/));
        
        repo.addListener(new ServiceRepositoryListener() {
            public void serviceAdded(ServiceProxy proxy) {
                addProviderFor(proxy);
            }
            public void serviceRemoved(ServiceProxy proxy) {
                removeProviderFor(proxy);
            }
        }, and(hasVariable("provides"), not(hasVariable("factoryId"))
                /*, hasVariable("name", null, "^.*Mouse.*$")*/));

        repo.addListener(new ServiceRepositoryListener() {
            public void serviceAdded(ServiceProxy proxy) {
                addAdapterTemplateFor(proxy);
            }
            public void serviceRemoved(ServiceProxy proxy) {
                removeAdapterTemplateFor(proxy);
            }
        }, and(nameIs("AdapterFactory"), hasVariable("from"), hasVariable("to")));

        startListeningOfAdapterCreations(repo);
    }
    private void startListeningOfAdapterCreations(final ServiceRepository repo) {
        final ServiceFilter filter = and(hasVariable("factoryId"), hasVariable("factoryParameters"));
        final ServiceRepositoryListener listener = new ServiceRepositoryListener() {
            public void serviceAdded(ServiceProxy proxy) {
                possiblyMakeAdapterPresent(proxy);
            }
            public void serviceRemoved(ServiceProxy proxy) {
                final ServiceRepositoryListener listener = this;
                new Timer("Refresh Created Adapters").schedule(new TimerTask() {
                    @Override
                    public void run() {
                        List<ServiceProxy> allServices = repo.getAllServices();
                        refreshAdapters(allServices);
                    }

                }, 300);
            }
        };
        new Timer("Refresh Created Adapters").schedule(new TimerTask() {
            @Override
            public void run() {
                final List<ServiceProxy> allServices = repo.getAllServices();
                refreshAdapters(allServices);
            }
        }, 1000);
        repo.addListener(listener, filter);
    }

    private ServiceSpecification readServiceSpecification(String specificationString) {
        specificationString = specificationString.replaceAll("^[ \\n]*", "").replaceAll("[ \\n]*$", "");
        String[] parts = specificationString.split(" +");
        ServiceSpecification spec = new ServiceSpecification(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            String[] sub = parts[i].split("=");
            spec.addToServiceProperties(new ServiceProperty(sub[0], sub[1]));
        }
        return spec;
    }


    private void refreshAdapters(List<ServiceProxy> allServices) {
        int remainingTries = 10;
        boolean shouldRetry = true;
        while (shouldRetry && remainingTries > 0) {
            shouldRetry = false;
            remainingTries--;
            for (Adapter adapter : model.getAdapters()) {
                if (adapter.getMode() == Adapter.Mode.TEMPLATE) {
                    continue;
                }
                String adapterId;
                String adapterParameters;
                try {
                    adapterId = adapter.getPossibleServiceProxy().getPeerIdAsString();
                    adapterParameters = OmiscidAdapterCreator.getParametersAsString(adapter)/*.replaceFirst("^[^\\n]*\\n", "")*/;
                } catch (Exception ex) {
                    // TODO cleaner
                    shouldRetry = true;
                    if (remainingTries == 1) {
                        adapter.getProvided().setPossibleServiceProxy(null);
                        adapter.setMode(Adapter.Mode.PROPOSED);
                    }
                    continue;
                }
                Adapter.Mode newMode = Adapter.Mode.PROPOSED;
                ServiceProxy newProxy = null;
                for (ServiceProxy proxy : allServices) {
                    try {
                        String proxyId = proxy.getVariableValue("factoryId");
                        if (proxyId.equalsIgnoreCase(adapterId)) {
                            String proxyParameters = proxy.getVariableValue("factoryParameters")/*.replaceFirst("^[^\\n]*\\n", "")*/;
                            if (proxyParameters.equals(adapterParameters)) {
                                newMode = Adapter.Mode.PRESENT;
                                newProxy = proxy;
                            } else {
                                //System.err.println("DIFF: '"+proxyParameters+"' vs '"+adapterParameters+"'");

                            }
                        }
                    } catch (UnknownVariable ex) {
                        // service is no good
                        //System.err.println("......... X for "+proxy.getName());
                    }
                }
                adapter.getProvided().setPossibleServiceProxy(newProxy);
                adapter.setMode(newMode);
            }
        }
    }

    private void representAdapters(List<ServiceProxy> allServices) {
        int remainingTries = 10;
        boolean shouldRetry = true;
        while (shouldRetry && remainingTries > 0) {
            shouldRetry = false;
            remainingTries--;
            System.err.println(remainingTries);
            for (ServiceProxy proxy : allServices) {
                System.err.println(proxy.getName());
                try {
                    possiblyMakeAdapterPresent(proxy);
                } catch (UnsupportedOperationException ex) {
                    shouldRetry = true;
                    System.err.println("............ X");
                } catch (UnknownVariable ex) {
                    // service is gone
                }
            }
        }
        /*
        int remainingTries = 10;
        boolean shouldRetry = true;
        while (shouldRetry && remainingTries > 0) {
            shouldRetry = false;
            remainingTries--;
            for (ServiceProxy proxy : allServices) {
                try {
                    String targetId = proxy.getVariableValue("factoryId");
                    String targetParameters = proxy.getVariableValue("factoryParameters");
                    for (Adapter adapter : model.getAdapters()) {
                        if (adapter.getMode() == Adapter.Mode.TEMPLATE) {
                            continue;
                        }
                        if (!OmiscidAdapterCreator.canGetParametersAsString(adapter)) {
                            shouldRetry = true;
                            continue;
                        }
                        if (targetId.equals(adapter.getPossibleServiceProxy().getPeerIdAsString()) && targetParameters.equals(OmiscidAdapterCreator.getParametersAsString(adapter))) {
                            adapter.setMode(Adapter.Mode.PRESENT);
                            adapter.getProvided().setPossibleServiceProxy(proxy);
                        }
                    }
                } catch (UnknownVariable ex) {
                    // service is gone
                }
            }
        }*/
    }

    private void changePresentAdaptersToProposed() {
        for (Adapter adapter : model.getAdapters()) {
            if (adapter.getMode() == Adapter.Mode.PRESENT) {
                adapter.setMode(Adapter.Mode.PROPOSED);
                adapter.getProvided().setPossibleServiceProxy(null);
            }
        }
    }
    private void possiblyMakeAdapterPresent(ServiceProxy proxy) {
        String targetId = proxy.getVariableValue("factoryId");
        String targetParameters = proxy.getVariableValue("factoryParameters");
//        int remainingTries = 10;
//        while (remainingTries > 0) {
//            try {
                for (Adapter adapter : model.getAdapters()) {
                    if (adapter.getMode() == Adapter.Mode.TEMPLATE) {
                        continue;
                    }
                    System.err.println("... "+targetId);
                    System.err.println("... "+targetParameters);
                    if (targetId.equals(adapter.getPossibleServiceProxy().getPeerIdAsString()) && targetParameters.equals(OmiscidAdapterCreator.getParametersAsString(adapter))) {
                        adapter.setMode(Adapter.Mode.PRESENT);
                        adapter.getProvided().setPossibleServiceProxy(proxy);
                    }
                }
//                remainingTries = 0;
//            } catch (UnsupportedOperationException ex) {
//                remainingTries--;
//            }
//        }
    }

    private void addAdapterTemplateFor(ServiceProxy proxy) {
        if (omiscidAdapters.containsKey(proxy)) {
            throw new IllegalArgumentException();
        }
        omiscidAdapters.put(proxy, new ArrayList<Adapter>());
        Adapter adapter = new Adapter(
                Adapter.Mode.TEMPLATE,
                new RequiredSlot(readServiceSpecification(proxy.getVariableValue("from")), null),
                new ProvidedSlot(readServiceSpecification(proxy.getVariableValue("to")), null, null),
                proxy
                );
        adapter.getProvided().setPossibleParentAdapter(adapter);
        if (proxy.getVariables().contains("parameters")) {
            for (String line : proxy.getVariableValue("parameters").split(" *\\n *")) {
                if (line.length() > 0) {
                    Matcher m = Pattern.compile("^([^ ]+) *: *([^ ]+) *([^ =]+) *(= *(.*))?$").matcher(line);
                    if (m.matches()) {
                        SpecialValue spe = null;
                        String def = null;
                        if (m.group(4) != null) {
                            if (m.group(5).matches("^#\\(.*\\)$")) {
                                String[] params = m.group(5).replaceFirst("^#\\((.*)\\)$", "$1").split(" +");
                                spe = new SpecialValue(Arrays.asList(params));
                            } else {
                                def = m.group(5);
                            }
                        }
                        Parameter p = new Parameter(m.group(1), m.group(2), m.group(3), def, spe, null);
                        adapter.addToParameters(p);
                    }
                }
            }
        }
        omiscidAdapters.get(proxy).add(adapter);
        model.addToAdapters(adapter);
    }

    private void removeAdapterTemplateFor(ServiceProxy proxy) {
        List<Adapter> content = omiscidAdapters.remove(proxy);
        for (Adapter adap : content) {
            model.removeFromAdapters(adap);
        }
    }

    private void addProviderFor(ServiceProxy proxy) {
        if (omiscidProviders.containsKey(proxy)) {
            throw new IllegalArgumentException();
        }
        omiscidProviders.put(proxy, new ArrayList<ProvidedSlot>());
        for (String line : proxy.getVariableValue("provides").split(" *\n *")) {
            if (line.length() == 0) {
                continue;
            }
            ProvidedSlot prov = new ProvidedSlot(readServiceSpecification(line), null, proxy);
            omiscidProviders.get(proxy).add(prov);
            model.addToProviders(prov);
        }
    }

    private void removeProviderFor(ServiceProxy proxy) {
        List<ProvidedSlot> content = omiscidProviders.remove(proxy);
        for (ProvidedSlot prov : content) {
            model.removeFromProviders(prov);
        }
    }

    private void addRequirementsFor(ServiceProxy proxy) {
        if (omiscidRequirements.containsKey(proxy)) {
            throw new IllegalArgumentException();
        }
        omiscidRequirements.put(proxy, new ArrayList<Requirement>());
        for (String line : proxy.getVariableValue("requires").split(" *\n *")) {
            if (line.length() == 0) {
                continue;
            }
            ServiceSpecification serviceSpecification = readServiceSpecification(line);
            Requirement req = new Requirement(serviceSpecification, new RequiredSlot(serviceSpecification, null), proxy);
            omiscidRequirements.get(proxy).add(req);
            model.addToRequirements(req);
        }
    }

    private void removeRequirementsFor(ServiceProxy proxy) {
        List<Requirement> content = omiscidRequirements.remove(proxy);
        for (Requirement req : content) {
            model.removeFromRequirements(req);
        }
    }


}
