/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.spring.boot.internal;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;

public class ClassGenerator implements Opcodes {
    /**
     * <code>
     * public class PaxLoggingSystem extends Slf4JLoggingSystem {
     * public PaxLoggingSystem(final ClassLoader loader) {
     * // no-op, inherit from OSGi one for now
     * super(loader);
     * }
     *
     * @Override protected String[] getStandardConfigLocations() {
     * return new String[0];
     * }
     * @Override protected void loadDefaults(final LoggingInitializationContext initializationContext,
     * final LogFile logFile) {
     * // no-op
     * }
     * }
     * </code>
     */
    public byte[] paxLoggingSystem() {
        final ClassWriter classWriter = new ClassWriter(COMPUTE_FRAMES);
        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER,
                "org/apache/karaf/spring/boot/embed/pax/logging/PaxLoggingSystem", null, "org/springframework/boot/logging/Slf4JLoggingSystem", null);
        classWriter.visitSource("PaxLoggingSystem.java", null);
        {
            final MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/ClassLoader;)V", null, null);
            methodVisitor.visitCode();
            final Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(13, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/logging/Slf4JLoggingSystem", "<init>", "(Ljava/lang/ClassLoader;)V", false);
            final Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(14, label1);
            methodVisitor.visitInsn(RETURN);
            final Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/embed/pax/logging/PaxLoggingSystem;", null, label0, label2, 0);
            methodVisitor.visitLocalVariable("loader", "Ljava/lang/ClassLoader;", null, label0, label2, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
            final MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PROTECTED, "getStandardConfigLocations", "()[Ljava/lang/String;", null, null);
            methodVisitor.visitCode();
            final Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(18, label0);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitTypeInsn(ANEWARRAY, "java/lang/String");
            methodVisitor.visitInsn(ARETURN);
            final Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/embed/pax/logging/PaxLoggingSystem;", null, label0, label1, 0);
            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
        {
            final MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PROTECTED, "loadDefaults", "(Lorg/springframework/boot/logging/LoggingInitializationContext;Lorg/springframework/boot/logging/LogFile;)V", null, null);
            methodVisitor.visitCode();
            final Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(25, label0);
            methodVisitor.visitInsn(RETURN);
            final Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/embed/pax/logging/PaxLoggingSystem;", null, label0, label1, 0);
            methodVisitor.visitLocalVariable("initializationContext", "Lorg/springframework/boot/logging/LoggingInitializationContext;", null, label0, label1, 1);
            methodVisitor.visitLocalVariable("logFile", "Lorg/springframework/boot/logging/LogFile;", null, label0, label1, 2);
            methodVisitor.visitMaxs(0, 3);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    public byte[] disableKarafTomcatURLStreamHandlerFactoryURLStreamHandlerRegistration() throws IOException {
        final byte[] bytes = readBytes("org/apache/catalina/webresources/TomcatURLStreamHandlerFactory.class");
        final ClassReader reader = new ClassReader(bytes);
        final ClassWriter writer = new KarafClassWriter(reader, COMPUTE_FRAMES, "org.apache.catalina.webresources.TomcatURLStreamHandlerFactory");
        reader.accept(new ClassRemapper(writer, new SimpleRemapper(
                "java/net/URL", "org/apache/karaf/spring/boot/internal/remapped/RemappedURL")), SKIP_FRAMES);
        return writer.toByteArray();
    }

    public byte[] karafLauncherClassLoaderUrlHandlerConnection() {
        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(V1_8, ACC_SUPER, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection", null, "java/net/URLConnection", null);

        classWriter.visitSource("KarafEnhancedChildLauncherClassLoader.java", null);

        classWriter.visitInnerClass("org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "StaticURLStreamHandler", ACC_PRIVATE | ACC_STATIC);

        classWriter.visitInnerClass("org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection", "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "StaticURLConnection", ACC_PRIVATE | ACC_STATIC);

        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "content", "Ljava/lang/String;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/net/URL;Ljava/lang/String;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(96, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/net/URLConnection", "<init>", "(Ljava/net/URL;)V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(97, label1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitFieldInsn(PUTFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection", "content", "Ljava/lang/String;");
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(98, label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection;", null, label0, label3, 0);
            methodVisitor.visitLocalVariable("u", "Ljava/net/URL;", null, label0, label3, 1);
            methodVisitor.visitLocalVariable("content", "Ljava/lang/String;", null, label0, label3, 2);
            methodVisitor.visitMaxs(2, 3);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "connect", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(103, label0);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection;", null, label0, label1, 0);
            methodVisitor.visitMaxs(0, 1);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getInputStream", "()Ljava/io/InputStream;", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(107, label0);
            methodVisitor.visitTypeInsn(NEW, "java/io/ByteArrayInputStream");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection", "content", "Ljava/lang/String;");
            methodVisitor.visitFieldInsn(GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;");
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayInputStream", "<init>", "([B)V", false);
            methodVisitor.visitInsn(ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection;", null, label0, label1, 0);
            methodVisitor.visitMaxs(4, 1);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    public byte[] karafLauncherClassLoaderUrlHandler() {
        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(V1_8, ACC_SUPER, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", null, "java/net/URLStreamHandler", null);

        classWriter.visitSource("KarafEnhancedChildLauncherClassLoader.java", null);

        classWriter.visitInnerClass("org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "StaticURLStreamHandler", ACC_PRIVATE | ACC_STATIC);

        classWriter.visitInnerClass("org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection", "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "StaticURLConnection", ACC_PRIVATE | ACC_STATIC);

        classWriter.visitInnerClass("org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$1", null, null, ACC_STATIC | ACC_SYNTHETIC);

        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "content", "Ljava/lang/String;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "<init>", "(Ljava/lang/String;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(83, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/net/URLStreamHandler", "<init>", "()V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(84, label1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitFieldInsn(PUTFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "content", "Ljava/lang/String;");
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(85, label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler;", null, label0, label3, 0);
            methodVisitor.visitLocalVariable("content", "Ljava/lang/String;", null, label0, label3, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PROTECTED, "openConnection", "(Ljava/net/URL;)Ljava/net/URLConnection;", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(89, label0);
            methodVisitor.visitTypeInsn(NEW, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "content", "Ljava/lang/String;");
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler$StaticURLConnection", "<init>", "(Ljava/net/URL;Ljava/lang/String;)V", false);
            methodVisitor.visitInsn(ARETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler;", null, label0, label1, 0);
            methodVisitor.visitLocalVariable("u", "Ljava/net/URL;", null, label0, label1, 1);
            methodVisitor.visitMaxs(4, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_SYNTHETIC, "<init>", "(Ljava/lang/String;Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$1;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(80, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "<init>", "(Ljava/lang/String;)V", false);
            methodVisitor.visitInsn(RETURN);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler;", null, label0, label1, 0);
            methodVisitor.visitLocalVariable("x0", "Ljava/lang/String;", null, label0, label1, 1);
            methodVisitor.visitLocalVariable("x1", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$1;", null, label0, label1, 2);
            methodVisitor.visitMaxs(2, 3);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    public byte[] karafLauncherClassLoader() {
        ClassWriter classWriter = new ClassWriter(0);
        FieldVisitor fieldVisitor;
        MethodVisitor methodVisitor;

        classWriter.visit(V1_8, ACC_PUBLIC | ACC_SUPER, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", null, "org/springframework/boot/loader/LaunchedURLClassLoader", null);

        classWriter.visitSource("KarafEnhancedChildLauncherClassLoader.java", null);

        classWriter.visitInnerClass("org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$1", null, null, ACC_STATIC | ACC_SYNTHETIC);

        classWriter.visitInnerClass("org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "StaticURLStreamHandler", ACC_PRIVATE | ACC_STATIC);

        classWriter.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", ACC_PUBLIC | ACC_FINAL | ACC_STATIC);

        {
            fieldVisitor = classWriter.visitField(ACC_PRIVATE | ACC_FINAL, "integrations", "Lorg/apache/karaf/spring/boot/internal/ClassGenerator;", null, null);
            fieldVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(ZLorg/springframework/boot/loader/archive/Archive;[Ljava/net/URL;Ljava/lang/ClassLoader;)V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(47, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ILOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/LaunchedURLClassLoader", "<init>", "(ZLorg/springframework/boot/loader/archive/Archive;[Ljava/net/URL;Ljava/lang/ClassLoader;)V", false);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(48, label1);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitTypeInsn(NEW, "org/apache/karaf/spring/boot/internal/ClassGenerator");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/apache/karaf/spring/boot/internal/ClassGenerator", "<init>", "()V", false);
            methodVisitor.visitFieldInsn(PUTFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "integrations", "Lorg/apache/karaf/spring/boot/internal/ClassGenerator;");
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(49, label2);
            methodVisitor.visitInsn(RETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader;", null, label0, label3, 0);
            methodVisitor.visitLocalVariable("exploded", "Z", null, label0, label3, 1);
            methodVisitor.visitLocalVariable("rootArchive", "Lorg/springframework/boot/loader/archive/Archive;", null, label0, label3, 2);
            methodVisitor.visitLocalVariable("urls", "[Ljava/net/URL;", null, label0, label3, 3);
            methodVisitor.visitLocalVariable("parent", "Ljava/lang/ClassLoader;", null, label0, label3, 4);
            methodVisitor.visitMaxs(5, 5);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PROTECTED, "loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;", "(Ljava/lang/String;Z)Ljava/lang/Class<*>;", new String[] { "java/lang/ClassNotFoundException" });
            methodVisitor.visitCode();
            Label label0 = new Label();
            Label label1 = new Label();
            Label label2 = new Label();
            methodVisitor.visitTryCatchBlock(label0, label1, label2, null);
            Label label3 = new Label();
            Label label4 = new Label();
            methodVisitor.visitTryCatchBlock(label3, label4, label2, null);
            Label label5 = new Label();
            methodVisitor.visitTryCatchBlock(label2, label5, label2, null);
            Label label6 = new Label();
            methodVisitor.visitLabel(label6);
            methodVisitor.visitLineNumber(53, label6);
            methodVisitor.visitVarInsn(ALOAD, 1);
            Label label7 = new Label();
            methodVisitor.visitJumpInsn(IFNONNULL, label7);
            Label label8 = new Label();
            methodVisitor.visitLabel(label8);
            methodVisitor.visitLineNumber(54, label8);
            methodVisitor.visitTypeInsn(NEW, "java/lang/ClassNotFoundException");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitLdcInsn("<null>");
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/ClassNotFoundException", "<init>", "(Ljava/lang/String;)V", false);
            methodVisitor.visitInsn(ATHROW);
            methodVisitor.visitLabel(label7);
            methodVisitor.visitLineNumber(56, label7);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "integrations", "Lorg/apache/karaf/spring/boot/internal/ClassGenerator;");
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/apache/karaf/spring/boot/internal/ClassGenerator", "shouldLoadFromBundle", "(Ljava/lang/String;)Z", false);
            Label label9 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label9);
            Label label10 = new Label();
            methodVisitor.visitLabel(label10);
            methodVisitor.visitLineNumber(57, label10);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "integrations", "Lorg/apache/karaf/spring/boot/internal/ClassGenerator;");
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/apache/karaf/spring/boot/internal/ClassGenerator", "loadFromBundle", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(label9);
            methodVisitor.visitLineNumber(59, label9);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "getClassLoadingLock", "(Ljava/lang/String;)Ljava/lang/Object;", false);
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitVarInsn(ASTORE, 3);
            methodVisitor.visitInsn(MONITORENTER);
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(60, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "findLoadedClass", "(Ljava/lang/String;)Ljava/lang/Class;", false);
            methodVisitor.visitVarInsn(ASTORE, 4);
            Label label11 = new Label();
            methodVisitor.visitLabel(label11);
            methodVisitor.visitLineNumber(61, label11);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitJumpInsn(IFNONNULL, label3);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "integrations", "Lorg/apache/karaf/spring/boot/internal/ClassGenerator;");
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/apache/karaf/spring/boot/internal/ClassGenerator", "handlesInChild", "(Ljava/lang/String;)Z", false);
            methodVisitor.visitJumpInsn(IFEQ, label3);
            Label label12 = new Label();
            methodVisitor.visitLabel(label12);
            methodVisitor.visitLineNumber(62, label12);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "integrations", "Lorg/apache/karaf/spring/boot/internal/ClassGenerator;");
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/apache/karaf/spring/boot/internal/ClassGenerator", "findByteCode", "(Ljava/lang/String;)[B", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "loadIntegrationClass", "(Ljava/lang/String;Z[B)Ljava/lang/Class;", false);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitInsn(MONITOREXIT);
            methodVisitor.visitLabel(label1);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(64, label3);
            methodVisitor.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/lang/Object"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitInsn(MONITOREXIT);
            methodVisitor.visitLabel(label4);
            Label label13 = new Label();
            methodVisitor.visitJumpInsn(GOTO, label13);
            methodVisitor.visitLabel(label2);
            methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Throwable"});
            methodVisitor.visitVarInsn(ASTORE, 5);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitInsn(MONITOREXIT);
            methodVisitor.visitLabel(label5);
            methodVisitor.visitVarInsn(ALOAD, 5);
            methodVisitor.visitInsn(ATHROW);
            methodVisitor.visitLabel(label13);
            methodVisitor.visitLineNumber(65, label13);
            methodVisitor.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ILOAD, 2);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/LaunchedURLClassLoader", "loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;", false);
            methodVisitor.visitInsn(ARETURN);
            Label label14 = new Label();
            methodVisitor.visitLabel(label14);
            methodVisitor.visitLocalVariable("existing", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", label11, label3, 4);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader;", null, label6, label14, 0);
            methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label6, label14, 1);
            methodVisitor.visitLocalVariable("resolve", "Z", null, label6, label14, 2);
            methodVisitor.visitMaxs(5, 6);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "getResources", "(Ljava/lang/String;)Ljava/util/Enumeration;", "(Ljava/lang/String;)Ljava/util/Enumeration<Ljava/net/URL;>;", new String[]{"java/io/IOException"});
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(67, label0);
            methodVisitor.visitLdcInsn("META-INF/spring.factories");
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            Label label1 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label1);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(68, label2);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(69, label3);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "handleSpringFactories", "(Ljava/lang/String;)Ljava/util/stream/Stream;", false);
            methodVisitor.visitTypeInsn(NEW, "java/net/URL");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitLdcInsn("karaf-spring-boot");
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitInsn(ICONST_M1);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitTypeInsn(NEW, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler");
            methodVisitor.visitInsn(DUP);
            methodVisitor.visitLdcInsn("org.springframework.boot.SpringApplicationRunListener=org.apache.karaf.spring.boot.services.base.SpringApplicationContextCapture");
            methodVisitor.visitInsn(ACONST_NULL);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$StaticURLStreamHandler", "<init>", "(Ljava/lang/String;Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader$1;)V", false);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/net/URL", "<init>", "(Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/net/URLStreamHandler;)V", false);
            Label label4 = new Label();
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLineNumber(70, label4);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/stream/Stream", "of", "(Ljava/lang/Object;)Ljava/util/stream/Stream;", true);
            Label label5 = new Label();
            methodVisitor.visitLabel(label5);
            methodVisitor.visitLineNumber(68, label5);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/stream/Stream", "concat", "(Ljava/util/stream/Stream;Ljava/util/stream/Stream;)Ljava/util/stream/Stream;", true);
            Label label6 = new Label();
            methodVisitor.visitLabel(label6);
            methodVisitor.visitLineNumber(72, label6);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/stream/Collectors", "toList", "()Ljava/util/stream/Collector;", false);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/stream/Stream", "collect", "(Ljava/util/stream/Collector;)Ljava/lang/Object;", true);
            methodVisitor.visitTypeInsn(CHECKCAST, "java/util/Collection");
            Label label7 = new Label();
            methodVisitor.visitLabel(label7);
            methodVisitor.visitLineNumber(68, label7);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "enumeration", "(Ljava/util/Collection;)Ljava/util/Enumeration;", false);
            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(74, label1);
            methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/LaunchedURLClassLoader", "getResources", "(Ljava/lang/String;)Ljava/util/Enumeration;", false);
            methodVisitor.visitInsn(ARETURN);
            Label label8 = new Label();
            methodVisitor.visitLabel(label8);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader;", null, label0, label8, 0);
            methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label8, 1);
            methodVisitor.visitMaxs(11, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "handleSpringFactories", "(Ljava/lang/String;)Ljava/util/stream/Stream;", "(Ljava/lang/String;)Ljava/util/stream/Stream<Ljava/net/URL;>;", new String[]{"java/io/IOException"});
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(78, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/LaunchedURLClassLoader", "getResources", "(Ljava/lang/String;)Ljava/util/Enumeration;", false);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "list", "(Ljava/util/Enumeration;)Ljava/util/ArrayList;", false);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "stream", "()Ljava/util/stream/Stream;", false);
            methodVisitor.visitInvokeDynamicInsn("apply", "()Ljava/util/function/Function;", new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false), new Object[]{Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"), new Handle(Opcodes.H_INVOKESTATIC, "org/apache/karaf/spring/boot/internal/factories/SpringFactoriesRewriter", "rewrite", "(Ljava/net/URL;)Ljava/util/stream/Stream;", false), Type.getType("(Ljava/net/URL;)Ljava/util/stream/Stream;")});
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(79, label1);
            methodVisitor.visitMethodInsn(INVOKEINTERFACE, "java/util/stream/Stream", "flatMap", "(Ljava/util/function/Function;)Ljava/util/stream/Stream;", true);
            Label label2 = new Label();
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(78, label2);
            methodVisitor.visitInsn(ARETURN);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader;", null, label0, label3, 0);
            methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label3, 1);
            methodVisitor.visitMaxs(2, 2);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "loadIntegrationClass", "(Ljava/lang/String;Z[B)Ljava/lang/Class;", "(Ljava/lang/String;Z[B)Ljava/lang/Class<*>;", null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(83, label0);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitInsn(ICONST_0);
            methodVisitor.visitVarInsn(ALOAD, 3);
            methodVisitor.visitInsn(ARRAYLENGTH);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/LaunchedURLClassLoader", "defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;", false);
            methodVisitor.visitVarInsn(ASTORE, 4);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(84, label1);
            methodVisitor.visitVarInsn(ILOAD, 2);
            Label label2 = new Label();
            methodVisitor.visitJumpInsn(IFEQ, label2);
            Label label3 = new Label();
            methodVisitor.visitLabel(label3);
            methodVisitor.visitLineNumber(85, label3);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "resolveClass", "(Ljava/lang/Class;)V", false);
            methodVisitor.visitLabel(label2);
            methodVisitor.visitLineNumber(87, label2);
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Class"}, 0, null);
            methodVisitor.visitVarInsn(ALOAD, 4);
            methodVisitor.visitInsn(ARETURN);
            Label label4 = new Label();
            methodVisitor.visitLabel(label4);
            methodVisitor.visitLocalVariable("this", "Lorg/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader;", null, label0, label4, 0);
            methodVisitor.visitLocalVariable("name", "Ljava/lang/String;", null, label0, label4, 1);
            methodVisitor.visitLocalVariable("resolve", "Z", null, label0, label4, 2);
            methodVisitor.visitLocalVariable("bytes", "[B", null, label0, label4, 3);
            methodVisitor.visitLocalVariable("value", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", label1, label4, 4);
            methodVisitor.visitMaxs(5, 5);
            methodVisitor.visitEnd();
        }
        {
            methodVisitor = classWriter.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            methodVisitor.visitCode();
            Label label0 = new Label();
            methodVisitor.visitLabel(label0);
            methodVisitor.visitLineNumber(40, label0);
            methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/ClassLoader", "registerAsParallelCapable", "()Z", false);
            methodVisitor.visitInsn(POP);
            Label label1 = new Label();
            methodVisitor.visitLabel(label1);
            methodVisitor.visitLineNumber(41, label1);
            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(1, 0);
            methodVisitor.visitEnd();
        }
        classWriter.visitEnd();

        return classWriter.toByteArray();
    }

    public byte[] patchLauncher(final byte[] original, final String archiveOrExplodedFolder) {
        final ClassReader reader = new ClassReader(original);
        final ClassWriter writer = new KarafClassWriter(reader, COMPUTE_FRAMES, "org.springframework.boot.loader.Launcher");
        reader.accept(new ClassVisitor(ASM8, writer) {
            @Override
            public MethodVisitor visitMethod(final int access, final String name, final String descriptor,
                                             final String signature, final String[] exceptions) {
                switch (name) {
                    case "createClassLoader":
                        if ("([Ljava/net/URL;)Ljava/lang/ClassLoader;".equals(descriptor)) { // ClassLoader createClassLoader(URL[])
                            // return new LaunchedURLClassLoader(isExploded(), getArchive(), urls, getClass().getClassLoader());
                            final MethodVisitor methodVisitor = super.visitMethod(
                                    ACC_PROTECTED, name, "([Ljava/net/URL;)Ljava/lang/ClassLoader;", null, new String[]{"java/lang/Exception"});
                            methodVisitor.visitCode();
                            final Label label0 = new Label();
                            methodVisitor.visitLabel(label0);
                            methodVisitor.visitLineNumber(12, label0);
                            methodVisitor.visitTypeInsn(NEW, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader");
                            methodVisitor.visitInsn(DUP);
                            methodVisitor.visitVarInsn(ALOAD, 0);
                            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/Launcher", "isExploded", "()Z", false);
                            methodVisitor.visitVarInsn(ALOAD, 0);
                            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/Launcher", "getArchive", "()Lorg/springframework/boot/loader/archive/Archive;", false);
                            methodVisitor.visitVarInsn(ALOAD, 1);
                            methodVisitor.visitVarInsn(ALOAD, 0);
                            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                            methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
                            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/apache/karaf/spring/boot/internal/KarafEnhancedChildLauncherClassLoader", "<init>", "(ZLorg/springframework/boot/loader/archive/Archive;[Ljava/net/URL;Ljava/lang/ClassLoader;)V", false);
                            methodVisitor.visitInsn(ARETURN);
                            final Label label1 = new Label();
                            methodVisitor.visitLabel(label1);
                            methodVisitor.visitLocalVariable("this", "Lorg/springframework/boot/loader/Launcher;", null, label0, label1, 0);
                            methodVisitor.visitLocalVariable("urls", "[Ljava/net/URL;", null, label0, label1, 1);
                            methodVisitor.visitMaxs(0, 0);
                            methodVisitor.visitEnd();
                            return super.visitMethod(access, name + "_karaf_spring_boot_integration_disabled", descriptor, signature, exceptions);
                        }
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    case "createArchive": { // return (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
                        final MethodVisitor methodVisitor = super.visitMethod(
                                ACC_PROTECTED | ACC_FINAL, name,
                                "()Lorg/springframework/boot/loader/archive/Archive;", null, new String[]{"java/lang/Exception"});
                        methodVisitor.visitCode();
                        final Label label0 = new Label();
                        methodVisitor.visitLabel(label0);
                        methodVisitor.visitLineNumber(13, label0);
                        methodVisitor.visitTypeInsn(NEW, "java/io/File");
                        methodVisitor.visitInsn(DUP);
                        methodVisitor.visitLdcInsn(archiveOrExplodedFolder);
                        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
                        methodVisitor.visitVarInsn(ASTORE, 1);
                        final Label label1 = new Label();
                        methodVisitor.visitLabel(label1);
                        methodVisitor.visitLineNumber(14, label1);
                        methodVisitor.visitVarInsn(ALOAD, 1);
                        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "isDirectory", "()Z", false);
                        final Label label2 = new Label();
                        methodVisitor.visitJumpInsn(IFEQ, label2);
                        methodVisitor.visitTypeInsn(NEW, "org/springframework/boot/loader/archive/ExplodedArchive");
                        methodVisitor.visitInsn(DUP);
                        methodVisitor.visitVarInsn(ALOAD, 1);
                        methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/archive/ExplodedArchive", "<init>", "(Ljava/io/File;)V", false);
                        final Label label3 = new Label();
                        methodVisitor.visitJumpInsn(GOTO, label3);
                        methodVisitor.visitLabel(label2);
                        methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/io/File"}, 0, null);
                        methodVisitor.visitTypeInsn(NEW, "org/springframework/boot/loader/archive/JarFileArchive");
                        methodVisitor.visitInsn(DUP);
                        methodVisitor.visitVarInsn(ALOAD, 1);
                        methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/springframework/boot/loader/archive/JarFileArchive", "<init>", "(Ljava/io/File;)V", false);
                        methodVisitor.visitLabel(label3);
                        methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"org/springframework/boot/loader/archive/Archive"});
                        methodVisitor.visitInsn(ARETURN);
                        final Label label4 = new Label();
                        methodVisitor.visitLabel(label4);
                        methodVisitor.visitLocalVariable("this", "L" + name + ";", null, label0, label4, 0);
                        methodVisitor.visitLocalVariable("root", "Ljava/io/File;", null, label1, label4, 1);
                        methodVisitor.visitMaxs(0, 0);
                        methodVisitor.visitEnd();
                        // keep it for now (if we need later) but renamed to disable it
                        return super.visitMethod(access, name + "_karaf_spring_boot_integration_disabled", descriptor, signature, exceptions);
                    }
                    default:
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            }
        }, SKIP_FRAMES);
        return writer.toByteArray();
    }

    // used by child loader
    public boolean handlesInChild(final String name) {
        if (name == null || !(name.startsWith("org.apache.karaf.spring.boot.") || name.startsWith("org.apache.catalina.webresources."))) {
            return false;
        }
        return "org.apache.karaf.spring.boot.embed.pax.logging.PaxLoggingSystem".equals(name) ||
                "org.apache.catalina.webresources.TomcatURLStreamHandlerFactory".equals(name) ||
                name.startsWith("org.apache.karaf.spring.boot.services.");
    }

    // used by child loader
    public byte[] findByteCode(final String name) throws ClassNotFoundException {
        switch (name) {
            case "org.apache.karaf.spring.boot.embed.pax.logging.PaxLoggingSystem":
                return paxLoggingSystem();
            case "org.apache.catalina.webresources.TomcatURLStreamHandlerFactory":
                try {
                    return disableKarafTomcatURLStreamHandlerFactoryURLStreamHandlerRegistration();
                } catch (final IOException e) {
                    throw new IllegalStateException(e);
                }
            default:
                if (name.startsWith("org.apache.karaf.spring.boot.services.")) {
                    try {
                        return readBytes(name.replace('.', '/') + ".class");
                    } catch (final IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
                if (name.startsWith("javax.servlet") /*should be @ConditionalOnProperty - see HttpServiceWebServer*/) {
                    throw new ClassNotFoundException(name);
                }
                throw new IllegalArgumentException(name);
        }
    }

    public boolean shouldLoadFromBundle(final String name) {
        return name != null && // todo: rewrite it with substring, it is faster
                (/*should be @ConditionalOnProperty - see HttpServiceWebServer*/
                        name.startsWith("javax.servlet.annotation.") ||
                                name.startsWith("javax.servlet.descriptor.") ||
                                name.startsWith("javax.servlet.http.") ||
                                (name.startsWith("javax.servlet.") && name.lastIndexOf('.') == "javax.servlet".length())
                );
    }

    public Class<?> loadFromBundle(final String name) throws ClassNotFoundException {
        return getClass().getClassLoader().loadClass(name);
    }

    private byte[] readBytes(final String name) throws IOException {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] tmp = new byte[4 * 1024];
        int read;
        try (final InputStream stream = requireNonNull(tccl.getResourceAsStream(name), "Can't find '" + name + "'")) {
            while ((read = stream.read(tmp)) >= 0) {
                if (read > 0) {
                    buffer.write(tmp, 0, read);
                }
            }
        }
        return buffer.toByteArray();
    }

    private static class KarafClassWriter extends ClassWriter {
        private final String clazz;

        private KarafClassWriter(final ClassReader reader, final int flags, final String clazz) {
            super(reader, flags);
            this.clazz = clazz;
        }

        @Override
        protected String getCommonSuperClass(final String type1, final String type2) {
            final ClassLoader loader = createTempLoader();
            Class<?> c, d;
            try {
                c = findClass(loader, type1.replace('/', '.'));
                d = findClass(loader, type2.replace('/', '.'));
            } catch (final Exception e) {
                throw new RuntimeException(e.toString());
            } catch (final ClassCircularityError e) {
                return "java/lang/Object";
            }
            if (c.isAssignableFrom(d)) {
                return type1;
            }
            if (d.isAssignableFrom(c)) {
                return type2;
            }
            if (c.isInterface() || d.isInterface()) {
                return "java/lang/Object";
            } else {
                do {
                    c = c.getSuperclass();
                } while (!c.isAssignableFrom(d));
                return c.getName().replace('.', '/');
            }
        }

        // todo if needed
        private ClassLoader createTempLoader() {
            return Thread.currentThread().getContextClassLoader();
        }

        protected Class<?> findClass(final ClassLoader tccl, final String className)
                throws ClassNotFoundException {
            try {
                return clazz.equals(className) ? Object.class : Class.forName(className, false, tccl);
            } catch (ClassNotFoundException e) {
                return Class.forName(className, false, getClass().getClassLoader());
            }
        }
    }
}
