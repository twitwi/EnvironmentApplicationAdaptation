/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.heeere.omiscidgui.eaa.adapterframework.visual;

import fr.prima.omiscid.user.service.ServiceProxy;
import com.heeere.omiscidgui.eaa.adapterframework.BindingPanelTopComponent;
import com.heeere.omiscidgui.eaa.adapterframework.OmiscidAdapterCreator;
import com.heeere.omiscidgui.eaa.adapterframework.model.Adapter;
import com.heeere.omiscidgui.eaa.adapterframework.model.ProvidedSlot;
import com.heeere.omiscidgui.eaa.adapterframework.model.RequiredSlot;
import com.heeere.omiscidgui.eaa.adapterframework.model.Requirement;
import com.heeere.omiscidgui.eaa.adapterframework.model.ServiceProperty;
import com.heeere.omiscidgui.eaa.adapterframework.model.ServiceSpecification;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;

/**
 *
 * @author twilight
 */
public class Widgets {

    public static ArrayList<Color> colors = new ArrayList<Color>() {
        {
            add(Color.RED);
            add(Color.GREEN);
            add(Color.BLUE);
            //add(Color.YELLOW); // too close to ORANGE
            add(Color.CYAN);
            add(Color.MAGENTA);
            add(Color.GRAY);
            add(Color.WHITE);
            add(Color.ORANGE);
            add(Color.PINK);
            Collections.shuffle(this);
        }
    };
    private static Map<String, Color> publishedColors = new HashMap<String, Color>();

    public static Color colorFor(ServiceSpecification s) {
        String key = s.getName();
        Color res = publishedColors.get(key);
        if (res == null) {
            res = colors.get(publishedColors.size() % colors.size());
            publishedColors.put(key, res);
        }
        return res;
    }

    private static Area requiredShape;
    private static final int requirementHalfSize = 14;
    static {
        int hs = requirementHalfSize;
        int innerTopLeft = requirementHalfSize - 8;
        int innerSize = 2 * (requirementHalfSize - innerTopLeft);
        requiredShape = new Area(new Ellipse2D.Double(1, 1, 2 * hs - 2, 2 * hs - 2));
        requiredShape.subtract(new Area(new Ellipse2D.Double(innerTopLeft, innerTopLeft, innerSize, innerSize)));
        requiredShape.subtract(new Area(new Rectangle2D.Double(0, 0, hs, 2 * hs)));
    }
    public static Widget required(Scene scene, final Color color, final String shortDescription) {
        return new Widget(scene) {
            @Override
            protected Rectangle calculateClientArea() {
                return new Rectangle(new Dimension(2 * requirementHalfSize, 2 * requirementHalfSize + 16));
            }
            @Override
            protected void paintWidget() {
                Graphics2D g = getGraphics();
                g.translate(0, 8f);
                g.setColor(color);
                g.fill(requiredShape);
                g.setColor(Color.BLACK);
                g.draw(requiredShape);
                float fontSize = 30.f / shortDescription.length();
                g.setFont(g.getFont().deriveFont(fontSize));
                g.drawString(shortDescription, 0.f, 2 * requirementHalfSize + 1.f + fontSize);
                g.translate(0, -8f);
            }
        };
    }

