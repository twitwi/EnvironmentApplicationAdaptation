/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework.visual;

import com.heeere.omiscidgui.eaa.adapterframework.BindingPanelTopComponent;
import com.heeere.omiscidgui.eaa.adapterframework.model.Adapter;
import com.heeere.omiscidgui.eaa.adapterframework.model.AdapterFrameworkModel;
import com.heeere.omiscidgui.eaa.adapterframework.model.ExpectedRequirementAutoLink;
import com.heeere.omiscidgui.eaa.adapterframework.model.Parameter;
import com.heeere.omiscidgui.eaa.adapterframework.model.ProvidedSlot;
import com.heeere.omiscidgui.eaa.adapterframework.model.RequiredSlot;
import com.heeere.omiscidgui.eaa.adapterframework.model.Requirement;
import com.heeere.omiscidgui.eaa.adapterframework.visual.ProposalGraphScene.Pin;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.JScrollPane;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutFactory;
import org.netbeans.api.visual.graph.layout.GraphLayoutSupport;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.NbBundle;

/**
 *
 * @author twilight
 */
public class ProposalGraphScene extends GraphPinScene<Object, String, Pin> {

    private static int edgeId = 0;
    private String edge() {
        return "edge " + edgeId++;
    }
    private String dottedEdge() {
        return "dotted edge " + edgeId++;
    }
    private String invisibleEdge() {
        return "invisible edge " + edgeId++;
    }

    public static class Pin {
        Object base;
        RequiredSlot required;
        ProvidedSlot provided;

        private Pin(Object base, RequiredSlot required, ProvidedSlot provided) {
            this.base = base;
            this.required = required;
            this.provided = provided;
        }
        private static Pin provided(Object o, ProvidedSlot pro) {
            return new Pin(o, null, pro);
        }
        private static Pin required(Object o, RequiredSlot req) {
            return new Pin(o, req, null);
        }
        private static Pin required(Object o, Requirement req) {
            return new Pin(o, req.getRequired(), null);
        }
        @Override
        public int hashCode() {
            return base.hashCode();
        }
        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof Pin &&
                    eq(base, ((Pin) obj).base) &&
                    eq(required, ((Pin) obj).required) &&
                    eq(provided, ((Pin) obj).provided);
        }

