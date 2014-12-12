package org.geekden.esri.examples.selectfeatures;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

import edu.ucdavis.cstars.client.Graphic;
import edu.ucdavis.cstars.client.InfoTemplate;
import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.SpatialReference;
import edu.ucdavis.cstars.client.callback.SelectFeaturesCallback;
import edu.ucdavis.cstars.client.dojo.Color;
import edu.ucdavis.cstars.client.event.DrawEndHandler;
import edu.ucdavis.cstars.client.event.LayerLoadHandler;
import edu.ucdavis.cstars.client.event.MapLoadHandler;
import edu.ucdavis.cstars.client.geometry.Extent;
import edu.ucdavis.cstars.client.geometry.Geometry;
import edu.ucdavis.cstars.client.layers.FeatureLayer;
import edu.ucdavis.cstars.client.layers.FeatureLayer.Modes;
import edu.ucdavis.cstars.client.layers.FeatureLayer.Selections;
import edu.ucdavis.cstars.client.layers.Layer;
import edu.ucdavis.cstars.client.symbol.SimpleFillSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol;
import edu.ucdavis.cstars.client.symbol.SimpleLineSymbol.StyleType;
import edu.ucdavis.cstars.client.tasks.Query;
import edu.ucdavis.cstars.client.toolbars.Draw;

public class SelectFeatures implements EntryPoint
{
  private final RootPanel root = RootPanel.get();
  
  private final Panel buttonPanel = new FlowPanel();
  private final Button selectFeaturesButton = new Button("Select Features");
  private final Button clearSelectionButton = new Button("Clear Selection");
  private final Label messages = new Label();
  
  private FeatureLayer featureLayer;
  private final Query queryTask = Query.create();
  private Draw selectionToolbar;

  public SelectFeatures() {
  }
  
  @Override
  public void onModuleLoad() {
    GWT.log("onModuleLoadx");
    
    Extent initialExtent =
        (Extent) Geometry.geographicToWebMercator(
            Extent.create(-97.5328, 37.4344, -97.2582, 37.64041, SpatialReference.create(4326)));

    buttonPanel.getElement().setId("button-panel");
    
    
    root.add(new MapPanel(onMapLoad, initialExtent));
    root.add(buttonPanel);
    root.add(messages);
    
    buttonPanel.addStyleName("btn-toolbar");
    
  }
  
  private FeatureLayer createFeatureLayer() {
    
    InfoTemplate infoTemplate = InfoTemplate.create();
    infoTemplate.setTitle("${FIELD_NAME}");
    String content = 
        "<b>Status</b>: ${STATUS}<br>" +
        "<b>Cummulative Gas</b>: ${CUMM_GAS} MCF<br>" +
        "<b>Total Acres</b>: ${APPROXACRE}";
    infoTemplate.setContent(content);
    
    FeatureLayer.Options flo = FeatureLayer.Options.create();
    flo.setMode(Modes.MODE_ONDEMAND);
    flo.setInfoTemplate(infoTemplate);
    flo.setOutFields(new String[]{"*"});
    flo.setOpacity(100);
    FeatureLayer layer = FeatureLayer.create("http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSPetro/MapServer/1", flo);
    
    layer.setDefinitionExpression("PROD_GAS='Yes'");
    
    SimpleFillSymbol fieldsSelectionSymbol = SimpleFillSymbol.create();
    Color selectionFillColor = Color.create(255,255,0,0.5);
    fieldsSelectionSymbol.setColor(selectionFillColor);
    
    SimpleLineSymbol selectionOutlineSymbol = SimpleLineSymbol.create(StyleType.STYLE_DASHDOT, Color.create(255,0,0), 2);
    fieldsSelectionSymbol.setOutline(selectionOutlineSymbol);
        
    layer.setSelectionSymbol(fieldsSelectionSymbol);
    return layer;
  }
  
  private MapLoadHandler onMapLoad = new MapLoadHandler() {

    @Override
    public void onLoad(MapWidget map) {
      GWT.log("onMapLoad");
      
      initSelectToolbar(map);
      onClientReady(map);
    }
  };

  private void initSelectToolbar(MapWidget mapWidget) {
    GWT.log("initSelectToolbar");
    
    Draw.Options drawOptions = Draw.Options.create();
    drawOptions.setDrawTime(75);
    drawOptions.showTooltips(true);
    drawOptions.setTolerance(8);
    selectionToolbar = Draw.create(mapWidget, drawOptions);
    
    selectionToolbar.addDrawEndHandler(new DrawEndHandler() {

      @Override
      public void onDrawEnd(Geometry geometry) {
        selectionToolbar.deactivate();
        queryTask.setGeometry(geometry);
        featureLayer.selectFeatures(queryTask, FeatureLayer.Selections.SELECTION_NEW,
            new SelectFeaturesCallback() {

              @Override
              public void onSelectionComplete(JsArray<Graphic> features, Selections selectionMethod) {
                sumGasProduction(features, selectionMethod);
              }

              @Override
              public void onError(edu.ucdavis.cstars.client.Error error) {
                String errorMsg = error.getMessage();
                throw new RuntimeException(errorMsg);
              }
            });
      }
    });
    
    
    selectFeaturesButton.addStyleName("btn");
    selectFeaturesButton.addStyleName("btn-primary");
    selectFeaturesButton.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        _selectFeaturesClick(SelectFeatures.this.selectionToolbar);
      }
    });
    
    
    clearSelectionButton.addStyleName("btn");
    clearSelectionButton.addStyleName("btn-warning");
    clearSelectionButton.addClickHandler(new ClickHandler() {
      
      @Override
      public void onClick(ClickEvent event) {
        featureLayer.clearSelection();
        
      }
    });

    buttonPanel.add(selectFeaturesButton);
    buttonPanel.add(clearSelectionButton);
  }
  
  private void sumGasProduction(JsArray<Graphic> features, Selections selectionMethod) {
    int productionSum = 0;
    // summarize the cumulative gas production to display
    for (int i = 0; i < features.length(); i++) {
      Graphic feature = features.get(i);
      productionSum += feature.getAttributes().getInt("CUMM_GAS");
    }
    messages.getElement().setInnerHTML(
        "<b>Selected Fields Production: " + productionSum + " mcf. </b>");
  }

  private native final void _selectFeaturesClick(Draw theSelectionToolbar) /*-{
    try {
      theSelectionToolbar.activate($wnd.esri.toolbars.Draw.EXTENT);
    } catch(e) {
      $wnd.alert("Exception hit: "+e);   
      throw e;    
    }
  }-*/;
            
  public void onClientReady(final MapWidget mapWidget) {
    GWT.log("onClientReady");
    // this fires when the mapClient is done loading (when the loading
    // screen goes away).  You can now access the map using
    // mapClient.getMapWidget() and add your own events and controls.

    featureLayer = createFeatureLayer();
    featureLayer.addLoadHandler(new LayerLoadHandler() {
      
      @Override
      public void onLoad(Layer layer) {
        mapWidget.addLayer(featureLayer);  
      }
    });   
  } 
}
