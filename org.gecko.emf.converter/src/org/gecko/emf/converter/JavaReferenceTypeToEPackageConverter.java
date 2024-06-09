/**
 * Copyright (c) 2012 - 2024 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.converter;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EPackage;

/**
 * Java Reference Type to EPackage converter
 * 
 * @author Michal H. Siemaszko
 */
public class JavaReferenceTypeToEPackageConverter extends JavaToEPackageConverter {

	private JavaReferenceTypeToEPackageConverter() {
		// Do not instantiate. This is a utility class.
	}

	@SafeVarargs
	public static EPackage convert(String packageName, String nsURI, String nsPrefix, Class<?>... classes) {
		return JavaToEPackageConverter.convert(packageName, nsURI, nsPrefix, classes);
	}

	public static EPackage convert(String packageName, String nsURI, String nsPrefix, Path mainJarFilePath,
			Path... dependenciesJarFilePaths) throws ClassNotFoundException, IOException {
		Set<Class<?>> classes = getClassesFromJarFile(mainJarFilePath, dependenciesJarFilePaths);

		return convert(packageName, nsURI, nsPrefix, classes.toArray(Class[]::new));
	}

	private static Set<String> getClassNamesFromJarFile(Path jarFilePath) throws IOException {
		Set<String> classNames = new TreeSet<>();

		try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFilePath.toFile()))) {
			JarEntry jarEntry;

			while ((jarEntry = jarStream.getNextJarEntry()) != null) {
				String jarEntryName = jarEntry.getName();
				if (isJarEntryNameValid(jarEntryName)) {
					// @formatter:off
					String className = jarEntryName
							.replace("/", ".")
							.replace(".class", "");
					// @formatter:on
					classNames.add(className);
				}
			}
		}

		return classNames;
	}

	private static boolean isJarEntryNameValid(String jarEntryName) {
		return (jarEntryName.endsWith(".class")
				&& (!("module-info.class").equals(jarEntryName) && !jarEntryName.endsWith("$1.class")));
	}

	private static Set<Class<?>> getClassesFromJarFile(Path mainJarFilePath, Path... dependenciesJarFilePaths)
			throws IOException, ClassNotFoundException {
		Set<String> classNames = getClassNamesFromJarFile(mainJarFilePath);

		List<URL> jarFilesURLs = constructJarFilesURLs(mainJarFilePath, dependenciesJarFilePaths);

		return loadClassesFromJarFiles(jarFilesURLs, classNames);
	}

	private static Set<Class<?>> loadClassesFromJarFiles(List<URL> jarFilesURLs, Set<String> classNames)
			throws ClassNotFoundException, IOException {
		Set<Class<?>> classes = new TreeSet<Class<?>>(new Comparator<Class<?>>() {
			@Override
			public int compare(Class<?> c1, Class<?> c2) {
				return c1.getName().compareTo(c2.getName());
			}
		});

		try (URLClassLoader cl = URLClassLoader.newInstance(jarFilesURLs.toArray(URL[]::new))) {
			for (String name : classNames) {
				Class<?> clazz = cl.loadClass(name);
				classes.add(clazz);
			}
		}

		return classes;
	}

	private static List<URL> constructJarFilesURLs(Path mainJarFilePath, Path... dependenciesJarFilePaths)
			throws MalformedURLException {
		// @formatter:off
		return mergeJarFilePaths(mainJarFilePath, dependenciesJarFilePaths).stream()
				.map(jfp -> constructJarFileURL(jfp))
				.collect(Collectors.toList());
		// @formatter:on
	}

	private static URL constructJarFileURL(Path jarFilePath) {
		try {
			return new URL("jar:file:" + jarFilePath.toFile() + "!/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<Path> mergeJarFilePaths(Path mainJarFilePath, Path... dependenciesJarFilePaths) {
		List<Path> jarFilePaths = new ArrayList<Path>();
		jarFilePaths.add(mainJarFilePath);
		jarFilePaths.addAll(Arrays.asList(dependenciesJarFilePaths));
		return jarFilePaths;
	}
}
