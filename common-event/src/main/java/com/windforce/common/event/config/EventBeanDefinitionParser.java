package com.windforce.common.event.config;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import org.w3c.dom.Element;

import com.windforce.common.event.anno.ReceiverAnno;
import com.windforce.common.event.core.EventBusManager;
import com.windforce.common.event.monitor.MonitorEventBusManager;

public class EventBeanDefinitionParser implements BeanDefinitionParser {

	private static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";
	private static final String MONITOR_ATTRIBUTE = "monitor";

	private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

	private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
			this.resourcePatternResolver);

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		registBeanPostProcessor(parserContext);
		registAllReceivers(element, parserContext);
		boolean monitor = Boolean.valueOf(element
				.getAttribute(MONITOR_ATTRIBUTE));
		Class<?> clz = EventBusManager.class;
		if (monitor) {
			clz = MonitorEventBusManager.class;
		}
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.rootBeanDefinition(clz);
		BeanDefinition definition = builder.getBeanDefinition();
		parserContext.getRegistry().registerBeanDefinition(
				StringUtils.uncapitalize(clz.getSimpleName()), definition);
		return definition;
	}

	private void registAllReceivers(Element element, ParserContext parserContext) {
		String[] classNames = finalAllResourceClassNames(element);
		for (String className : classNames) {
			try {
				Class<?> clz = Class.forName(className);
				if (clz.isAnnotationPresent(ReceiverAnno.class)) {
					BeanDefinitionBuilder builder = BeanDefinitionBuilder
							.rootBeanDefinition(clz);
					parserContext.getRegistry().registerBeanDefinition(
							StringUtils.uncapitalize(clz.getSimpleName()),
							builder.getBeanDefinition());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void registBeanPostProcessor(ParserContext context) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.rootBeanDefinition(EventReceiverBeanPostProcessor.class);
		context.getRegistry().registerBeanDefinition(
				StringUtils.uncapitalize(EventReceiverBeanPostProcessor.class
						.getSimpleName()), builder.getBeanDefinition());
	}

	private String[] finalAllResourceClassNames(Element element) {
		String[] basePackages = StringUtils.tokenizeToStringArray(
				element.getAttribute(BASE_PACKAGE_ATTRIBUTE),
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
		List<String> classNameList = new LinkedList<String>();
		for (String basePackage : basePackages) {
			classNameList.addAll(findResourceClassNames(basePackage));
		}
		return classNameList.toArray(new String[classNameList.size()]);
	}

	private List<String> findResourceClassNames(String basePackage) {
		List<String> result = new LinkedList<String>();
		try {
			String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
					+ resolveBasePackage(basePackage)
					+ "/"
					+ DEFAULT_RESOURCE_PATTERN;
			Resource[] resources = this.resourcePatternResolver
					.getResources(packageSearchPath);
			for (Resource resource : resources) {
				if (resource.isReadable()) {
					MetadataReader metadataReader = metadataReaderFactory
							.getMetadataReader(resource);
					result.add(metadataReader.getClassMetadata().getClassName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String resolveBasePackage(String basePackage) {
		return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils
				.resolvePlaceholders(basePackage));
	}
}
