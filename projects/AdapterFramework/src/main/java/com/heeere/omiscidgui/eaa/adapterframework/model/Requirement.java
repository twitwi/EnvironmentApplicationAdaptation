// Autogenerated, edition is futile
package com.heeere.omiscidgui.eaa.adapterframework.model;

public class Requirement {

    protected java.beans.PropertyChangeSupport ___pcs = new java.beans.PropertyChangeSupport(this);
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {___pcs.addPropertyChangeListener(l);}
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {___pcs.removePropertyChangeListener(l);}
// required won't have any fullfilledBy value
    public Requirement(ServiceSpecification specification, RequiredSlot required, java.util.List<ExpectedRequirementAutoLink> expectedLinks, fr.prima.omiscid.user.service.ServiceProxy requiringServiceProxy) {
        this.specification = specification;
        this.required = required;
        this.expectedLinks = expectedLinks;
        this.requiringServiceProxy = requiringServiceProxy;
    }

    public Requirement(ServiceSpecification specification, RequiredSlot required, fr.prima.omiscid.user.service.ServiceProxy requiringServiceProxy) {
        this.specification = specification;
        this.required = required;
        this.expectedLinks = CollectionFactory.list();
        this.requiringServiceProxy = requiringServiceProxy;
    }

    private ServiceSpecification specification;
    public ServiceSpecification getSpecification() {
        return specification;
    }
    public void setSpecification(ServiceSpecification specification) {
        ServiceSpecification __old = this.specification;
        this.specification = specification;
        ___pcs.firePropertyChange("specification", __old, this.specification);
    }

    private RequiredSlot required;
    public RequiredSlot getRequired() {
        return required;
    }
    public void setRequired(RequiredSlot required) {
        RequiredSlot __old = this.required;
        this.required = required;
        ___pcs.firePropertyChange("required", __old, this.required);
    }

    private java.util.List<ExpectedRequirementAutoLink> expectedLinks;
    public java.util.List<ExpectedRequirementAutoLink> getExpectedLinks() {
        return expectedLinks;
    }
    public void setExpectedLinks(java.util.List<ExpectedRequirementAutoLink> expectedLinks) {
        java.util.List<ExpectedRequirementAutoLink> __old = this.expectedLinks;
        this.expectedLinks = expectedLinks;
        ___pcs.firePropertyChange("expectedLinks", __old, this.expectedLinks);
    }
    public void addToExpectedLinks(ExpectedRequirementAutoLink o) {
        this.expectedLinks.add(o);
    }
    public boolean removeFromExpectedLinks(ExpectedRequirementAutoLink o) {
        return this.expectedLinks.remove(o);
    }

    private fr.prima.omiscid.user.service.ServiceProxy requiringServiceProxy;
    public fr.prima.omiscid.user.service.ServiceProxy getRequiringServiceProxy() {
        return requiringServiceProxy;
    }
    public void setRequiringServiceProxy(fr.prima.omiscid.user.service.ServiceProxy requiringServiceProxy) {
        fr.prima.omiscid.user.service.ServiceProxy __old = this.requiringServiceProxy;
        this.requiringServiceProxy = requiringServiceProxy;
        ___pcs.firePropertyChange("requiringServiceProxy", __old, this.requiringServiceProxy);
    }

    public String deepToString(String indent, String delta, int maxDepth) {
        if (maxDepth <= 0) { return "!!!MAX!!!\n";}
        StringBuilder res = new StringBuilder();
        String nextIndent = indent + delta;
        String nextNextIndent = nextIndent + delta;
        res.append("Requirement {\n");
        res.append(nextIndent).append("specification: ");
        if (null == specification) {
            res.append("null\n");
        } else {
            res.append(specification.deepToString(nextIndent, delta, maxDepth-1));
        }
        res.append(nextIndent).append("required: ");
        if (null == required) {
            res.append("null\n");
        } else {
            res.append(required.deepToString(nextIndent, delta, maxDepth-1));
        }
        res.append(nextIndent).append("expectedLinks: ");
        res.append("[\n");
        for (ExpectedRequirementAutoLink ___o : expectedLinks) res.append(nextNextIndent).append(___o.deepToString(nextNextIndent, delta, maxDepth-1));
        res.append(nextIndent).append("]\n");
        res.append(nextIndent).append("requiringServiceProxy: ");
        res.append(requiringServiceProxy).append("\n");
        res.append(indent).append("} // Requirement\n");
        return res.toString();
    }

    public Requirement smartDeepCopy() {
        java.util.HashMap<Object,Object> conversion = new java.util.HashMap<Object,Object>();
        return smartDeepCopyInto(conversion);
    }
    /*package*/ Requirement smartDeepCopyInto(java.util.HashMap<Object,Object> conversion) {
        if (conversion.containsKey(this)) return (Requirement) conversion.get(this);
        Requirement ___res = new Requirement();
        conversion.put(this, ___res);
        if (null == specification) {
            ___res.specification = null;
        } else {
            ___res.specification = specification.smartDeepCopyInto(conversion);
        }
        if (null == required) {
            ___res.required = null;
        } else {
            ___res.required = required.smartDeepCopyInto(conversion);
        }
        for (ExpectedRequirementAutoLink ___expectedLinks : expectedLinks) {
            if (___expectedLinks == null) {
                ___res.addToExpectedLinks(null);
            } else {
                ___res.addToExpectedLinks(___expectedLinks.smartDeepCopyInto(conversion));
            }
        }
        ___res.requiringServiceProxy = requiringServiceProxy;
        return ___res;
    }
    /*package*/ Requirement() {
        this.specification = null;
        this.required = null;
        this.expectedLinks = new java.util.Vector<ExpectedRequirementAutoLink>();
        this.requiringServiceProxy = null;
    }

}