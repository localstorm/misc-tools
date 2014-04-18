package co.kuznetsov.xml;

import co.kuznetsov.util.SimpleStack;

import java.io.Reader;

import static co.kuznetsov.util.StringBuilderUtils.*;

/**
 * Based on Steven R. Brandt's (Java Tip 128: Create a quick-and-dirty XML parser)
 * <p/>
 * This parser is, like the SAX parser, an event based parser, but with much less functionality.
 * We also use our own events like attributeName and attributeValue to make sure we don't accumulate anything
 * <p/>
 * It uses ByteBuffer as a data source.
 * <p/>
 * The instances of QDParser are non-thread safe, because they have internal state.
 * Internal state could be avoided, but for sake of avoiding garbage we pre-create
 * StringBuilder instances.
 * <p/>
 * It also tries the best to avoid garbage creation
 */
public class QDParser {

    private static int popMode(SimpleStack st) {
        if (!st.empty())
            return st.pop();
        else
            return PRE;
    }

    private final static int
            TEXT = 1,
            ENTITY = 2,
            OPEN_TAG = 3,
            CLOSE_TAG = 4,
            START_TAG = 5,
            ATTRIBUTE_LVALUE = 6,
            ATTRIBUTE_EQUAL = 9,
            ATTRIBUTE_RVALUE = 10,
            QUOTE = 7,
            IN_TAG = 8,
            SINGLE_TAG = 12,
            COMMENT = 13,
            DONE = 11,
            DOCTYPE = 14,
            PRE = 15,
            CDATA = 16;

    private final SimpleStack st;
    private final StringBuilder sb;
    private final StringBuilder etag;
    private final StringBuilder tag;

    public QDParser(int maxStateDepth) {
        this.st = new SimpleStack(maxStateDepth);
        this.sb = new StringBuilder();
        this.etag = new StringBuilder();
        this.tag = new StringBuilder();
    }

