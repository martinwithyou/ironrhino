package org.ironrhino.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

public class JavaSourceExecutor {

	public static void execute(String code) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager stdFileManager = compiler
				.getStandardFileManager(null, null, null);
		JavaFileManager fileManager = new ForwardingJavaFileManager(
				stdFileManager) {
			@Override
			public JavaFileObject getJavaFileForOutput(Location location,
					final String className, Kind kind, FileObject sibling)
					throws IOException {
				JavaFileObject jfo = super.getJavaFileForOutput(location,
						className, kind, sibling);
				return new ForwardingJavaFileObject(jfo) {
					@Override
					public OutputStream openOutputStream() throws IOException {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ByteArrayClassLoader.getInstance().defineClass(
								className, bos);
						return bos;
					}
				};
			}
		};
		code = complete(code);
		JavaSource source = new JavaSource(extractClassName(code), code);
		List<JavaFileObject> list = new ArrayList<JavaFileObject>();
		list.add(source);
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		compiler.getTask(null, fileManager, diagnostics, null, null, list)
				.call();
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics
				.getDiagnostics())
			System.out.format("Error [%s] on line %d in %s", diagnostic
					.getMessage(null), diagnostic.getLineNumber(), diagnostic
					.getSource().toUri());
		fileManager.close();

		Class clazz = ByteArrayClassLoader.getInstance().loadClass(
				source.getClassName());
		Object instance = clazz.newInstance();
		Method method = clazz.getMethod("run");
		if (method.getReturnType() == Void.TYPE) {
			int mod = method.getModifiers();
			if (!Modifier.isStatic(mod) && Modifier.isPublic(mod))
				method.invoke(instance);
		}
	}

	private static String complete(String code) {
		String className = extractClassName(code);
		if (className == null) {
			className = "C" + System.currentTimeMillis();
			StringBuilder sb = new StringBuilder();
			sb.append("public class ");
			sb.append(className);
			sb.append("{public void run(){");
			sb.append(code);
			sb.append("}}");
			code = sb.toString();
		}
		return code;
	}

	private static Pattern PACKAGE = Pattern.compile("package\\s+(\\w+)");
	private static Pattern CLASS = Pattern.compile("class\\s+(\\w+)");

	private static String extractClassName(String code) {
		String p = null;
		String c = null;
		Matcher m = PACKAGE.matcher(code);
		if (m.find())
			p = m.group(1);
		Matcher m2 = CLASS.matcher(code);
		if (m2.find())
			c = m2.group(1);
		if (c == null)
			return null;
		else
			return p == null ? c : p + "." + c;
	}

	static class JavaSource extends SimpleJavaFileObject {

		private String code;

		private String className;

		JavaSource(String className, String code) {
			super(URI.create("string:///" + className.replace('.', '/')
					+ Kind.SOURCE.extension), Kind.SOURCE);
			this.className = className;
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}

		public String getClassName() {
			return className;
		}

	}

	static class ByteArrayClassLoader extends ClassLoader {

		private static ByteArrayClassLoader instance = AccessController
				.doPrivileged(new PrivilegedAction<ByteArrayClassLoader>() {
					public ByteArrayClassLoader run() {
						return new ByteArrayClassLoader();
					}
				});

		private Map<String, ByteArrayOutputStream> bytes = new HashMap<String, ByteArrayOutputStream>();

		public static ByteArrayClassLoader getInstance() {
			return instance;
		}

		public Class findClass(String name) {
			byte[] classData = bytes.remove(name).toByteArray();
			return defineClass(name, classData, 0, classData.length);
		}

		public void defineClass(String name, ByteArrayOutputStream b) {
			bytes.put(name, b);
		}

	}

}
