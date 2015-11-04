/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.AnnotationUtils.*;
import static org.junit.gen5.commons.util.ReflectionUtils.loadClass;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.TestDescriptor;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class ClassNameTestDescriptorResolver
		implements TestDescriptorResolver<ClassNameSpecification, ClassTestDescriptor> {

	private static final TestClassTester classTester = new TestClassTester();

	private static final TestMethodTester methodTester = new TestMethodTester();

	@Override
	public ClassTestDescriptor resolve(TestDescriptor parent, ClassNameSpecification element) {
		Class<?> clazz = null;
		try {
			clazz = loadClass(element.getClassName());
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
		if (classTester.accept(clazz)) {
			return null;
//			return new ClassTestDescriptor(clazz, parent);
		}
		else {
			return null;
		}
	}

	@Override
	public List<TestDescriptor> resolveChildren(ClassTestDescriptor parent, ClassNameSpecification element) {
		if (parent.getUniqueId().endsWith(element.getClassName())) {
			// TODO Retrieve children resolvers according to type
			List<TestDescriptor> children = new LinkedList<>();

			List<Method> testMethodCandidates = findMethods(parent.getTestClass(), methodTester::accept,
				MethodSortOrder.HierarchyDown);

			// @formatter:off
			children.addAll(testMethodCandidates.stream()
				.map(method -> new MethodTestDescriptor("a unique id", method))
//				.map(method -> new MethodTestDescriptor(method, parent))
				.collect(toList()));
			// @formatter:on

			// @formatter:off
// Disabled following code since it will lead to stack overflow when a nested class is present in test class
//			List<ClassTestDescriptor> groups = Arrays.stream(parent.getTestClass().getDeclaredClasses())
//				.filter(Class::isMemberClass)
//				.map(clazz -> new ClassTestDescriptor(clazz, parent))
//				.collect(toList());
//			children.addAll(groups);
			// @formatter:on
			//
			//			groups.forEach(group -> children.addAll(resolveChildren(group, element)));

			return children;
		}
		else {
			ClassTestDescriptor child = resolve(parent, element);
			List<TestDescriptor> children = resolveChildren(child, element);
			List<TestDescriptor> result = new LinkedList<>();
			result.add(child);
			result.addAll(children);
			return result;
		}
	}

}
