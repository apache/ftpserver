package org.apache.ftpserver.config.spring;

import org.apache.ftpserver.filesystem.NativeFileSystemManager;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parses the FtpServer "native-filesystem" element into a Spring
 * bean graph
 */
public class FileSystemBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends FileSystemManager> getBeanClass(Element element) {
        return NativeFileSystemManager.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        if(StringUtils.hasText(element.getAttribute("case-insensitive"))) {
            builder.addPropertyValue("caseInsensitive", Boolean.parseBoolean(element.getAttribute("case-insensitive")));
        }
        if(StringUtils.hasText(element.getAttribute("create-home"))) {
            builder.addPropertyValue("create-home", Boolean.parseBoolean(element.getAttribute("create-home")));
        }
    }
}
