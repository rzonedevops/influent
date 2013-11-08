package influent.server.spi.impl.anb;

import influent.server.spi.ExportDataService;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import oculus.aperture.spi.store.ConflictException;
import oculus.aperture.spi.store.ContentService;
import oculus.aperture.spi.store.ContentService.Document;
import oculus.aperture.spi.store.ContentService.DocumentDescriptor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;

public class AnbExportDataService implements ExportDataService {

	private final ContentService service;
	
	// The name of the CMS store we'll use for image captures
	private final static String DEFAULT_STORE = "aperture.render";
	
	@Inject
	public AnbExportDataService(
		ContentService service
	) {
		this.service = service;
	}
	
	
	
	
	@Override
	public DocumentDescriptor exportToAnb(JSONObject JSONData) throws ConflictException, JSONException, JAXBException {
		
		byte[] XMLdata = convertJSONToXML(JSONData);
		final String xmlType = "application/xml";
		
		// Store to the content service, return a URL to the image
		Document doc = service.createDocument();
		doc.setContentType(xmlType);
		doc.setDocument(XMLdata);
		
		// Store and let the content service pick the id
		return service.storeDocument(
			doc, 
			DEFAULT_STORE, 
			null, 
			null
		);
	}

	
	
	
	
	private byte[] convertJSONToXML(JSONObject JSONData) throws JSONException, JAXBException {

		Chart chart = new Chart();
        chart.setChartItemCollection(new ChartItemCollection());
        chart.getChartItemCollection().setChartItems(new ArrayList<ChartItem>());
        
    	JSONArray columns = JSONData.getJSONArray("columns");
    	for (int i = 0; i < columns.length(); i++) {
            JSONArray files = columns.getJSONObject(i).getJSONArray("files");
        	for (int j = 0; j < files.length(); j++) {
        		if (files.get(j) instanceof JSONObject) {
                	
                	JSONObject file = files.getJSONObject(j);
                	
                	ChartItem nodeItem = new ChartItem();
                    chart.getChartItemCollection().getChartItems().add(nodeItem);

                    nodeItem.setAttrLabel(file.getString("title"));

                    nodeItem.setCIStyle(new CIStyle());
                    nodeItem.getCIStyle().setFont(new Font());
                    nodeItem.getCIStyle().getFont().setAttrPointSize(1);

                    nodeItem.setEnd(new End());
                    nodeItem.getEnd().setEntity(new Entity());
                    nodeItem.getEnd().getEntity().setAttrEntityId(file.getString("xfId"));
                    nodeItem.getEnd().getEntity().setIcon(new Icon());
                    nodeItem.getEnd().getEntity().getIcon().setIconStyle(new IconStyle());

                    nodeItem.setAttributeCollection(new AttributeCollection());
                    nodeItem.getAttributeCollection().setAttributes(new ArrayList<Attribute>());
                    
                    JSONArray links = file.getJSONArray("links");
                    for (int k = 0; k < links.length(); k++) {
                    	if (links.get(k) instanceof JSONObject) {
                    		JSONObject link = links.getJSONObject(k);
                    		
                    		ChartItem linkItem = new ChartItem();
                    		chart.getChartItemCollection().getChartItems().add(linkItem);

                    		linkItem.setLink(new Link());
                    		linkItem.getLink().setAttrEnd1Id(file.getString("xfId"));
                    		linkItem.getLink().setAttrEnd2Id(link.getString("destination"));

                    		linkItem.getLink().setLinkStyle(new LinkStyle());
                    		linkItem.getLink().getLinkStyle().setAttrType(link.getString("type"));
                    	}
                    }
        		}
        	}
    	}
        
    	JAXBContext jc = JAXBContext.newInstance(Chart.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        marshaller.marshal(chart, baoStream);
        
        return baoStream.toByteArray();
	}
}
