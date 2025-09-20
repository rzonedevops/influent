package oculus.aperture.spi.store;

import java.io.InputStream;
import java.util.Date;

/**
 * Stub interface to replace missing ApertureJS dependency
 */
public interface ContentService {
    
    public static class DocumentDescriptor {
        private String id;
        private String contentType;
        private Date modified;
        
        public DocumentDescriptor(String id, String contentType, Date modified) {
            this.id = id;
            this.contentType = contentType;
            this.modified = modified;
        }
        
        public String getId() { return id; }
        public String getContentType() { return contentType; }
        public Date getModified() { return modified; }
    }
    
    public static class Document {
        private DocumentDescriptor descriptor;
        private InputStream inputStream;
        
        public Document(DocumentDescriptor descriptor, InputStream inputStream) {
            this.descriptor = descriptor;
            this.inputStream = inputStream;
        }
        
        public DocumentDescriptor getDescriptor() { return descriptor; }
        public InputStream getInputStream() { return inputStream; }
    }
    
    DocumentDescriptor createDocument() throws ConflictException;
    Document getDocument(String docId);
}