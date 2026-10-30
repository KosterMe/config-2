package depviz.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Чтение конфигурационного файла XML в набор пар «параметр — значение».
 *
 * <p>Ожидается корневой элемент {@code <config>}, внутри которого каждый
 * параметр записан отдельным дочерним элементом с текстовым значением.
 */
final class XmlConfigReader {

    /** Имя корневого элемента. */
    static final String ROOT = "config";

    private static final String NO_DOCTYPE =
            "http://apache.org/xml/features/disallow-doctype-decl";
    private static final Set<String> KNOWN = Set.of(
            Config.PACKAGE, Config.REPOSITORY, Config.TEST_MODE, Config.ASCII_TREE);

    private XmlConfigReader() {
    }

    /**
     * Разбирает текст XML и возвращает значения параметров.
     *
     * @param xml содержимое конфигурационного файла
     * @return значения параметров в порядке их появления в файле
     * @throws ConfigException если XML некорректен или нарушена структура файла
     */
    static Map<String, String> read(String xml) throws ConfigException {
        Element root = parse(xml).getDocumentElement();
        if (!ROOT.equals(root.getTagName())) {
            throw new ConfigException("root element must be <" + ROOT + ">, got <"
                    + root.getTagName() + ">");
        }
        Map<String, String> values = new LinkedHashMap<>();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                addParameter(values, (Element) node);
            } else if (isText(node) && !node.getTextContent().isBlank()) {
                throw new ConfigException("unexpected text inside <" + ROOT + ">: '"
                        + node.getTextContent().trim() + "'");
            }
        }
        return values;
    }

    private static void addParameter(Map<String, String> values, Element element)
            throws ConfigException {
        String name = element.getTagName();
        if (!KNOWN.contains(name)) {
            throw new ConfigException("unknown parameter <" + name + ">");
        }
        if (values.containsKey(name)) {
            throw new ConfigException("parameter <" + name + "> is specified twice");
        }
        if (hasChildElements(element)) {
            throw new ConfigException("parameter <" + name + "> must contain only text");
        }
        values.put(name, element.getTextContent().trim());
    }

    private static boolean hasChildElements(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    private static boolean isText(Node node) {
        return node.getNodeType() == Node.TEXT_NODE
                || node.getNodeType() == Node.CDATA_SECTION_NODE;
    }

    private static Document parse(String xml) throws ConfigException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(NO_DOCTYPE, true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new StrictErrorHandler());
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (SAXParseException e) {
            throw new ConfigException("invalid XML at line " + e.getLineNumber() + ": "
                    + e.getMessage());
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new ConfigException("invalid XML: " + e.getMessage());
        }
    }

    /**
     * Обработчик ошибок разбора: прерывает разбор вместо печати в stderr.
     */
    private static final class StrictErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException e) {
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
}
