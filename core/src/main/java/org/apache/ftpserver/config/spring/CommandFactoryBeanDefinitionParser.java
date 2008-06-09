package org.apache.ftpserver.config.spring;

import java.util.List;

import org.apache.ftpserver.DefaultCommandFactory;
import org.apache.ftpserver.interfaces.CommandFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Parses the FtpServer "commands" element into a Spring
 * bean graph
 */
public class CommandFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends CommandFactory> getBeanClass(Element element) {
        return DefaultCommandFactory.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        ManagedMap commands = new ManagedMap();
        
        List<Element> childs = SpringUtil.getChildElements(element);

        for(Element commandElm : childs) {
            String name = commandElm.getAttribute("name");
            Object bean = SpringUtil.parseSpringChildElement(commandElm, parserContext, builder); 
            commands.put(name, bean);
        }
        
        builder.addPropertyValue("commandMap", commands);
        
        if(StringUtils.hasText(element.getAttribute("use-default"))) {
            builder.addPropertyValue("useDefaultCommands", Boolean.parseBoolean(element.getAttribute("use-default")));
        }
    }
}
