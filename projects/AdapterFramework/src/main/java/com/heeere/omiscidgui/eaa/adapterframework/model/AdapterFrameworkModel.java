// Autogenerated, edition is futile
package com.heeere.omiscidgui.eaa.adapterframework.model;

public class AdapterFrameworkModel {

    protected java.beans.PropertyChangeSupport ___pcs = new java.beans.PropertyChangeSupport(this);
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {___pcs.addPropertyChangeListener(l);}
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {___pcs.removePropertyChangeListener(l);}
    public AdapterFrameworkModel(java.util.List<Adapter> adapters, java.util.List<Requirement> requirements, java.util.List<ProvidedSlot> providers) {
        this.adapters = adapters;
        this.requirements = requirements;
        this.providers = providers;
    }

    public AdapterFrameworkModel() {
        this.adapters = CollectionFactory.list();
        this.requirements = CollectionFactory.list();
        this.providers = CollectionFactory.list();
    }

    private java.util.List<Adapter> adapters;
    public java.util.List<Adapter> getAdapters() {
        return adapters;
    }
    public void setAdapters(java.util.List<Adapter> adapters) {
        java.util.List<Adapter> __old = this.adapters;
        this.adapters = adapters;
        ___pcs.firePropertyChange("adapters", __old, this.adapters);
    }
    public void addToAdapters(Adapter o) {
        this.adapters.add(o);
    }
    public boolean removeFromAdapters(Adapter o) {
        return this.adapters.remove(o);
    }

    private java.util.List<Requirement> requirements;
    public java.util.List<Requirement> getRequirements() {
        return requirements;
    }
    public void setRequirements(java.util.List<Requirement> requirements) {
        java.util.List<Requirement> __old = this.requirements;
        this.requirements = requirements;
        ___pcs.firePropertyChange("requirements", __old, this.requirements);
    }
    public void addToRequirements(Requirement o) {
        this.requirements.add(o);
    }
    public boolean removeFromRequirements(Requirement o) {
        return this.requirements.remove(o);
    }

    private java.util.List<ProvidedSlot> providers;
    public java.util.List<ProvidedSlot> getProviders() {
        return providers;
    }
    public void setProviders(java.util.List<ProvidedSlot> providers) {
        java.util.List<ProvidedSlot> __old = this.providers;
        this.providers = providers;
        ___pcs.firePropertyChange("providers", __old, this.providers);
    }
    public void addToProviders(ProvidedSlot o) {
        this.providers.add(o);
    }
    public boolean removeFromProviders(ProvidedSlot o) {
        return this.providers.remove(o);
    }

    public String deepToString(String indent, String delta, int maxDepth) {
        if (maxDepth <= 0) { return "!!!MAX!!!\n";}
        StringBuilder res = new StringBuilder();
        String nextIndent = indent + delta;
        String nextNextIndent = nextIndent + delta;
        res.append("AdapterFrameworkModel {\n");
        res.append(nextIndent).append("adapters: ");
        res.append("[\n");
        for (Adapter ___o : adapters) res.append(nextNextIndent).append(___o.deepToString(nextNextIndent, delta, maxDepth-1));
        res.append(nextIndent).append("]\n");
        res.append(nextIndent).append("requirements: ");
        res.append("[\n");
        for (Requirement ___o : requirements) res.append(nextNextIndent).append(___o.deepToString(nextNextIndent, delta, maxDepth-1));
        res.append(nextIndent).append("]\n");
        res.append(nextIndent).append("providers: ");
        res.append("[\n");
        for (ProvidedSlot ___o : providers) res.append(nextNextIndent).append(___o.deepToString(nextNextIndent, delta, maxDepth-1));
        res.append(nextIndent).append("]\n");
        res.append(indent).append("} // AdapterFrameworkModel\n");
        return res.toString();
    }

    public AdapterFrameworkModel smartDeepCopy() {
        java.util.HashMap<Object,Object> conversion = new java.util.HashMap<Object,Object>();
        return smartDeepCopyInto(conversion);
    }
    /*package*/ AdapterFrameworkModel smartDeepCopyInto(java.util.HashMap<Object,Object> conversion) {
        if (conversion.containsKey(this)) return (AdapterFrameworkModel) conversion.get(this);
        AdapterFrameworkModel ___res = new AdapterFrameworkModel();
        conversion.put(this, ___res);
        for (Adapter ___adapters : adapters) {
            if (___adapters == null) {
                ___res.addToAdapters(null);
            } else {
                ___res.addToAdapters(___adapters.smartDeepCopyInto(conversion));
            }
        }
        for (Requirement ___requirements : requirements) {
            if (___requirements == null) {
                ___res.addToRequirements(null);
            } else {
                ___res.addToRequirements(___requirements.smartDeepCopyInto(conversion));
            }
        }
        for (ProvidedSlot ___providers : providers) {
            if (___providers == null) {
                ___res.addToProviders(null);
            } else {
                ___res.addToProviders(___providers.smartDeepCopyInto(conversion));
            }
        }
        return ___res;
    }
}
