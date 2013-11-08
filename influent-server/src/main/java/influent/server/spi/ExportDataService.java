package influent.server.spi;

import javax.xml.bind.JAXBException;

import oculus.aperture.spi.store.ConflictException;
import oculus.aperture.spi.store.ContentService.DocumentDescriptor;

import org.json.JSONException;
import org.json.JSONObject;

public interface ExportDataService {

	public DocumentDescriptor exportToAnb(JSONObject data) throws ConflictException, JSONException, JAXBException;
}
