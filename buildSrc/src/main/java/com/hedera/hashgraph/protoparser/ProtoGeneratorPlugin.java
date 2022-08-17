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
			final File protoDir = getProtoSrcDir().getAsFile().get();
			final File generatedDir = getGeneratedFileDir().getAsFile().get();
			RecordAndEnumGenerator.generateRecordsAndEnums(protoDir, generatedDir);
		}
	}