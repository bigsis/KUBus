package kubus.ws.ku.kubus;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

/**
 * Created by EARTH on 13/12/2557.
 */
public class MarkerController {
    private GoogleMap mMap;
    private HashMap<String, Marker> markers;
    public static MarkerController mc;
    private MarkerController(GoogleMap mMap, HashMap<String, Marker> markers){
            this.mMap = mMap;
            this.markers = markers;
    }

    public static MarkerController getInstance(GoogleMap mMap, HashMap<String, Marker> markers){
        if(mc == null){
            mc = new MarkerController(mMap, markers);
        }
        return mc;
    }

    public void setBusLocation(List<Bus> buses) {
//        System.out.println(buses.toString());
        if (markers.size() == 0) {
            for (Bus b : buses) {
                MarkerOptions mo = new MarkerOptions();
                mo.title(b.getId() + "");
                mo.snippet(b.getBusLineID() + "");
                mo.icon(getBusIcon(b.getBusLineID()));
                mo.visible(true);
                markers.put(b.getId() + "", mMap.addMarker(mo.position(new LatLng(b.getLat(), b.getLon()))));
            }
        } else {
            for (Bus b : buses) {
//                    System.out.println(buses.toString());
                if (markers.containsKey(b.getId() + "")) {
                    markers.get(b.getId() + "").setPosition(new LatLng(b.getLat(), b.getLon()));
                    System.out.println("equal");
//                    break;
                } else {
                    MarkerOptions mo = new MarkerOptions();
                    mo.title(b.getId() + "");
                    mo.snippet(b.getBusLineID() + "");
                    mo.icon(getBusIcon(b.getBusLineID()));
                    mo.visible(true);
                    markers.put(b.getId() + "", mMap.addMarker(mo.position(new LatLng(b.getLat(), b.getLon()))));
                }
            }
        }
    }

    public BitmapDescriptor getBusIcon(long id){
        BitmapDescriptor bd = null;
        if( id == 1){
            bd = BitmapDescriptorFactory.fromResource(R.drawable.bus1);
        }
        else if( id == 2){
            bd = BitmapDescriptorFactory.fromResource(R.drawable.bus2);
        }
        else if( id == 3){
            bd = BitmapDescriptorFactory.fromResource(R.drawable.bus3);
        }
        else if( id == 4){
            bd = BitmapDescriptorFactory.fromResource(R.drawable.bus4);
        }
        else if( id == 5){
            bd = BitmapDescriptorFactory.fromResource(R.drawable.bus5);
        }
        return bd;
    }

    public void setAllAlpha(){
        String[] busID = markers.keySet().toArray(new String[0]);
            for (String s : busID)
                markers.get(s).setAlpha((float) 0.3);
    }

    public void setAlpha(Marker m){
        if (markers.containsKey(m.getTitle() + "")) {
            getMarker(m).setAlpha((float) 0.3);
        }
    }

    public void reAlpha(Marker m){
        if (markers.containsKey(m.getTitle() + "")) {
           getMarker(m).setAlpha((float) 1);
        }
    }

    public void reAllAlpha(){
        String[] busID = markers.keySet().toArray(new String[0]);
        for (String s : busID)
            markers.get(s).setAlpha((float) 1);
    }

    public Marker getMarker(Marker m){
        return markers.get(m.getTitle() + "");
    }

}
