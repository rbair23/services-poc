package com.hedera.hashgraph.protoparser;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public abstract class ProtoGeneratorPlugin extends DefaultTask {

		@InputDirectory
		abstract public DirectoryProperty getProtoSrcDir();

		@OutputDirectory
		abstract public DirectoryProperty getGeneratedFileDir();

		@TaskAction
		public void perform() throws IOException {
			try {
				final File protoDir = getProtoSrcDir().getAsFile().get();
				final File generatedDir = getGeneratedFileDir().getAsFile().get();
				final File generatedJavaMainDir = new File(getGeneratedFileDir().getAsFile().get(),"main/java");
				generatedJavaMainDir.mkdirs();
				final File generatedJavaTestDir = new File(getGeneratedFileDir().getAsFile().get(),"test/java");
				generatedJavaTestDir.mkdirs();
				final LookupHelper lookupHelper = new LookupHelper(protoDir);
				ModelGenerator.generateModel(protoDir, generatedJavaMainDir, lookupHelper);
				SchemaGenerator.generateSchemas(protoDir, generatedJavaMainDir, lookupHelper);
				ParserGenerator.generateParsers(protoDir, generatedJavaMainDir, lookupHelper);
				WriterGenerator.generateWriters(protoDir, generatedJavaMainDir, lookupHelper);
				TestGenerator.generateUnitTests(protoDir, generatedJavaTestDir, lookupHelper);
			} catch (Throwable e) {
				e.printStackTrace();
				throw e;
			}
		}
	}