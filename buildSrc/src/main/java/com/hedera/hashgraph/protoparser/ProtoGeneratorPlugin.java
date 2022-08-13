package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3BaseListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import com.hedera.hashgraph.protoparser.grammar.Protobuf3Lexer;
import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ProtoGeneratorPlugin extends DefaultTask {

		@InputDirectory
		abstract public DirectoryProperty getProtoSrcDir();

		@OutputDirectory
		abstract public DirectoryProperty getGeneratedFileDir();

		@TaskAction
		public void perform() throws IOException {
			final File protoDir = getProtoSrcDir().getAsFile().get();
			final File generatedDir = getGeneratedFileDir().getAsFile().get();
			System.out.println("protoDir.getAbsolutePath() = " + protoDir.getAbsolutePath());
			for (final File file : protoDir.listFiles()) {
				System.out.println("	file.getAbsolutePath() = " + file.getAbsolutePath());
//				if (file.getName().equals("timestamp.proto")) {
					generateFile(file, generatedDir);
//				}
			}
		}

		private void generateFile(File protoFile, File destinationDir) throws IOException {
			try (var input = new FileInputStream(protoFile)) {
				final var lexer = new Protobuf3Lexer(CharStreams.fromStream(input));
				final var parser = new Protobuf3Parser(new CommonTokenStream(lexer));
				var parsedDoc =parser.proto();
				final String javaPackage = getJavaPackage(parsedDoc);
				System.out.println("javaPackage = " + javaPackage);
				final Path packageDir = destinationDir.toPath().resolve(javaPackage.replace('.','/'));
				System.out.println("packageDir = " + packageDir);
				Files.createDirectories(packageDir);
				for(var topLevelDef: parsedDoc.topLevelDef()){

					final var msgDef = topLevelDef.messageDef();
					if (msgDef != null) {
						var msgName = msgDef.messageName().getText();
						System.out.println("msgName = " + msgName);
						final var javaFile = packageDir.resolve(msgName+".java");
						try(FileWriter javaWriter = new FileWriter(javaFile.toFile())) {
							javaWriter.write("package "+javaPackage+";\n\n");
							javaWriter.write("public record "+msgName+"(\n");
							for (int i = 0; i < msgDef.messageBody().messageElement().size(); i++) {
								final boolean isLast = i == (msgDef.messageBody().messageElement().size()-1);
								var item =  msgDef.messageBody().messageElement().get(i);
								var fieldName = item.field().fieldName().getText();
								System.out.println("	fieldName = " + fieldName);
								var fieldNumber = Integer.parseInt(item.field().fieldNumber().getText());
								System.out.println("	fieldNumber = " + fieldNumber);

								final var optionalComma = isLast ? "" : ",";
								javaWriter.write("    "+fieldName+optionalComma+"\n");
							}

							javaWriter.write("){}\n");
						}
					}

				}
			}
		}

		private static String getJavaPackage(Protobuf3Parser.ProtoContext parsedDoc) {
			String packageName = "";
			for(var option: parsedDoc.optionStatement()){
				if ("java_package".equals(option.optionName().getText())) {
					packageName = option.constant().getText();
				}
			}
			return packageName;
		}
	}