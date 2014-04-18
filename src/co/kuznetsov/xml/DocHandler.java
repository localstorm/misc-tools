package co.kuznetsov.xml;

/**
 * @author localstorm
 *         Date: 25.02.14
 */

public interface DocHandler {
    public void startElement(StringBuilder tag) throws Exception;
    public void endElement(StringBuilder tag) throws Exception;
    public void startDocument() throws Exception;
    public void endDocument() throws Exception;
    public void text(StringBuilder str) throws Exception;
    public void attributeName(StringBuilder name);
    public void attributeValue(StringBuilder value);
}
