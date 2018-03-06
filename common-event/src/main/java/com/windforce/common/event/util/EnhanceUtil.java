package com.windforce.common.event.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

import com.windforce.common.event.core.IReceiverInvoke;
import com.windforce.common.event.core.ReceiverDefintion;
import com.windforce.common.event.event.IEvent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;

public final class EnhanceUtil {

	private static final ClassPool classPool = ClassPool.getDefault();

	private static final AtomicInteger index = new AtomicInteger(0);

	public static IReceiverInvoke createReceiverInvoke(ReceiverDefintion defintion) throws Exception {
		Object bean = defintion.getBean();
		Method method = defintion.getMethod();
		String methodName = method.getName();
		Class<?> clz = bean.getClass();
		CtClass enhancedClz = buildCtClass(IReceiverInvoke.class);
		CtField field = new CtField(classPool.get(clz.getCanonicalName()), //
				"bean", enhancedClz);
		field.setModifiers(Modifier.PRIVATE);
		enhancedClz.addField(field);

		CtConstructor constructor = new CtConstructor(classPool.get(new String[] { clz.getCanonicalName() }),
				enhancedClz);
		constructor.setBody("{this.bean = $1;}");
		constructor.setModifiers(Modifier.PUBLIC);
		enhancedClz.addConstructor(constructor);

		CtMethod ctMethod = new CtMethod( //
				classPool.get(void.class.getCanonicalName()), //
				"invoke", //
				classPool.get(new String[] { IEvent.class.getCanonicalName() }), enhancedClz);
		ctMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(" bean." + methodName + "((" + defintion.getClz().getCanonicalName() + ")$1);");
		sb.append("}");
		ctMethod.setBody(sb.toString());
		enhancedClz.addMethod(ctMethod);

		// 添加一个equals方法
		CtMethod ectMethod = new CtMethod( //
				classPool.get(boolean.class.getCanonicalName()), //
				"equals", //
				classPool.get(new String[] { Object.class.getCanonicalName() }), enhancedClz);
		ectMethod.setModifiers(Modifier.PUBLIC);

		sb = new StringBuilder();
		sb.append("{");
		sb.append("com.windforce.common.event.core.IReceiverInvoke other = (com.windforce.common.event.core.IReceiverInvoke) $1;");
		sb.append("return bean.equals(other.getBean());");
		sb.append("}");
		ectMethod.setBody(sb.toString());
		enhancedClz.addMethod(ectMethod);

		// 添加一个hashcode方法
		CtMethod hctMethod = new CtMethod( //
				classPool.get(int.class.getCanonicalName()), //
				"hashCode", //
				classPool.get(new String[] {}), enhancedClz);
		hctMethod.setModifiers(Modifier.PUBLIC);

		sb = new StringBuilder();
		sb.append("{");
		sb.append(" return bean.hashCode();");
		sb.append("}");
		hctMethod.setBody(sb.toString());
		enhancedClz.addMethod(hctMethod);

		// 添加一个getBean方法
		CtMethod gctMethod = new CtMethod( //
				classPool.get(Object.class.getCanonicalName()), //
				"getBean", //
				classPool.get(new String[] {}), enhancedClz);
		gctMethod.setModifiers(Modifier.PUBLIC);

		sb = new StringBuilder();
		sb.append("{");
		sb.append("return bean;");
		sb.append("}");
		gctMethod.setBody(sb.toString());
		enhancedClz.addMethod(gctMethod);

		Class<?> rClz = enhancedClz.toClass();
		Constructor<?> con = rClz.getConstructor(clz);
		IReceiverInvoke result = (IReceiverInvoke) con.newInstance(bean);
		return result;
	}

	private static CtClass buildCtClass(Class<?> clz) throws Exception {
		CtClass result = classPool.makeClass(clz.getSimpleName() + "Enhance" + index.incrementAndGet());
		result.addInterface(classPool.get(clz.getCanonicalName()));
		return result;
	}
}
