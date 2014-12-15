package kubus.ws.ku.kubus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by EARTH on 13/12/2557.
 */
public class ParseBusXml {
    private static ParseBusXml pbx;

    private ParseBusXml() {
    }

    public static ParseBusXml getInstance(){
        if(pbx == null){
          pbx = new ParseBusXml();
        }
        return pbx;
    }

    public List<Bus> parseXmlToBusWebSer(String inputXml) {
        inputXml = inputXml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
        inputXml = inputXml.replace("<buses>", "").replace("</buses>", "");
        String[] temp = inputXml.split("</bus>");
        List<Bus> busList = new ArrayList<Bus>();
        for (int i = 0; i < temp.length; i++) {
            Bus bus = new Bus();
            String[] temp2 = temp[i].split(">");
            System.out.print(temp2[0]);
            bus.setId(Long.parseLong(temp2[0].split("\"")[1]));
            bus.setBusLineID((Integer.parseInt(temp2[2].split("<")[0])));
            bus.setLat((Double.parseDouble(temp2[4].split("<")[0])));
            bus.setLon(Double.parseDouble(temp2[6].split("<")[0]));
            bus.setTimestamp(temp2[8].split("<")[0]);
            busList.add(bus);
        }
        return busList;
    }

    public List<Bus> parseXmlToBusWebSoc(String inputXml) {
        inputXml = inputXml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
        inputXml = inputXml.replace("<busList>", "").replace("</busList>", "");
        String[] temp = inputXml.split("</list>");
        List<Bus> busList = new ArrayList<Bus>();
        for (int i = 0; i < temp.length; i++) {
            System.out.println(temp[i]);
            Bus bus = new Bus();
            String[] temp2 = temp[i].split(">");

            System.out.println(temp2[0]);

            bus.setId(Long.parseLong(temp2[0].split("\"")[1]));
            bus.setBusLineID((Integer.parseInt(temp2[2].split("<")[0])));
            bus.setLat((Double.parseDouble(temp2[4].split("<")[0])));
            bus.setLon(Double.parseDouble(temp2[6].split("<")[0]));
            bus.setTimestamp(temp2[8].split("<")[0]);
            busList.add(bus);
        }
        return busList;
    }
}