    public void parse(DocHandler doc, Reader reader) throws Exception {
        st.clear();
        sb.setLength(0);
        etag.setLength(0);
        tag.setLength(0);

        int c;
        int depth = 0;
        int mode = PRE;
        int quotec = '"';

        doc.startDocument();
        int line = 1, col = 0;
        boolean eol = false;
        while ((c = reader.read()) != -1) {
            // We need to map \r, \r\n, and \n to \n
            // See XML spec section 2.11
            if (c == '\n' && eol) {
                eol = false;
                continue;
            } else if (eol) {
                eol = false;
            } else if (c == '\n') {
                line++;
                col = 0;
            } else if (c == '\r') {
                eol = true;
                c = '\n';
                line++;
                col = 0;
            } else {
                col++;
            }

            if (mode == DONE) {
                doc.endDocument();
                return;

                // We are between tags collecting text.
            } else if (mode == TEXT) {
                if (c == '<') {
                    st.push(mode);
                    mode = START_TAG;
                    if (sb.length() > 0) {
                        doc.text(sb);
                        sb.setLength(0);
                    }
                } else if (c == '&') {
                    st.push(mode);
                    mode = ENTITY;
                    etag.setLength(0);
                } else
                    sb.append((char) c);

                // we are processing a closing tag: e.g. </foo>
            } else if (mode == CLOSE_TAG) {
                if (c == '>') {
                    mode = popMode(st);
                    tag.setLength(0);
                    tag.append(sb);
                    sb.setLength(0);
                    depth--;
                    if (depth == 0)
                        mode = DONE;
                    doc.endElement(tag);
                } else {
                    sb.append((char) c);
                }

                // we are processing CDATA
            } else if (mode == CDATA) {
                if (c == '>' && endsWith(sb, "]]")) {
                    sb.setLength(sb.length() - 2);
                    doc.text(sb);
                    sb.setLength(0);
                    mode = popMode(st);
                } else
                    sb.append((char) c);

                // we are processing a comment.  We are inside
                // the <!-- .... --> looking for the -->.
            } else if (mode == COMMENT) {
                if (c == '>' && endsWith(sb, "--")) {
                    sb.setLength(0);
                    mode = popMode(st);
                } else
                    sb.append((char) c);

                // We are outside the root tag element
            } else if (mode == PRE) {
                if (c == '<') {
                    mode = TEXT;
                    st.push(mode);
                    mode = START_TAG;
                }

                // We are inside one of these <? ... ?>
                // or one of these <!DOCTYPE ... >
            } else if (mode == DOCTYPE) {
                if (c == '>') {
                    mode = popMode(st);
                    if (mode == TEXT) mode = PRE;
                }

                // we have just seen a < and
                // are wondering what we are looking at
                // <foo>, </foo>, <!-- ... --->, etc.
            } else if (mode == START_TAG) {
                mode = popMode(st);
                if (c == '/') {
                    st.push(mode);
                    mode = CLOSE_TAG;
                } else if (c == '?') {
                    mode = DOCTYPE;
                } else {
                    st.push(mode);
                    mode = OPEN_TAG;
                    tag.setLength(0);
                    sb.append((char) c);
                }

                // we are processing an entity, e.g. &lt;, &#187;, etc.
            } else if (mode == ENTITY) {
                if (c == ';') {
                    mode = popMode(st);
                    if (equalsTo(etag, "lt"))
                        sb.append('<');
                    else if (equalsTo(etag, "gt"))
                        sb.append('>');
                    else if (equalsTo(etag, "amp"))
                        sb.append('&');
                    else if (equalsTo(etag, "quot"))
                        sb.append('"');
                    else if (equalsTo(etag, "apos"))
                        sb.append('\'');
                        // Could parse hex entities if we wanted to
                    else if(startsWith(etag, "#x"))
                        sb.append((char) parseInt(etag, 2, 16));
                    else if (startsWith(etag, "#"))
                        sb.append((char) parseInt(etag, 1, 10));
                        // Insert custom entity definitions here
                    else
                        exc("Unknown entity: &" + etag + ";", line, col);

                    etag.setLength(0);
                } else {
                    etag.append((char) c);
                }

                // we have just seen something like this:
                // <foo a="b"/
                // and are looking for the final >.
            } else if (mode == SINGLE_TAG) {
                if (tag.length() == 0) {
                    tag.append(sb);
                }
                if (c != '>') {
                    exc("Expected > for tag: <" + tag + "/>", line, col);
                }
                doc.endElement(tag);
                if (depth == 0) {
                    doc.endDocument();
                    return;
                }
                sb.setLength(0);
                tag.setLength(0);
                mode = popMode(st);

                // we are processing something
                // like this <foo ... >.  It could
                // still be a <!-- ... --> or something.
            } else if (mode == OPEN_TAG) {
                if (c == '>') {
                    if (tag.length() == 0) {
                        tag.append(sb);
                    }
                    sb.setLength(0);
                    depth++;
                    doc.startElement(tag);
                    tag.setLength(0);
                    mode = popMode(st);
                } else if (c == '/') {
                    doc.startElement(tag);
                    mode = SINGLE_TAG;
                } else if (c == '-' && equalsTo(sb, "!-")) {
                    mode = COMMENT;
                } else if (c == '[' && equalsTo(sb, "![CDATA")) {
                    mode = CDATA;
                    sb.setLength(0);
                } else if (c == 'E' && equalsTo(sb, "!DOCTYP")) {
                    sb.setLength(0);
                    mode = DOCTYPE;
                } else if (Character.isWhitespace((char) c)) {
                    tag.setLength(0);
                    tag.append(sb);
                    sb.setLength(0);
                    doc.startElement(tag);
                    mode = IN_TAG;
                } else {
                    sb.append((char) c);
                }

                // We are processing the quoted right-hand side
                // of an element's attribute.
            } else if (mode == QUOTE) {
                if (c == quotec) {
                    doc.attributeValue(sb);
                    sb.setLength(0);
                    mode = IN_TAG;
                    // See section the XML spec, section 3.3.3
                    // on normalization processing.
                } else if (" \r\n\u0009".indexOf(c) >= 0) {
                    sb.append(' ');
                } else if (c == '&') {
                    st.push(mode);
                    mode = ENTITY;
                    etag.setLength(0);
                } else {
                    sb.append((char) c);
                }

            } else if (mode == ATTRIBUTE_RVALUE) {
                if (c == '"' || c == '\'') {
                    quotec = c;
                    mode = QUOTE;
                } else if (Character.isWhitespace((char) c)) {
                    ;
                } else {
                    exc("Error in attribute processing", line, col);
                }

            } else if (mode == ATTRIBUTE_LVALUE) {
                if (Character.isWhitespace((char) c)) {
                    doc.attributeName(sb);
                    sb.setLength(0);
                    mode = ATTRIBUTE_EQUAL;
                } else if (c == '=') {
                    doc.attributeName(sb);
                    sb.setLength(0);
                    mode = ATTRIBUTE_RVALUE;
                } else {
                    sb.append((char) c);
                }

            } else if (mode == ATTRIBUTE_EQUAL) {
                if (c == '=') {
                    mode = ATTRIBUTE_RVALUE;
                } else if (Character.isWhitespace((char) c)) {
                    // Ignore
                } else {
                    exc("Error in attribute processing.", line, col);
                }

            } else if (mode == IN_TAG) {
                if (c == '>') {
                    mode = popMode(st);
                    depth++;
                    tag.setLength(0);
                } else if (c == '/') {
                    mode = SINGLE_TAG;
                } else if (Character.isWhitespace((char) c)) {
                    // Ignore
                } else {
                    mode = ATTRIBUTE_LVALUE;
                    sb.append((char) c);
                }
            }
        }
        if (mode == DONE)
            doc.endDocument();
        else
            exc("missing end tag", line, col);
    }

    private static void exc(String s, int line, int col)
            throws Exception {
        throw new Exception(s + " near line " + line + ", column " + col);
    }
}

