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
			RecordGenerator.generateRecords(protoDir, generatedDir);
		}
	}