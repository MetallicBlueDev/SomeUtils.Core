package com.tr4ncer.logger;

import org.xml.sax.*;

/**
 * Redirige les erreurs XML vers le journal d'erreur.
 *
 * @version 1.00.00
 * @author Sebastien Villemain
 */
public class SAXErrorHandler implements ErrorHandler {

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        LoggerManager.getInstance().addWarning(exception.getMessage());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        LoggerManager.getInstance().addError(exception);
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        LoggerManager.getInstance().addError(exception);
    }
}
