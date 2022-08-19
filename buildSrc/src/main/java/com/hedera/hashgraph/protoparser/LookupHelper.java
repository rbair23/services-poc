package com.hedera.hashgraph.protoparser;

import com.hedera.hashgraph.protoparser.grammar.Protobuf3Lexer;
import com.hedera.hashgraph.protoparser.grammar.Protobuf3Parser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.hedera.hashgraph.protoparser.Common.MODELS_DEST_PACKAGE;
import static com.hedera.hashgraph.protoparser.Common.PARSERS_DEST_PACKAGE;
import static com.hedera.hashgraph.protoparser.Common.computeJavaPackageSuffix;

/**
 * Class that process protobuf and builds maps of message name to destination package the perser or model will be generated in
 */
public class LookupHelper {
	private final Map<String,String> packageSuffixMap = new HashMap<>();
	private final Set<String> enumNames = new HashSet<>();

	/**
	 * Main generate method that process a single protobuf file and writes records and enums into package directories
	 * inside the destinationSrcDir.
	 *
	 * @param protoDir The protobuf file to parse
	 * @param destinationSrcDir the generated source directory to write files into
	 * @throws IOException if there was a problem writing files
	 */
	public LookupHelper(File protoDir) throws IOException {
		buildMesageToPackageMap(protoDir, packageSuffixMap);
	}

	public String getModelPackage(String messageName) {
		final String suffix = packageSuffixMap.get(messageName);
		if (suffix == null) return null;
		return MODELS_DEST_PACKAGE + suffix;
	}

	public String getParserPackage(String messageName) {
		final String suffix = packageSuffixMap.get(messageName);
		if (suffix == null) return null;
		return PARSERS_DEST_PACKAGE + packageSuffixMap.get(messageName);
	}

	public boolean isEnum(String messageType) {
		return enumNames.contains(messageType);
	}

	public void addEnum(String messageType){
		enumNames.add(messageType);
	}

	/**
	 * Builds onto map from protobuf message to java package. This is used to produce imports for messages in other packages.
	 *
	 * @param protosFileOrDir a directory containing protobuf files or the protobuf file to parse and look for messages types in
	 * @param packageMap map of message name to java package name to add to
	 */
	private static void buildMesageToPackageMap(File protosFileOrDir, Map<String,String> packageSuffixMap) {
		if (protosFileOrDir.isDirectory()) {
			for (final File file : protosFileOrDir.listFiles()) {
				buildMesageToPackageMap(file,packageSuffixMap);
			}
		} else if (protosFileOrDir.getName().endsWith(".proto")){
			final String dirName = protosFileOrDir.getParentFile().getName().toLowerCase();
			try (var input = new FileInputStream(protosFileOrDir)) {
				final var lexer = new Protobuf3Lexer(CharStreams.fromStream(input));
				final var parser = new Protobuf3Parser(new CommonTokenStream(lexer));
				final String javaPackageSuffix = computeJavaPackageSuffix(dirName);
				var parsedDoc = parser.proto();
				for (var topLevelDef : parsedDoc.topLevelDef()) {
					final var msgDef = topLevelDef.messageDef();
					if (msgDef != null) {
						var msgName = msgDef.messageName().getText();
						packageSuffixMap.put(msgName, javaPackageSuffix);
					}
					final Protobuf3Parser.EnumDefContext enumDef = topLevelDef.enumDef();
					if (enumDef != null) {
						final var enumName = enumDef.enumName().getText();
						packageSuffixMap.put(enumName, javaPackageSuffix);
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
