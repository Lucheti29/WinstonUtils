package com.luksdlt92.winstonutils

import com.luksdlt92.winstonutils.exceptions.InvalidJarLoadException
import com.luksdlt92.winstonutils.exceptions.ResourcesLoadException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.util.jar.Attributes
import java.util.jar.JarFile

class JarHelper {
    companion object {

        @JvmStatic fun writeFileFromResources(loader: ClassLoader?, folderPath: String, fileName: String): Boolean {
            try {
                val folder = File(folderPath)

                if (loader == null) throw ResourcesLoadException("The class loader is null")

                if (!folder.exists() && !folder.mkdirs()) {
                    throw ResourcesLoadException("The folder for $fileName couldn't be created")
                }

                val existingFile = File(folderPath + fileName)
                if (existingFile.exists()) return true

                val inputStream = loader.getResourceAsStream(fileName) ?: throw ResourcesLoadException("The config.conf file for $fileName doesn't exist")

                try {
                    writeFile(inputStream, folderPath + fileName)
                    return true
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } catch (e: ResourcesLoadException) {
                //LOGGER.log(Level.SEVERE, e.getMessage())
                println("Something went wrong 1")
            }

            return false
        }

        @JvmStatic fun loadJar(loader: ClassLoader?, jar: File?, superClassName: String?): Any? {
            try {
                val classPath: String?
                val classUrl: URL
                val classUrls: Array<URL>

                if (loader == null) throw InvalidJarLoadException("The class loader is null")
                if (jar == null) throw InvalidJarLoadException("The jar file is null")

                try {
                    classPath = getMainClassFromJar(jar)
                } catch (ex: IOException) {
                    throw InvalidJarLoadException(jar.name + " couldn't be loaded")
                }

                try {
                    classUrl = jar.toURI().toURL()
                    classUrls = arrayOf(classUrl)
                } catch (ex: MalformedURLException) {
                    throw InvalidJarLoadException("File URL malformed " + jar.name)
                }

                val child = URLClassLoader(classUrls, loader)
                val classToLoad: Class<*>

                try {
                    classToLoad = Class.forName(classPath, true, child)
                } catch (ex: ClassNotFoundException) {
                    throw InvalidJarLoadException("Cannot find main class " + classPath!!)
                }

                if (superClassName != null && !classToLoad.superclass.simpleName.equals("BaseEventContainer", ignoreCase = true)) {
                    throw InvalidJarLoadException("Wrong inheritance for " + classToLoad.simpleName)
                }

                try {
                    return classToLoad.newInstance()
                } catch (ex: InstantiationException) {
                    throw InvalidJarLoadException("The class " + classToLoad.simpleName + " cannot be instantiated")
                } catch (ex: IllegalAccessException) {
                    throw InvalidJarLoadException("The class " + classToLoad.simpleName + " cannot be accessed")
                }

            } catch (ex: InvalidJarLoadException) {
                //LOGGER.log(Level.SEVERE, ex.getMessage())
                println("Something went wrong 2")
            }

            return null
        }

        @Throws(IOException::class)
        private fun getMainClassFromJar(jar: File): String? {
            // Open the JAR file
            val jarfile = JarFile(jar)

            // Get the manifest
            val manifest = jarfile.manifest

            // Get the main attributes in the manifest
            val attrs = manifest.mainAttributes

            // Enumerate each attribute
            val it = attrs.keys.iterator()
            while (it.hasNext()) {
                // Get attribute name
                val attrName = it.next() as Attributes.Name

                if (attrName.toString().equals("Main-Class", ignoreCase = true)) {
                    return attrs.getValue(attrName)
                }
            }
            return null
        }

        @Throws(IOException::class)
        private fun writeFile(inputStream: InputStream, path: String) {
            val os = FileOutputStream(path)

            val buffer = ByteArray(inputStream.available())

            var bytesRead: Int
            //read from is to buffer
            bytesRead = inputStream.read(buffer)

            while (bytesRead != -1) {
                os.write(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer)
            }

            inputStream.close()

            //flush OutputStream to write any buffered data to file
            os.flush()
            os.close()
        }

    }
}