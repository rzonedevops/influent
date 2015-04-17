/*
 * Copyright (C) 2013-2015 Uncharted Software Inc.
 *
 * Property of Uncharted(TM), formerly Oculus Info Inc.
 * http://uncharted.software/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package influent.server.spi.impl.graphml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Simple handler to marshal/unmarshal XML text in JAXB. This is necessary to ensure the XML text
 * is actually output as XML, as opposed to having &lt; and &gt; replacements everywhere.
 * 
 * @author pmoore
 *
 */
public class JAXBXMLHandler implements DomHandler<String, StreamResult> {
    private static final String JSON_START_TAG = "<json>";
    private static final String JSON_END_TAG = "</json>";
 
    private StringWriter xmlWriter = new StringWriter();
 
    public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
        return new StreamResult(xmlWriter);
    }
 
    public String getElement(StreamResult rt) {
        String xml = rt.getWriter().toString();
        int beginIndex = xml.lastIndexOf(JSON_START_TAG) + JSON_START_TAG.length();
        int endIndex = xml.lastIndexOf(JSON_END_TAG);
        return xml.substring(beginIndex, endIndex);
    }
 
    public Source marshal(String n, ValidationEventHandler errorHandler) {
        try {
            String xml = JSON_START_TAG + n.trim() + JSON_END_TAG;
            StringReader xmlReader = new StringReader(xml);
            return new StreamSource(xmlReader);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}