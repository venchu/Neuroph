package org.neuroph.netbeans.visual.widgets;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.neuroph.core.Connection;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.netbeans.visual.popup.MainPopupMenuProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Zoran Sevarac
 */
public class NeuralNetworkScene extends ObjectScene {
//http://bits.netbeans.org/dev/javadoc/org-netbeans-api-visual/org/netbeans/api/visual/widget/doc-files/documentation.html

    private LayerWidget mainLayer;
    private LayerWidget interractionLayer;
    private LayerWidget connectionLayer;
    private NeuralNetworkWidget neuralNetworkWidget;
    private NeuralNetwork neuralNetwork;
    private boolean showConnections = true;
    private boolean waitingClick = false;
    private boolean waitingLayerClick = false;
    // neurons and widgets bufferd index
    HashMap<Neuron, NeuronWidget> neuronsAndWidgets = new HashMap<Neuron, NeuronWidget>();
        InstanceContent content = new InstanceContent();
    AbstractLookup aLookup = new AbstractLookup(content);
    ArrayList<Neuron> neurons = new ArrayList<Neuron>();
    //ProxyLookup lookup = new ProxyLookup();
    

    boolean isFirstSelection = true;

 // ide modul neuron node (org.neuroph.netbeans.ide.explorer) 
    public NeuralNetworkScene(NeuralNetwork neuralNet) {

        this.neuralNetwork = neuralNet;
        neuralNetworkWidget = new NeuralNetworkWidget(this, neuralNet);
        neuralNetworkWidget.setPreferredLocation(new Point(100, 10));

        connectionLayer = new LayerWidget(this);    // draw connections
        interractionLayer = new LayerWidget(this); // draw connections while creating them
        mainLayer = new LayerWidget(this);

        mainLayer.addChild(neuralNetworkWidget);

        addChild(mainLayer);
        addChild(connectionLayer);
        addChild(interractionLayer);

        addObject(neuralNetwork, neuralNetworkWidget);

        getActions().addAction(ActionFactory.createPanAction());
        getActions().addAction(ActionFactory.createMouseCenteredZoomAction(1.1));
        getActions().addAction(ActionFactory.createPopupMenuAction(new MainPopupMenuProvider()));
        getActions().addAction(this.createSelectAction()); // to invert selection when network is clciked
        
        
        addObjectSceneListener(new ObjectSceneListener() {

            public void objectAdded(ObjectSceneEvent event, Object addedObject) {
         //       throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
           //     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
                // add selected objects to llokup somehow....

            }

            public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
//                for(Object o : previousSelection)
//                   content.remove(o);
                               
                for(Object o : newSelection)
                    content.add(o);  
                
                
            }

            public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
           //     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptProvider() {
            public ConnectorState isAcceptable(final Widget widget, final Point point, final Transferable t) {

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
//                        DataFlavor flavor = t.getTransferDataFlavors()[3];
//                Class droppedClass = flavor.getRepresentationClass();
                        Image dragImage = getImageFromTransferable(t);
                        JComponent view = widget.getScene().getView();

                        // Graphics2D graphics = (Graphics2D) view.getGraphics(); 
                        Graphics2D graphics = widget.getScene().getGraphics();

                        //Rectangle visRect = view.getVisibleRect();
                        //view.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
//                Image dragImage = null;
//                try {
//                    dragImage = (Image) t.getTransferData(DataFlavor.imageFlavor);
//                } catch (UnsupportedFlavorException ex) {
//                    Exceptions.printStackTrace(ex);
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                } 

                        graphics.drawImage(dragImage,
                                AffineTransform.getTranslateInstance(point.getLocation().getX(),
                                point.getLocation().getY()),
                                null);
                        // widget.getScene().paint(graphics);
                    }
                });


                return ConnectorState.REJECT;
            }

            public void accept(Widget widget, Point point, Transferable t) {
            }
        }));
    
    }

    @Override
    public Lookup getLookup() {               
       // return Lookups.fixed(neuralNetwork, getSelectedObjects().toArray());        
//        return new ProxyLookup(
//                         new Lookup[]{
//                               aLookup});                       
//        InstanceContent content = new InstanceContent();
//        content.add(neuralNetwork);
//        for(Object o : getSelectedObjects()) {
//            content.add(o);
//        }
//        
        return aLookup;
//        
    }
    
    
            
    private Image getImageFromTransferable(Transferable transferable) {
        Object o = null;
        try {
            o = transferable.getTransferData(DataFlavor.imageFlavor);
        } catch (IOException ex) {
        } catch (UnsupportedFlavorException ex) {
        }
        return o instanceof Image ? (Image) o : ImageUtilities.loadImage("org/netbeans/shapesample/palette/shape1.png");
    }

    public void resizeLayer(Widget w) {
        int i = w.getChildren().size();
        Dimension d = new Dimension(((int) (i * 65)) + 10, (int) w.getPreferredSize().getHeight());
        w.setPreferredSize(d);
    }

    // http://bits.netbeans.org/dev/javadoc/org-netbeans-api-visual/org/netbeans/api/visual/widget/doc-files/documentation.html#ValidationProcess
    public void refresh() {

        visualizeNetwork();

    }

    public void visualizeNetwork() {
        //clear layers (with neurons)
        neuralNetworkWidget.removeChildren();
        // clear connections
        connectionLayer.removeChildren();
         
        createNeuralLayers();
        if (showConnections) {
            createConnections();
        }
        this.validate(); // only one call to validate since they ar eusing same scene instance    
    }

    private void createNeuralLayers() {
        neurons = new ArrayList<Neuron>();
        neuronsAndWidgets = new HashMap<Neuron, NeuronWidget>();

        for (int i = 0; i < neuralNetwork.getLayersCount(); i++) { // iterate all layers in network
            IconNodeWidget layerWrapperWidget = new IconNodeWidget(this); // parent container layer for label and NeuralLayer
            layerWrapperWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
            Layer layer = neuralNetwork.getLayerAt(i); // get layer for this widget
            NeuralLayerWidget neuralLayerWidget = new NeuralLayerWidget(this, layer); // create widget for layer
            
            if (!getObjects().contains(layer))
                    addObject(layer, neuralLayerWidget);
           
            
//            if (selection instanceof NeuralLayerWidget) { // if this neurn was selected before refresh...
//                if (((NeuralLayerWidget) selection).getLayer() == layer) {
//                    //setSelection(neuron.getLabel() != null && neuron.getLabel().equals("Selected"));
//                    neuralLayerWidget.setSelected();
//                }
//            }
            //neuralLayerWidget.setSelected(layer.getLabel() != null && layer.getLabel().equals("Selected"));
            LabelWidget layerLabelWidget = new LabelWidget(this);

            String layerLabel = layer.getLabel();
            if (layerLabel == null) {
                layerLabel = "Layer " + i;
            } else if (layerLabel.isEmpty()) {
                layerLabel = "Layer " + i;
            }

            layerLabelWidget.setLabel(layerLabel);

            if (layer.getNeuronsCount() == 0) {
                LabelWidget emptyLabel = new LabelWidget(this, "Empty Layer");
                emptyLabel.setForeground(Color.LIGHT_GRAY);
                emptyLabel.setFont(new Font("Arial", Font.BOLD, 14));
                neuralLayerWidget.setLayout(LayoutFactory.createAbsoluteLayout());
                emptyLabel.setPreferredLocation(new Point(20, 25));
                neuralLayerWidget.setPreferredSize(new Dimension(140, (int) neuralLayerWidget.getPreferredSize().getHeight()));
                neuralLayerWidget.addChild(emptyLabel);
            }

            for (int j = 0; j < neuralNetwork.getLayerAt(i).getNeuronsCount(); j++) {
                Neuron neuron = neuralNetwork.getLayerAt(i).getNeuronAt(j);
                NeuronWidget neuronWidget = new NeuronWidget(this, neuron);
                if (!getObjects().contains(neuron))                
                    addObject(neuron, neuronWidget);
//                if (selection instanceof NeuronWidget) { // if this neurn was selected before refresh...
//                    if (((NeuronWidget) selection).getNeuron() == neuron) {
//                        //setSelection(neuron.getLabel() != null && neuron.getLabel().equals("Selected"));
//                        neuronWidget.setSelected();
//                    }
//
//
//                }
                resizeLayer(neuralLayerWidget);
                //Napravio wrapper oko neuronWidget i label da bi label pisao unutar widgeta. Koristim OverlayLayout
                IconNodeWidget neuronWrapperWidget = new IconNodeWidget(this);
                neuronWrapperWidget.setLayout(LayoutFactory.createOverlayLayout());
                neuronWrapperWidget.addChild(neuronWidget);
                double output = neuronWidget.getNeuron().getOutput();
                DecimalFormat df = new DecimalFormat("#.###");
                LabelWidget label = new LabelWidget(this, df.format(output));
                label.setForeground(Color.white);
                label.setAlignment(LabelWidget.Alignment.CENTER);
                label.setVerticalAlignment(LabelWidget.VerticalAlignment.CENTER);
                neuronWrapperWidget.addChild(label);
                neuralLayerWidget.addChild(neuronWrapperWidget);
                neurons.add(neuron);

                neuronsAndWidgets.put(neuron, neuronWidget);
            }

            layerWrapperWidget.addChild(layerLabelWidget);
            layerWrapperWidget.addChild(neuralLayerWidget);
            neuralNetworkWidget.addChild(layerWrapperWidget);
        }


        // create inputs widgets
        IconNodeWidget inputsWidget = new IconNodeWidget(this);
        inputsWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 5));



        IconNodeWidget outputsWidget = new IconNodeWidget(this);
        outputsWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 5));

        // 
        if (neuralNetwork.getInputNeurons() != null) {
            for (int i = 0; i < neuralNetwork.getInputNeurons().length; i++) {
                LabelWidget inputLabel = new LabelWidget(this);
                inputLabel.setLabel("Input " + (i + 1));
                inputLabel.setBorder(org.netbeans.api.visual.border.BorderFactory.createRoundedBorder(5, 5, Color.white, Color.black));
                inputsWidget.addChild(inputLabel);

                NeuronWidget targetWidget = neuronsAndWidgets.get(neuralNetwork.getInputNeurons()[i]);
                if (showConnections) {
                    ConnectionWidget connWidget = new ConnectionWidget(this);

                    connWidget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
                    connWidget.setSourceAnchor(AnchorFactory.createRectangularAnchor(inputLabel));
                    connWidget.setTargetAnchor(AnchorFactory.createRectangularAnchor(targetWidget));
                    connectionLayer.addChild(connWidget);
                }
            }
            neuralNetworkWidget.addChild(0, inputsWidget);
        }

        if (neuralNetwork.getOutputNeurons() != null) {
            neuralNetworkWidget.addChild(outputsWidget);
            for (int i = 0; i < neuralNetwork.getOutputNeurons().length; i++) {
                LabelWidget outputLabel = new LabelWidget(this);
                outputLabel.setLabel("Output " + (i + 1));
                outputLabel.setBorder(org.netbeans.api.visual.border.BorderFactory.createRoundedBorder(5, 5, Color.white, Color.black));
                outputsWidget.addChild(outputLabel);

                NeuronWidget sourceWidget = neuronsAndWidgets.get(neuralNetwork.getOutputNeurons()[i]);
                if (showConnections) {
                    ConnectionWidget connWidget = new ConnectionWidget(this);

                    connWidget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
                    connWidget.setSourceAnchor(AnchorFactory.createRectangularAnchor(sourceWidget));
                    connWidget.setTargetAnchor(AnchorFactory.createRectangularAnchor(outputLabel));
                    connectionLayer.addChild(connWidget);
                }
            }
        }





    }

    private void createConnections() {
// treba ih izbaciti i iz neurons i neurons aAndWidgets!
        for (int i = 0; i < neurons.size(); i++) {
            Neuron targetNeuron = neurons.get(i);
            Connection[] inputConnections = targetNeuron.getInputConnections();
            for (int c = 0; c < inputConnections.length; c++) {
                NeuronWidget targetWidget = neuronsAndWidgets.get(targetNeuron);
                NeuronWidget sourceWidget = neuronsAndWidgets.get(inputConnections[c].getFromNeuron());
                if (sourceWidget == null) { // hack when layer is deleted
                    continue;
                }
                NeuronConnectionWidget connWidget = new NeuronConnectionWidget(this, inputConnections[c], sourceWidget, targetWidget);
   
                connWidget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
                connWidget.setSourceAnchor(AnchorFactory.createRectangularAnchor(sourceWidget));
                connWidget.setTargetAnchor(AnchorFactory.createRectangularAnchor(targetWidget));
                sourceWidget.addConnection(connWidget);
                targetWidget.addConnection(connWidget);
                connectionLayer.addChild(connWidget);
                
                addObject(inputConnections[c], connWidget);
            }
        }

    }

    public boolean isWaitingLayerClick() {
        return waitingLayerClick;
    }

    public void setWaitingLayerClick(boolean waitingLayerClick) {
        this.waitingLayerClick = waitingLayerClick;
    }

    public NeuralNetworkWidget getNeuralNetworkWidget() {
        return neuralNetworkWidget;
    }

    public LayerWidget getInterractionLayer() {
        return interractionLayer;
    }

    public LayerWidget getConnectionLayer() {
        return connectionLayer;
    }

    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }

    public boolean isShowConnections() {
        return showConnections;
    }

    public void setShowConnections(boolean showConnections) {
        this.showConnections = showConnections;
    }

    public boolean isWaitingClick() {
        return waitingClick;
    }

    public void setWaitingClick(boolean waitingClick) {
        this.waitingClick = waitingClick;
    }
}
