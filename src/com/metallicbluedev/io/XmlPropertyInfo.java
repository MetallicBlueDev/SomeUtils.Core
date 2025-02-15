package com.metallicbluedev.io;

import com.metallicbluedev.logger.*;
import com.metallicbluedev.utils.*;
import jakarta.xml.bind.*;
import java.io.*;
import java.nio.file.*;
import javax.xml.stream.*;

/**
 * Java object marshalling and unmarshalling in XML.
 *
 * @author Sébastien Villemain
 * @param <E>
 */
public final class XmlPropertyInfo<E> {

    private final Class<E> type;
    private E xmlObject;

    private Path path;
    private JAXBContext xmlContext;

    public XmlPropertyInfo(Path path, Class<E> type) {
        this.type = type;
        setPath(path);
    }

    public Path getPath() {
        return path;
    }

    public Class<E> getType() {
        return type;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setXmlObject(E xmlObject) {
        this.xmlObject = xmlObject;
    }

    public E getXmlObject() {
        return xmlObject;
    }

    public E getXmlObjectOrDefault() {
        E currentObject = xmlObject;

        if (currentObject == null) {
            currentObject = createDefaultXmlObject();
        }
        return currentObject;
    }

    public void setDefaultXmlObjectOnNull() {
        if (xmlObject == null) {
            xmlObject = createDefaultXmlObject();
        }
    }

    public boolean load() {
        LoggerManager.getInstance().addInformation("Loading XML file " + path);

        xmlObject = loadObjectFromXml();
        return xmlObject != null;
    }

    public boolean save() {
        LoggerManager.getInstance().addInformation("Saving XML file " + path);

        return saveObjectToXml();
    }

    private boolean saveObjectToXml() {
        boolean saved = false;

        if (path != null && xmlObject != null) {
            File file = path.toFile();

            try {
                Marshaller m = getContext().createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                m.marshal(xmlObject, file);
                saved = true;
            } catch (JAXBException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
        return saved;
    }

    private E loadObjectFromXml() {
        E loadedObject = null;

        try {
            XMLStreamReader xReader = getXMLStreamReader();

            if (xReader != null) {
                Unmarshaller u = getContext().createUnmarshaller();
                JAXBElement<E> root = u.unmarshal(xReader, type);
                loadedObject = root.getValue();
            }
        } catch (XMLStreamException | JAXBException ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return loadedObject;
    }

    private XMLStreamReader getXMLStreamReader() throws XMLStreamException {
        XMLStreamReader xReader = null;

        if (path != null) {
            File file = path.toFile();

            if (file.exists()) {
                try {
                    xReader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(file), "UTF-8");
                } catch (IOException ex) {
                    LoggerManager.getInstance().addError(ex);
                }
            }
        }
        return xReader;
    }

    private JAXBContext getContext() throws JAXBException {
        if (xmlContext == null && type != null) {
            xmlContext = JAXBContext.newInstance(type);
        }
        if (xmlContext == null) {
            throw new JAXBException("Unable to create JAXBContext");
        }
        return xmlContext;
    }

    private E createDefaultXmlObject() {
        return PackagesHelper.makeInstance(type);
    }
}