    private static Shape providedShape = new Ellipse2D.Double(1, 1, 14, 14);
    public static Widget provided(Scene scene, final Color color, final String shortDescription) {
        return new Widget(scene) {
            @Override
            protected Rectangle calculateClientArea() {
                return new Rectangle(16, 16 + 16);
            }
            @Override
            protected void paintWidget() {
                Graphics2D g = getGraphics();
                g.translate(0, 8f);
                g.setColor(color);
                g.fill(providedShape);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(2));
                g.draw(providedShape);
                float fontSize = 30.f / shortDescription.length();
                g.setFont(g.getFont().deriveFont(fontSize));
                g.drawString(shortDescription, 0.f, 17.f + fontSize);
                g.translate(0, -8f);
            }
        };
    }

    private static final int adapterBoxFullSize = 32;
    private static final int adapterBoxBoxSize = 16;
    private static final int adapterBoxBoxHeight = 16;
    private static Area adapterBoxShape;
    static {
        float delta = (adapterBoxFullSize - adapterBoxBoxSize - 2) / 2.f;
        adapterBoxShape = new Area(new Rectangle2D.Double(delta, 1, adapterBoxBoxSize, adapterBoxBoxHeight - 2));
        adapterBoxShape.add(new Area(new Rectangle2D.Double(1, adapterBoxBoxHeight / 2.f - 1.f, adapterBoxFullSize - 2, 2.f)));
    }
    public static Widget adapterBox(ProposalGraphScene scene, final Adapter adapter) {
        final Color c1 = Widgets.colorFor(adapter.getRequired().getSpecification());
        final Color c2 = Widgets.colorFor(adapter.getProvided().getSpecification());
        return new Widget(scene) {
            @Override
            protected Rectangle calculateClientArea() {
                return new Rectangle(new Dimension(adapterBoxFullSize, adapterBoxBoxHeight));
            }
            @Override
            protected void paintWidget() {
                Graphics2D g = getGraphics();
                GradientPaint gr = new GradientPaint(0, 0, c1, adapterBoxFullSize, 0, c2);
                g.setPaint(gr);
                g.fill(adapterBoxShape);
                g.setColor(Color.BLACK);
                g.setStroke(new BasicStroke(1));
                g.draw(adapterBoxShape);
            }
        };
    }
    private static EnumMap<Adapter.Mode, Float> opacity = new EnumMap<Adapter.Mode, Float>(Adapter.Mode.class) {
        {
            put(Adapter.Mode.PROPOSED, .35f);
            put(Adapter.Mode.IN_CREATION, .2f);
            put(Adapter.Mode.PRESENT, 1.f);
        }
    };
    public static Widget adapter(ProposalGraphScene scene, final Adapter adapter) {
        Widget result = new IconNodeWidget(scene, IconNodeWidget.TextOrientation.RIGHT_CENTER) {
            {
                adapter.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        revalidate();
                        getScene().validate();
                    }
                });
            }
            @Override
            protected void paintChildren() {
                float alpha = opacity.get(adapter.getMode()) == null ? .1f : opacity.get(adapter.getMode());
                Graphics2D g = getGraphics();
                Composite comp = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
                super.paintChildren();
                g.setComposite(comp);
            }
        };
        result.getActions().addAction(ActionFactory.createEditAction(new EditProvider() {
            public void edit(final Widget widget) {
                // on double-click
                OmiscidAdapterCreator.createServiceFor(adapter, BindingPanelTopComponent.getDefault().getServiceClient());
            }
        }));
        return result;
    }

    public static Widget provided(ProposalGraphScene scene, ProvidedSlot provided) {
        ServiceSpecification specification = provided.getSpecification();
        String shortDescription = shortDescription(provided.getSpecification(), provided.getPossibleServiceProxy());
        Widget widget = Widgets.provided(scene, Widgets.colorFor(specification), shortDescription);
        String toolTip = "<html><b>" + specification.getName()+"</b>";
        toolTip += serviceDescription(provided.getPossibleServiceProxy());
        toolTip += "</html>";
        widget.setToolTipText(toolTip);
        return widget;
    }

    public static String shortDescription(ServiceSpecification specification, ServiceProxy proxy) {
        if (proxy != null && proxy.getVariables().contains("description")) {
            return proxy.getVariableValue("description");
        }
        return specification.getName();
    }
    public static String serviceDescription(ServiceProxy proxy) {
        if (proxy == null) {
            return "";
        }
        String result = "<br/><i>Service " + proxy.getName();
        if (proxy.getVariables().contains("description")) {
            result += "<br/>  (" + proxy.getVariableValue("description") + ")";
        }
        result += "</i>";
        return result;
    }
    public static Widget required(ProposalGraphScene scene, RequiredSlot required) {
        ServiceSpecification specification = required.getSpecification();
        String shortDescription = shortDescription(required.getSpecification(), null);
        Widget widget = Widgets.required(scene, Widgets.colorFor(specification), shortDescription);
        String toolTip = "<html><b>" + specification.getName()+"</b>";
        for (ServiceProperty p : specification.getServiceProperties()) {
            toolTip += "<br/>  " + p.getName() + " = " + p.getValue();
        }
        toolTip += "</html>";
        widget.setToolTipText(toolTip);
        return widget;
    }
    public static Widget required(ProposalGraphScene scene, Requirement required) {
        ServiceSpecification specification = required.getSpecification();
        String shortDescription = shortDescription(required.getSpecification(), required.getRequiringServiceProxy());
        Widget widget = Widgets.required(scene, Widgets.colorFor(specification), shortDescription);
        String toolTip = "<html><b>" + specification.getName()+"</b>";
        for (ServiceProperty p : specification.getServiceProperties()) {
            toolTip += "<br/>  " + p.getName() + " = " + p.getValue();
        }
        toolTip += serviceDescription(required.getRequiringServiceProxy());
        toolTip += "</html>";
        widget.setToolTipText(toolTip);
        return widget;
    }

}