        private boolean eq(Object o1, Object o2) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }
    }

    private PropertyEditorSetter propertyEditorSetter;
    public void setPropertyEditorSetter(PropertyEditorSetter propertyEditorSetter) {
        this.propertyEditorSetter = propertyEditorSetter;
    }

    private JScrollPane scrollPane;
    private AdapterFrameworkModel model;
    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    private Object rootNode = "ROOT";
    private Object antirootNode = "ANTIROOT";
    private Pin rootPin = new Pin(rootNode, null, null);
    private Pin antirootPin = new Pin(antirootNode, null, null);

    public ProposalGraphScene(JScrollPane scrollPane, final AdapterFrameworkModel model) {
        this.scrollPane = scrollPane;
        this.model = model;
        this.mainLayer =  new LayerWidget(this);
        this.connectionLayer =  new LayerWidget(this);
        importModel();
        addChild(this.mainLayer);
        addChild(this.connectionLayer);
        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createWheelPanAction());


        //final TreeGraphLayout<Object, String> graphLayout = new TreeGraphLayout<Object, String>(this, 0, 0, 100, 100, false);
        //final GraphLayout<Object, String> graphLayout = new GridGraphLayout<Object, String>();
        //*
        final GraphLayout<Object, String> graphLayout = GraphLayoutFactory.createTreeGraphLayout(0, 0, 7, 20, false);
        //GraphLayoutSupport.setTreeGraphLayoutRootNode(graphLayout, model.getProviders().get(0));
        GraphLayoutSupport.setTreeGraphLayoutRootNode(graphLayout, rootNode);
         /*/
        final GraphLayout<Object, String> graphLayout = new GridGraphLayout<Object, String>();
        //*/
        final SceneLayout sceneGraphLayout = LayoutFactory.createSceneGraphLayout(this, graphLayout);
        graphLayout.setAnimated(false);
        sceneGraphLayout.invokeLayout();
        installAutomaticZoomFit(1000);
    }
    private void installAutomaticZoomFit(long delay) {
        final long timeout = System.currentTimeMillis() + delay;
        this.addSceneListener(new SceneListener() {

            public void sceneRepaint() {
            }

            public void sceneValidating() {
            }

            public void sceneValidated() {
                if (System.currentTimeMillis() - timeout < 0) {
                    automaticZoomFit();
                } else {
                    ProposalGraphScene.this.removeSceneListener(this);
                }
            }
        });
    }
    private void automaticZoomFit() {
        Rectangle rectangle = null;
        for (Widget widget : mainLayer.getChildren()) {
            if (rectangle == null) {
                rectangle = widget.convertLocalToScene(widget.getBounds());
            } else {
                rectangle = rectangle.union(widget.convertLocalToScene(widget.getBounds()));
            }
        }
        Dimension dim = rectangle.getSize();
        Dimension viewDim = scrollPane.getViewportBorderBounds().getSize();
        //float newZoomFactor = Math.min((float) viewDim.width / dim.width, (float) viewDim.height / dim.height);
        float newZoomFactor = (float) viewDim.width / dim.width;
        if (this.getZoomFactor() != newZoomFactor) {
            this.setZoomFactor(newZoomFactor);
        }
    }

    private void importModel() {
        {
            addNode(rootNode);
            addPin(rootNode, rootPin);
            addNode(antirootNode);
            addPin(antirootNode, antirootPin);
        }
        for (ProvidedSlot pro : model.getProviders()) {
            addNode(pro);
            Pin pin = Pin.provided(pro, pro);
            addPin(pro, pin);
            {
                String e = invisibleEdge();
                addEdge(e);
                setEdgeSource(e, rootPin);
                setEdgeTarget(e, pin);
            }
        }
        for (Adapter adapter : model.getAdapters()) {
            if (adapter.getMode() == Adapter.Mode.PROPOSED) {
                addNode(adapter);
                addPin(adapter, Pin.required(adapter, adapter.getRequired()));
                addPin(adapter, Pin.provided(adapter, adapter.getProvided()));
                String edge = edge();
                addEdge(edge);
                ProvidedSlot provider = adapter.getRequired().getFullfilledBy();
                Object base = provider.getPossibleParentAdapter() != null ? provider.getPossibleParentAdapter() : provider;
                setEdgeSource(edge, Pin.provided(base, provider));
                setEdgeTarget(edge, Pin.required(adapter, adapter.getRequired()));
            }
        }
        for (Requirement req : model.getRequirements()) {
            Pin pin = Pin.required(req, req);
            addNode(req);
            addPin(req, pin);
            {
                String e = invisibleEdge();
                addEdge(e);
                setEdgeSource(e, pin);
                setEdgeTarget(e, antirootPin);
            }
            {
                String e = invisibleEdge();
                addEdge(e);
                setEdgeSource(e, rootPin);
                setEdgeTarget(e, pin);
            }
            for (ExpectedRequirementAutoLink link : req.getExpectedLinks()) {
                String edge = dottedEdge();
                addEdge(edge);
                ProvidedSlot provider = link.getFrom();
                Object base = provider.getPossibleParentAdapter() != null ? provider.getPossibleParentAdapter() : provider;
                setEdgeSource(edge, Pin.provided(base, provider));
                setEdgeTarget(edge, pin);
            }
        }
    }

    @Override
    protected Widget attachNodeWidget(Object node) {
        if (node == rootNode) {
            Widget rootWidget = new LabelWidget(this);
            mainLayer.addChild(rootWidget);
            return rootWidget;
        }
        if (node == antirootNode) {
            Widget antirootWidget = new LabelWidget(this);
            mainLayer.addChild(antirootWidget);
            return antirootWidget;
        }
        Widget group;
        if (node instanceof Adapter) {
            group = Widgets.adapter(this, (Adapter) node);
        } else {
            group = new IconNodeWidget(this, IconNodeWidget.TextOrientation.RIGHT_CENTER);
        }
        group.getActions().addAction(ActionFactory.createMoveAction());
        group.getActions().addAction(new org.netbeans.api.visual.action.WidgetAction.Adapter() {

            @Override
            public State mouseClicked(Widget widget, WidgetMouseEvent event) {
                selectWidget(widget);
                return super.mouseClicked(widget, event);
            }

        });
        mainLayer.addChild(group);
        return group;
    }

    private void selectWidget(Widget widget) {
        if (propertyEditorSetter != null) {
            Node toSet = null;
            Object o = findObject(widget);
            if (o instanceof Adapter) {
                Adapter a = (Adapter) o;
                if (a.getMode() != Adapter.Mode.IN_CREATION && a.getParameters() != null) {
                    final Property[] properties = new Property[a.getParameters().size()];
                    int i = 0;
                    for (final Parameter p : a.getParameters()) {
                        properties[i] = new Property<String>(String.class) {

                            @Override
                            public String getName() {
                                return p.getName();
                            }

                            @Override
                            public boolean canRead() {
                                return true;
                            }
                            @Override
                            public String getValue() throws IllegalAccessException, InvocationTargetException {
                                return p.getPossibleAffectedValue();
                            }
                            @Override
                            public boolean canWrite() {
                                return p.getSpecialValue() == null;
                            }
                            @Override
                            public void setValue(String newValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                                p.setPossibleAffectedValue(newValue);
                            }
                        };
                        i++;
                    }
                    String propertySetName = NbBundle.getMessage(BindingPanelTopComponent.class, "CTL_DefaultPropertySetName");
                    String propertySetDescription = NbBundle.getMessage(BindingPanelTopComponent.class, "CTL_DefaultPropertySetDescription");
                    final PropertySet pSet = new PropertySet("simple", propertySetName, propertySetDescription) {
                        @Override
                        public Property<?>[] getProperties() {
                            return properties.clone();
                        }
                    };
                    final PropertySet[] propertySets = new PropertySet[] {pSet};

                    Node parameters = new AbstractNode(Children.LEAF) {
                        @Override
                        public PropertySet[] getPropertySets() {
                            return propertySets;
                        }
                    };
                    toSet = parameters;
                }
            }
            propertyEditorSetter.setNodeToDisplayInProperties(toSet);
        }

    }

    private <T> T cast(Object o) {
        return (T) o;
    }

    @Override
    protected Widget attachPinWidget(final Object node, final Pin pin) {
        if (Arrays.asList(rootPin, antirootPin).contains(pin)) {
            Widget res = new LabelWidget(this);
            findWidget(node).addChild(res);
            return res;
        }
        if (node instanceof Adapter) {
            Adapter adapter = cast(node);
            Widget widget;
            if (pin.required != null) {
                widget = Widgets.required(this, adapter.getRequired());
            } else {
                findWidget(node).addChild(Widgets.adapterBox(this, adapter));
                widget = Widgets.provided(this, adapter.getProvided());
            }
            findWidget(node).addChild(widget);
            return widget;
        }
        if (node instanceof ProvidedSlot) {
            ProvidedSlot pro = cast(node);
            Widget widget = Widgets.provided(this, pro);
            findWidget(node).addChild(widget);
            return widget;
        }
        if (node instanceof Requirement) {
            Requirement req = cast(node);
            Widget widget = Widgets.required(this, req);
            findWidget(node).addChild(widget);
            return widget;
        }
        return null;
    }

    @Override
    protected Widget attachEdgeWidget(String edgeId) {
        ConnectionWidget widget = new ConnectionWidget(this);
        //widget.setLineColor(Color.BLACK);
        BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
        if (edgeId.contains("dotted ")) {
            stroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_BUTT, stroke.getLineJoin(), stroke.getMiterLimit(), new float[]{10f, 5f}, 0);
        }
        if (edgeId.contains("invisible ")) {
            widget.setVisible(false);
        }
        widget.setStroke(stroke);
        widget.setSourceAnchorShape(AnchorShape.NONE);
        widget.setTargetAnchorShape(AnchorShape.NONE);
        //widget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        connectionLayer.addChild(widget);
        widget.bringToBack();
        return widget;
    }
    
    protected void attachEdgeSourceAnchor(String edge, Pin oldSourceNode, Pin sourceNode) {
        ConnectionWidget edgeWidget = (ConnectionWidget) findWidget(edge);
        Widget sourceNodeWidget = findWidget(sourceNode);
        Anchor sourceAnchor = AnchorFactory.createCircularAnchor(sourceNodeWidget, 7);
        edgeWidget.setSourceAnchor(sourceAnchor);
    }

    protected void attachEdgeTargetAnchor(String edge, Pin oldTargetNode, Pin targetNode) {
        ConnectionWidget edgeWidget = (ConnectionWidget) findWidget(edge);
        Widget targetNodeWidget = findWidget(targetNode);
        Anchor targetAnchor = AnchorFactory.createCenterAnchor(targetNodeWidget);
        edgeWidget.setTargetAnchor(targetAnchor);
    }

}
