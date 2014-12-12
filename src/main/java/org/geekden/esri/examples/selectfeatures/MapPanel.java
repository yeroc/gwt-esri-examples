package org.geekden.esri.examples.selectfeatures;

import java.util.concurrent.atomic.AtomicLong;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.SimplePanel;

import edu.ucdavis.cstars.client.MapWidget;
import edu.ucdavis.cstars.client.MapWidget.BaseMap;
import edu.ucdavis.cstars.client.Util;
import edu.ucdavis.cstars.client.dijits.OverviewMap;
import edu.ucdavis.cstars.client.event.MapLoadHandler;
import edu.ucdavis.cstars.client.geometry.Extent;

public class MapPanel extends SimplePanel {
  
  private static final AtomicLong mapIdGenerator = new AtomicLong();
  
  private final MapLoadHandler onMapLoad;
  private final Extent initialExtent;
  
  public MapPanel(MapLoadHandler callback, Extent initialExtent) {
    this.onMapLoad = callback;
    this.initialExtent = initialExtent;
    
    // The container for an ESRI map MUST have an id assigned...
    getElement().setId("map-" + mapIdGenerator.incrementAndGet());
  }
  
  @Override
  protected void onLoad() {
    loadMapDependencies();
  }
  
  private void loadMapDependencies() {
    Util.addRequiredPackage(Util.Package.ESRI_DIJIT_OVERVIEWMAP);
    Util.addRequiredPackage(Util.Package.ESRI_TOOLBARS_DRAW);
    Util.addRequiredPackage(Util.Package.ESRI_LAYERS_FEATURELAYER);
    Util.addEsriLoadHandler(onEsriLoad);
  }
  
  private Runnable onEsriLoad = new Runnable() {

    @Override
    public void run() {
      GWT.log("onEsriLoad");
      
      MapWidget.Options mapOptions = MapWidget.Options.create();
      
      mapOptions.setBaseMap(BaseMap.HYBRID);
      mapOptions.setExtent(initialExtent);
      mapOptions.setLogo(false);
      mapOptions.setShowAttribution(false);
      mapOptions.showSlider(true);
      
      MapWidget map = new MapWidget(MapPanel.this, onMapLoad, mapOptions);
      
      OverviewMap.Parameters overviewParams = OverviewMap.Parameters.create();
      overviewParams.setAttachTo("bottom-right");
      overviewParams.setMap(map);
      overviewParams.showMaximizeButton(true);
      
      new OverviewMap(overviewParams).startup();
    }
  };
  
}
