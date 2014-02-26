package com.dafttech.classloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassLoader {
    private File sourceDir = null;
    private String sourcePackage = "";
    private boolean isJarfile = false;
    private boolean canLoadItself = false;
    private boolean correctPathSpaces = true;
    private List<Class<?>> loaded = new ArrayList<Class<?>>();

    public ClassLoader(File dir) {
        dir = new File(dir.toString().replace("\\", "/"));
        if (dir.toString().startsWith("file:") && dir.toString().endsWith("!")) {
            isJarfile = true;
            sourceDir = new File(dir.toString().substring(6, dir.toString().length() - 1));
        } else {
            sourceDir = dir;
        }
    }

    public ClassLoader setSourcePackage(String packageName) {
        if (packageName != null) sourcePackage = packageName.replace("\\", ".").replace("/", ".");
        return this;
    }

    public ClassLoader setCanLoadItself(boolean value) {
        canLoadItself = value;
        return this;
    }

    public ClassLoader setCorrectPathSpaces(boolean value) {
        correctPathSpaces = value;
        return this;
    }

    public ClassLoader load() {
        if (correctPathSpaces) correctPathSpaces();
        if (isJarfile) {
            loadJar(sourceDir);
        } else {
            loadDir(sourceDir);
        }
        return this;
    }

    public List<Class<?>> getLoaded() {
        return loaded;
    }

    private void loadDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return;
        for (File file : files) {
            if (file.isDirectory()) {
                loadDir(file);
            } else if (file.getName().endsWith(".class")) {
                loadClass(file);
            }
        }
    }

    private void loadJar(File dir) {
        try {
            JarFile jarfile = new JarFile(dir);
            Enumeration<JarEntry> entries = jarfile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    loadClass(new File(dir.getAbsolutePath() + "/" + entry.getName()));
                }
            }
            jarfile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadClass(File dir) {
        String currPckge = getCurrentPackage(dir.getName().endsWith(".class") ? new File(dir.getAbsolutePath().substring(0,
                dir.getAbsolutePath().length() - 6)) : dir);
        if (currPckge.startsWith(sourcePackage)) {
            try {
                URL[] urls = { sourceDir.toURI().toURL() };
                URLClassLoader classloader = URLClassLoader.newInstance(urls, getClass().getClassLoader());
                Class<?> tmpClass = classloader.loadClass(currPckge);
                if (tmpClass != null && (canLoadItself || tmpClass != getClass())) loaded.add(tmpClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NoClassDefFoundError e) {
                e.printStackTrace();
            }
        }
    }

    private String getCurrentPackage(File dir) {
        if (!dir.getAbsolutePath().startsWith(sourceDir.getAbsolutePath())
                || dir.getAbsolutePath().length() <= sourceDir.getAbsolutePath().length()) return "";
        return dir.getAbsolutePath().substring(sourceDir.getAbsolutePath().length() + 1).replace("\\", ".").replace("/", ".");
    }

    private void correctPathSpaces() {
        sourceDir = new File(sourceDir.toString().replace("%20", " "));
    }

    public static File getCurrentPath() {
        try {
            Enumeration<URL> resources;
            String parentPackage = "";
            for (Package pck : Package.getPackages()) {
                parentPackage = pck.getName().contains(".") ? pck.getName().substring(0, pck.getName().indexOf(".")) : pck.getName();
                resources = java.lang.ClassLoader.getSystemResources(parentPackage);
                if (resources.hasMoreElements()) {
                    String currPath = resources.nextElement().getFile();
                    if (currPath.endsWith(parentPackage))
                        return new File(currPath.substring(0, currPath.length() - parentPackage.length()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
