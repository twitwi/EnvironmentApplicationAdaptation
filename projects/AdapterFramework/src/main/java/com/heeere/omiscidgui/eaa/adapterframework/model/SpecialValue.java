// Autogenerated, edition is futile
package com.heeere.omiscidgui.eaa.adapterframework.model;

public class SpecialValue {

    protected java.beans.PropertyChangeSupport ___pcs = new java.beans.PropertyChangeSupport(this);
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {___pcs.addPropertyChangeListener(l);}
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {___pcs.removePropertyChangeListener(l);}
    public SpecialValue(java.util.List<String> parameters) {
        this.parameters = parameters;
    }

    public SpecialValue() {
        this.parameters = CollectionFactory.list();
    }

    private java.util.List<String> parameters;
    public java.util.List<String> getParameters() {
        return parameters;
    }
    public void setParameters(java.util.List<String> parameters) {
        java.util.List<String> __old = this.parameters;
        this.parameters = parameters;
        ___pcs.firePropertyChange("parameters", __old, this.parameters);
    }
    public void addToParameters(String o) {
        this.parameters.add(o);
    }
    public boolean removeFromParameters(String o) {
        return this.parameters.remove(o);
    }

    public String deepToString(String indent, String delta, int maxDepth) {
        if (maxDepth <= 0) { return "!!!MAX!!!\n";}
        StringBuilder res = new StringBuilder();
        String nextIndent = indent + delta;
        String nextNextIndent = nextIndent + delta;
        res.append("SpecialValue {\n");
        res.append(nextIndent).append("parameters: ");
        res.append(parameters.toString()).append("\n");
        res.append(indent).append("} // SpecialValue\n");
        return res.toString();
    }

    public SpecialValue smartDeepCopy() {
        java.util.HashMap<Object,Object> conversion = new java.util.HashMap<Object,Object>();
        return smartDeepCopyInto(conversion);
    }
    /*package*/ SpecialValue smartDeepCopyInto(java.util.HashMap<Object,Object> conversion) {
        if (conversion.containsKey(this)) return (SpecialValue) conversion.get(this);
        SpecialValue ___res = new SpecialValue();
        conversion.put(this, ___res);
        for (String ___parameters : parameters) {
            ___res.addToParameters(___parameters);
        }
        return ___res;
    }
}
