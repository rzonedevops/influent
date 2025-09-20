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
        
        // Additional stub methods
        public String getStore() { return ""; }
        public String getRevision() { return ""; }
    }
    
    public static class Document {
        private DocumentDescriptor descriptor;
        private InputStream inputStream;
        private String contentType;
        private byte[] document;
        
        public Document(DocumentDescriptor descriptor, InputStream inputStream) {
            this.descriptor = descriptor;
            this.inputStream = inputStream;
        }
        
        public DocumentDescriptor getDescriptor() { return descriptor; }
        public InputStream getInputStream() { return inputStream; }
        
        // Additional stub methods
        public InputStream getContent() { return inputStream; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public void setDocument(byte[] document) { this.document = document; }
    }
    
    Document createDocument();
    Document getDocument(String docId);
    
    // Additional stub methods
    default DocumentDescriptor storeDocument(Document doc, String store, Object type, Object encoding) throws ConflictException {
        return new DocumentDescriptor("stub-id", "text/plain", new Date());
    }
}