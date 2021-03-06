/*
    Structorizer
    A little tool which you can use to create Nassi-Schneiderman Diagrams (NSD)

    Copyright (C) 2009  Bob Fisch

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or any
    later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Bob Fisch
 *
 *      Description:    This class generates ANSI C code.
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author                  Date            Description
 *      ------                  ----            -----------
 *      Bob Fisch               2008.11.17      First Issue
 *      Gunter Schillebeeckx    2009.08.10      Bugfixes (see comment)
 *      Bob Fisch               2009.08.17      Bugfixes (see comment)
 *      Bob Fisch               2010.08-30      Different fixes asked by Kay Gürtzig
 *                                              and Peter Ehrlich
 *      Kay Gürtzig             2010.09.10      Bugfixes and cosmetics (see comment)
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2014.11.06      Support for logical Pascal operators added
 *      Kay Gürtzig             2014.11.16      Bugfixes in operator conversion
 *      Kay Gürtzig             2015.10.18      Indentation and comment mechanisms revised, bugfix
 *      Kay Gürtzig             2015.10.21      New generator now supports multiple-case branches
 *      Kay Gürtzig             2015.11.01      Language transforming reorganised, FOR loop revision
 *      Kay Gürtzig             2015.11.10      Bugfixes KGU#71 (switch default), KGU#72 (div operators)
 *      Kay Gürtzig             2015.11.10      Code style option optionBlockBraceNextLine() added,
 *                                              bugfix/enhancement #22 (KGU#74 jump and return handling)
 *      Kay Gürtzig             2015.12.13      Bugfix #51 (=KGU#108): Cope with empty input and output
 *      Kay Gürtzig             2015.12.21      Adaptations for Bugfix #41/#68/#69 (=KGU#93)
 *      Kay Gürtzig             2016.01.15      Bugfix #64 (exit instruction was exported without ';')
 *      Kay Gürtzig             2016.01.15      Issue #61/#107: improved handling of typed variables 
 *      Kay Gürtzig             2016.03.16      Enh. #84: Minimum support for FOR-IN loops (KGU#61) 
 *      Kay Gürtzig             2016.04.01      Enh. #144: Export option to suppress content conversion 
 *      Kay Gürtzig             2016.04.03      Enh. KGU#150: ord and chr functions converted (raw approach)
 *      Kay Gürtzig             2016.07.20      Enh. #160: Option to involve subroutines implemented (=KGU#178)
 *      Kay Gürtzig             2016.08.10      Issue #227: <stdio.h> and TODOs only included if needed 
 *      Kay Gürtzig             2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2016.09.25      Enh. #253: CodeParser.keywordMap refactored 
 *      Kay Gürtzig             2016.10.14      Enh. 270: Handling of disabled elements (code.add(...) --> addCode(..))
 *      Kay Gürtzig             2016.10.15      Enh. 271: Support for input instructions with prompt
 *      Kay Gürtzig             2016.10.16      Enh. #274: Colour info for Turtleizer procedures added
 *      Kay Gürtzig             2016.12.01      Bugfix #301: More sophisticated test for condition enclosing by parentheses
 *      Kay Gürtzig             2016.12.22      Enh. #314: Support for File API
 *      Kay Gürtzig             2017.01.26      Enh. #259/#335: Type retrieval and improved declaration support 
 *      Kay Gürtzig             2017.01.31      Enh. #113: Array parameter transformation
 *      Kay Gürtzig             2017.02.06      Minor corrections in generateJump(), String delimiter conversion (#343)
 *      Kay Gürtzig             2017.02.27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017.03.05      Bugfix #365: Fundamental revision of generateForInCode(), see comment.
 *      Kay Gürtzig             2017.03.15      Bugfix #181/#382: String delimiter transformation didn't work 
 *      Kay Gürtzig             2017.03.15      Issue #346: Insertion mechanism was misplaced (depended on others)
 *      Kay Gürtzig             2017.03.30      Issue #365: FOR-IN loop code generation revised again
 *      Kay Gürtzig             2017.04.12      Enh. #388: Handling of constants
 *      Kay Gürtzig             2017.04.13      Enh. #389: Preparation for subclass-dependent handling of import CALLs
 *      Kay Gürtzig             2017.04.14      Bugfix #394: Export of Jump elements (esp. leave) revised
 *      Kay Gürtzig             2017.05.16      Enh. #372: Export of copyright information
 *      Kay Gürtzig             2017.09.26      Enh. #389/#423: Export with includable diagrams (as global definitions)
 *      Kay Gürtzig             2017.09.30      Enh. #423: struct export fixed.
 *      Kay Gürtzig             2017.11.02      Issue #447: Line continuation in Alternative and Case elements supported
 *      Kay Gürtzig             2017.11.06      Issue #453: Modifications for string type and input and output instructions
 *      Kay Gürtzig             2018.03.13      Bugfix #520,#521: Mode suppressTransform enforced for declarations
 *      Kay Gürtzig             2018.07.21      Enh. #563, Bugfix #564: Smarter record initializers / array initializer defects
 *
 ******************************************************************************************************
 *
 *      Comment:
 *      
 *      2017.03.05 - Bugfix #365 (Kay Gürtzig)
 *      - Improved FOR-IN loop export applying the now available typeMap information.
 *      - generic names no longer with constant suffix but with loop-specific hash code, allowing global distinction
 *      - generic type definitions now global (old ANSI C didn't support local type definitions (relevant for reimport)
 *
 *      2016.04.01 - Enh. #144 (Kay Gürtzig)
 *      - A new export option suppresses conversion of text content and restricts the export
 *        more or less to the mere control structure generation.
 *        
 *      2015.12.21 - Bugfix #41/#68/#69 (Kay Gürtzig)
 *      - Operator replacement had induced unwanted padding and string literal modifications
 *      - new subclassable method transformTokens() for all token-based replacements 
 *      
 *      2015-11-29 - enhancement #23: Sensible handling of Jump elements (break / return / exit)
 *      - return instructions and assignments to variables named "result" or like the function
 *        are registered, such that return instructions may be generated on demand
 *      - "leave" jumps will generate break or goto instructions
 *      - exit instructions are produced as well.
 *      - new methods insertBlockHeading() and insertBlockTail() facilitate code style variation and
 *        subclassing w.r.t. multi-level jump instructions.
 *      
 *      2015-11-01 - Code revision / enhancements
 *      - Most of the transform stuff delegated to Element and Generator (KGU#18/KGU23)
 *      - Enhancement #10 (KGU#3): FOR loops themselves now provide more reliable loop parameters  
 *      
 *      2015.10.21 - Enhancement KGU#15: Case element with comma-separated constant list per branch
 *      
 *      2015.10.18 - Bugfixes and modificatons (Kay Gürtzig)
 *      - Bugfix: The export option "export instructions as comments" had been ignored before
 *      - An empty Jump element will now be translated into a break; instruction by default.
 *      - Comment method signature simplified
 *      - Indentation mechanism revised
 *      
 *      2014.11.16 - Bugfixes (Kay Gürtzig)
 *      - conversion of comparison and logical operators had still been flawed
 *      - comment generation unified by new inherited generic method insertComment 
 *      
 *      2014.11.06 - Enhancement (Kay Gürtzig)
 *      - logical operators "and", "or", and "not" supported 
 *      
 *      2010.09.10 - Bugfixes (Kay Gürtzig)
 *      - conditions for automatic bracket insertion for "while", "switch", "if" corrected
 *      - case keyword inserted for the branches of "switch"
 *      - function header and return statement for non-program diagram export adjusted
 *      - "cosmetic" changes to the block ends of "switch" and "do while" 
 *      
 *      2010.08.30
 *      - replaced standard I/O by the correct versions for C (not Pascal ;-P))
 *      - comments are put into code as well
 *      - code transformations (copied from Java)
 *
 *      2009.08.17 - Bugfixes
 *      - added automatic brackets for "while", "switch" & "if"
 *      - in the "repeat": "not" => "!"
 *      - pascal operator convertion
 *
 *      2009.08.10 - Bugfixes
 *      - Mistyping of the keyword "switch" in CASE statement
 *      - Mistyping of brackets in IF statement
 *      - Implementation of FOR loop
 *      - Indent replaced from 2 spaces to TAB-character (TAB configurable in IDE)
 *
 ******************************************************************************************************///

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import lu.fisch.utils.*;
import lu.fisch.structorizer.parsers.*;
import lu.fisch.structorizer.elements.*;
import lu.fisch.structorizer.executor.Function;

public class CGenerator extends Generator {

	/************ Fields ***********************/
	@Override
	protected String getDialogTitle() {
		return "Export ANSI C ...";
	}

	@Override
	protected String getFileDescription() {
		return "ANSI C Source Code";
	}

	@Override
	protected String getIndent() {
		return "\t";
	}

	@Override
	protected String[] getFileExtensions() {
		String[] exts = { "c" };
		return exts;
	}

	// START KGU 2015-10-18: New pseudo field
	@Override
	protected String commentSymbolLeft() {
		// In ANSI C99, line comments are already allowed
		return "//";
	}

	// END KGU 2015-10-18

// START KGU#16 2015-12-18: Moved to Generator.java	and made an ExportOptionDialoge option
//	// START KGU#16 2015-11-29: Code style option for opening brace placement
//	protected boolean optionBlockBraceNextLine() {
//		// (KGU 2015-11-29): Should become an ExportOptionDialoge option
//		return true;
//	}
//	// END KGU#16 2015-11-29
// END KGU#16 2015-12-18
	
	// START KGU#261/#332 2017-01-27: Enh. #259/#335
	protected HashMap<String, TypeMapEntry> typeMap;
	// END KGU#261/#332 2017-01-27
	
	// START KGU#16/KGU#74 2015-11-30: Unification of block generation (configurable)
	/**
	 * This subclassable method is used for insertBlockHeading()
	 * @return Indicates where labels for multi-level loop exit jumps are to be placed
	 * (in C, C++, C# after the loop, in Java at the beginning of the loop). 
	 */
	protected boolean isLabelAtLoopStart()
	{
		return false;
	}
	
	/**
	 * Instruction to be used to leave an outer loop (subclassable)
	 * A label string is supposed to be appended without parentheses.
	 * @return a string containing the respective reserved word
	 */
	protected String getMultiLevelLeaveInstr()
	{
		return "goto";
	}
	
	// See also insertExitInstr(int, String)
	// END KGU#16/KGU#74 2015-11-30

	// START KGU#78 2015-12-18: Enh. #23 We must know whether to create labels for simple breaks
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#supportsSimpleBreak()
	 */
	@Override
	protected boolean breakMatchesCase()
	{
		return true;
	}
	// END KGU#78 2015-12-18

//	// START KGU 2016-08-12: Enh. #231 - information for analyser - obsolete since 3.27
//    private static final String[] reservedWords = new String[]{
//		"auto", "break", "case", "char", "const", "continue",
//		"default", "do", "double", "else", "enum", "extern",
//		"float", "for", "goto", "if", "int", "long",
//		"register", "return",
//		"short", "signed", "sizeof", "static", "struct", "switch",
//		"typedef", "union", "unsigned", "void", "volatile", "while"};
//	public String[] getReservedWords()
//	{
//		return reservedWords;
//	}
//	public boolean isCaseSignificant()
//	{
//		return true;
//	}
//	// END KGU 2016-08-12

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getIncludePattern()
	 */
	@Override
	protected String getIncludePattern()
	{
		return "#include %";
	}
	// END KGU#351 2017-02-26

	/************ Code Generation **************/

	// START KGU#560 2018-07-22 Bugfix #564
	/** @return whether the element number is to be given in array type specifiers */
	protected boolean wantsSizeInArrayType()
	{
		return true;
	}
	// END KGU#560 2018-07-22

	// START KGU#18/KGU#23 2015-11-01 Transformation decomposed
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getInputReplacer(boolean)
	 */
	// START KGU#281 2016-10-15: Enh. #271
	//protected String getInputReplacer() {
	//	return "scanf(\"\", &$1)";
	//}
	@Override
	protected String getInputReplacer(boolean withPrompt) {
		if (withPrompt) {
			return "printf($1); scanf(\"TODO: specify format\", &$2)";
		}
		return "scanf(\"TODO: specify format\", &$1)";
	}
	// END KGU#281 2016-10-15

	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#getOutputReplacer()
	 */
	@Override
	protected String getOutputReplacer() {
		return "printf(\"TODO: specify format\", $1); printf(\"\\n\")";
	}

	// START KGU#351 2017-02-26: Enh. #346 - include / import / uses config
	/**
	 * Method pre-processes an include file name for the #include
	 * clause. This version surrounds a string not enclosed in angular
	 * brackets by quotes.
	 * @param _includeFileName a string from the user include configuration
	 * @return the preprocessed string as to be actually inserted
	 */
	protected String prepareIncludeItem(String _includeFileName)
	{
		if (!(_includeFileName.startsWith("<") && _includeFileName.endsWith(">"))) {
			_includeFileName = "\"" + _includeFileName + "\"";
		}
		return _includeFileName;
	}
	// END KGU#351 2017-02-26

	// START KGU#16/#47 2015-11-30
	/**
	 * Instruction to create a language-specific exit instruction (subclassable)
	 * The exit code will be passed to the generated code.
	 */
	protected void insertExitInstr(String _exitCode, String _indent, boolean isDisabled)
	{
		// START KGU 2016-01-15: Bugfix #64 (reformulated) semicolon was missing
		//code.add(_indent + "exit(" + _exitCode + ")");
		addCode("exit(" + _exitCode + ");", _indent, isDisabled);
		// END KGU 2016-01-15
	}
	// END KGU#16/#47 2015-11-30

	// START KGU#93 2015-12-21: Bugfix #41/#68/#69
//	/**
//	 * Transforms assignments in the given intermediate-language code line.
//	 * Replaces "<-" by "="
//	 * 
//	 * @param _interm
//	 *            - a code line in intermediate syntax
//	 * @return transformed string
//	 */
//	@Deprecated
//	protected String transformAssignment(String _interm) {
//		return _interm.replace(" <- ", " = ");
//	}
	
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTokens(lu.fisch.utils.StringList)
	 */
	@Override
	protected String transformTokens(StringList tokens)
	{
		tokens.replaceAll("div", "/");
		tokens.replaceAll("<-", "=");
		// START KGU#150 2016-04-03: Handle Pascal ord and chr function
		int pos = - 1;
		while ((pos = tokens.indexOf("ord", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "(int)");
		}
		pos = -1;
		while ((pos = tokens.indexOf("chr", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "(char)");
		}
		// END KGU#150 2016-04-03
		// START KGU#311 2016-12-22: Enh. #314 - Structorizer file API support
		if (this.usesFileAPI) {
			transformFileAPITokens(tokens);
		}
		// END KGU#311 2016-12-22
		// START KGU#342 2017-02-07: Bugfix #343
		for (int i = 0; i < tokens.count(); i++) {
			String token = tokens.get(i);
			int tokenLen = token.length();
			// START KGU#190 2017-03-15: Bugfix #181/#382 - String delimiter conversion had failed
			//if (tokenLen >= 2 && (token.startsWith("'") && token.endsWith("\"") || token.startsWith("\"") && token.endsWith("'"))) {
			if (tokenLen >= 2 && (token.startsWith("'") && token.endsWith("'") || token.startsWith("\"") && token.endsWith("\""))) {
			// END KGU#190 2017-03-15
				char delim = token.charAt(0);
				String internal = token.substring(1, tokenLen-1);
				// Escape all unescaped double quotes
				pos = -1;
				while ((pos = internal.indexOf("\"", pos+1)) >= 0) {
					if (pos == 0 || internal.charAt(pos-1) != '\\') {
						internal = internal.substring(0, pos) + "\\\"" + internal.substring(pos+1);
						pos++;
					}
				}
				if (!(tokenLen == 3 || tokenLen == 4 && token.charAt(1) == '\\')) {
					delim = '\"';
				}
				tokens.set(i, delim + internal + delim);
			}
		}
		// END KGU#342 2017-02-07
		return tokens.concatenate().trim();
	}
	// END KGU#93 2015-12-21

	// END KGU#18/KGU#23 2015-11-01

	// START KGU#311 2016-12-22: Enh. #314 - Structorizer file API support
	/**
	 * Subclassable submethod of transformTokens(), designed to do specific replacements or manipulations
	 * with the subroutine names of the File API. It is called after all other token transformations are done
	 * (immediately before re-concatenation).
	 * This does some C-specific stuff here prefixing fileRead with pointer operators and a dummy type casting,
	 * so subclasses should better overwrite it.
	 * @param tokens
	 */
	protected void transformFileAPITokens(StringList tokens)
	{
		int pos = -1;
		while ((pos = tokens.indexOf("fileRead", pos+1)) >= 0 && pos+1 < tokens.count() && tokens.get(pos+1).equals("("))
		{
			tokens.set(pos, "*(/*type?*/*)fileRead");
		}
	}
	// END KGU#311 2016-12-22
	
// START KGU#18/KGU#23 2015-11-01: Obsolete    
//    public static String transform(String _input)
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transform(java.lang.String)
	 */
	@Override
	protected String transform(String _input)
	{
		// START KGU#162 2016-04-01: Enh. #144
		if (!this.suppressTransformation)
		{
		// END KGU#162 2016-04-01
			// START KGU#109/KGU#141 2016-01-16: Bugfix #61,#107,#112
			_input = Element.unifyOperators(_input);
			int asgnPos = _input.indexOf("<-");
			if (asgnPos > 0)
			{
				String lval = _input.substring(0, asgnPos).trim();
				String expr = _input.substring(asgnPos + "<-".length()).trim();
				String[] typeNameIndex = this.lValueToTypeNameIndexComp(lval);
				String index = typeNameIndex[2];
				_input = (typeNameIndex[0] + " " + typeNameIndex[1] + 
						(index.isEmpty() ? "" : "["+index+"]") + 
						// START KGU#388 2017-09-27: Enh. #423
						typeNameIndex[3] +
						// END KGU#388 2017-09-27: Enh. #423
						" <- " + expr).trim();
			}
			// END KGU#109/KGU#141 2016-01-16
		// START KGU#162 2016-04-01: Enh. #144
		}
		// END KGU#162 2016-04-01
		
		_input = super.transform(_input);

		// START KGU#108 2015-12-13: Bugfix #51: Cope with empty input and output
		_input = _input.replace("scanf(\"TODO: specify format\", &)", "getchar()");
		_input = _input.replace("printf(\"TODO: specify format\", ); ", "");
		// END KGU#108 2015-12-13

		return _input.trim();
	}

	// START KGU#16 2015-11-29
	/* (non-Javadoc)
	 * @see lu.fisch.structorizer.generators.Generator#transformTypeString(java.lang.String, java.lang.String)
	 * see also: transformType(java.lang.String, java.lang.String)
	 */
	@Override
	protected String transformType(String _type, String _default) {
		if (_type == null)
			_type = _default;
		// START KGU 2017-04-12: We must not generally flatten the case (consider user types!)
		//_type = _type.toLowerCase();
		//_type = _type.replace("integer", "int");
		//_type = _type.replace("real", "double");
		//_type = _type.replace("boolean", "int");
		//_type = _type.replace("boole", "int");
		//_type = _type.replace("character", "char");
		_type = _type.replaceAll("(^|.*\\W)(I" + BString.breakup("nt") + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("integer") + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(L" + BString.breakup("ong") + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("longint") + ")($|\\W.*)", "$1long$3");
		_type = _type.replaceAll("(^|.*\\W)(D" + BString.breakup("ouble") + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("real") + ")($|\\W.*)", "$1double$3");
		_type = _type.replaceAll("(^|.*\\W)(F" + BString.breakup("loat") + ")($|\\W.*)", "$1float$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("boolean") + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("boole") + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("bool") + ")($|\\W.*)", "$1int$3");
		_type = _type.replaceAll("(^|.*\\W)(C" + BString.breakup("har") + ")($|\\W.*)", "$1char$3");
		_type = _type.replaceAll("(^|.*\\W)(" + BString.breakup("character") + ")($|\\W.*)", "$1char$3");
		// END KGU 2017-04-12
		// START KGU#332 2017-01-30: Enh. #335 - more sophisticated type info
		if (this.getClass().getSimpleName().equals("CGenerator")) {
			_type = _type.replace("string", "char*");
			_type = _type.replace("String", "char*");
		}
		// END KGU#332 2017-01-30
		return _type;
	}
	// END KGU#16 2015-11-29
	
	// START KGU#388 2017-09-29: Enh. #423
	protected String transformTypeWithLookup(String _type, String _default) {
		TypeMapEntry typeInfo = this.typeMap.get(":" + _type);
		// The typeInfo might be an alias, in this case no specific measures are necessary
		if (typeInfo != null && typeInfo.isRecord() && _type.equals(typeInfo.typeName)) {
			_type = this.transformRecordTypeRef(typeInfo.typeName, false);
		}
		else {
			_type = transformType(_type, _default);
		}
		return _type;
	}
	// END KGU#388 2017-09-29



	// START KGU#140 2017-01-31: Enh. #113: Advanced array transformation
	protected String transformArrayDeclaration(String _typeDescr, String _varName)
	{
		String decl = "";
		if (_typeDescr.toLowerCase().startsWith("array") || _typeDescr.endsWith("]")) {
			// TypeMapEntries are really good at analysing array definitions
			TypeMapEntry typeInfo = new TypeMapEntry(_typeDescr, null, null, 0, false, true, false);
			String canonType = typeInfo.getTypes().get(0);
			decl = this.makeArrayDeclaration(canonType, _varName, typeInfo).trim();
		}
		else {
			decl = (_typeDescr + " " + _varName).trim();
		}
		return decl;
	}
	// END KGU#140 2017-01-31
	
	// START KGU#388 2017-09-26: Enh. #423 struct type support
	/**
	 * Returns a target-language expression replacing the Structorizer record
	 * initializer- as far as it can be handled within one line
	 * @param constValue - the Structorizer record initializer
	 * @param typeInfo - the TypeMapEntry describing the record type
	 * @return the equivalent target code as expression string
	 */
	protected String transformRecordInit(String constValue, TypeMapEntry typeInfo) {
		// START KGU#559 2018-07-20: Enh. #563 - smarter initializer evaluation
		//HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue);
		HashMap<String, String> comps = Instruction.splitRecordInitializer(constValue, typeInfo);
		// END KGU#559 2018-07-20
		LinkedHashMap<String, TypeMapEntry> compInfo = typeInfo.getComponentInfo(true);
		String recordInit = "{";
		boolean isFirst = true;
		for (Entry<String, TypeMapEntry> compEntry: compInfo.entrySet()) {
			String compName = compEntry.getKey();
			String compVal = comps.get(compName);
			if (isFirst) {
				isFirst = false;
			}
			else {
				recordInit += ", ";
			}
			if (!compName.startsWith("§")) {
				if (compVal == null) {
					recordInit += "0 /*undef.*/";
				}
				else if (compEntry.getValue().isRecord()) {
					recordInit += transformRecordInit(compVal, compEntry.getValue());
				}
				else {
					recordInit += transform(compVal);
				}
			}
		}
		recordInit += "}";
		return recordInit;
	}

	// END KGU#388 2017-09-26

	protected void insertBlockHeading(Element elem, String _headingText, String _indent)
	{
		boolean isDisabled = elem.isDisabled();
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem) && this.isLabelAtLoopStart())  
		{
				_headingText = this.labelBaseName + this.jumpTable.get(elem) + ": " + _headingText;
		}
		if (!this.optionBlockBraceNextLine())
		{
			addCode(_headingText + " {", _indent, isDisabled);
		}
		else
		{
			addCode(_headingText, _indent, isDisabled);
			addCode("{", _indent, isDisabled);
		}
	}

	protected void insertBlockTail(Element elem, String _tailText, String _indent)
	{
		boolean isDisabled = elem.isDisabled();
		if (_tailText == null) {
			addCode("}", _indent, isDisabled);
		}
		else {
			addCode("} " + _tailText + ";", _indent, isDisabled);
		}
		
		if (elem instanceof ILoop && this.jumpTable.containsKey(elem) && !this.isLabelAtLoopStart()) {
			addCode(this.labelBaseName + this.jumpTable.get(elem) + ": ;", _indent, isDisabled);
		}
	}
	// END KGU#74 2015-11-30
	
	// START KGU#332 2017-01-27: Enh. #335
	/**
	 * States whether constant definitions or varaible declarations may occur anywhere in
	 * the code or only at block beginning
	 * @return true if declarations may be mixed among instructions
	 */
	protected boolean isInternalDeclarationAllowed()
	{
		return false;
	}
	// END KGU#332 2017-01-27
	
	// START KGU#388 2017-09-26: Enh. #423
	/**
	 * Creates a type description suited for C code from the given TypeMapEntry {@code typeInfo}
	 * The returned type description will have to be split before the first
	 * occurring opening bracket in order to place the variable or type name there.
	 * @param typeInfo - the defining or derived TypeMapInfo of the type 
	 * @return a String suited as C type description in declarations etc. 
	 */
	@Override
	protected String transformTypeFromEntry(TypeMapEntry typeInfo, TypeMapEntry definingWithin) {
		// Record type description won't usually occur (rather names)
		String _typeDescr;
//		String canonType = typeInfo.getTypes().get(0);
		String canonType = typeInfo.getCanonicalType(true, true);
		int nLevels = canonType.lastIndexOf('@')+1;
		String elType = (canonType.substring(nLevels)).trim();
		if (typeInfo.isRecord()) {
			elType = transformRecordTypeRef(elType, typeInfo == definingWithin);
		}
		else {
			elType = transformType(elType, "/*???*/");
		}
		_typeDescr = elType;
		for (int i = 0; i < nLevels; i++) {
			_typeDescr += "[";
			if (this.wantsSizeInArrayType()) {
				int minIndex = typeInfo.getMinIndex(i);
				int maxIndex = typeInfo.getMaxIndex(i);
				int indexRange = maxIndex+1 - minIndex;
				// We try a workaround for negative minIndex...
				if (indexRange > maxIndex + 1) {
					maxIndex = indexRange - 1;
				}
				if (maxIndex > 0) {
					_typeDescr += Integer.toString(maxIndex+1);
				}
			}
			_typeDescr += "]";
		}
		return _typeDescr;
	}

	/**
	 * Special adaptation of record type name references in C-like languages, e.g. C
	 * adds a prefix "struct" wherever it is used. C++ doesn't need to, Java and C#
	 * don't, so the inheriting classes must override this.
	 * @param structName - name of the structured type
	 * @param isRecursive - if used defining this very type
	 * @return the prepared reference string
	 */
	protected String transformRecordTypeRef(String structName, boolean isRecursive) {
		return "struct " + structName + (isRecursive ? " * " : "");
	}

	/**
	 * Adds the type definitions for all types in {@code _root.getTypeInfo()}.
	 * @param _root - originating Root
	 * @param _indent - current indentation level (as String)
	 */
	protected void generateTypeDefs(Root _root, String _indent) {
		for (Entry<String, TypeMapEntry> typeEntry: _root.getTypeInfo().entrySet()) {
			String typeKey = typeEntry.getKey();
			if (typeKey.startsWith(":")) {
				generateTypeDef(_root, typeKey.substring(1), typeEntry.getValue(), _indent, false);
			}
		}
	}

	/**
	 * Inserts a typedef or struct definition for the type passed in by {@code _typeEnry}
	 * if it hadn't been defined globally or in the preamble before.
	 * @param _root - the originating Root
	 * @param _type - the type map entry the definition for which is requested here
	 * @param _indent - the current indentation
	 * @param _asComment - if the type deinition is only to be added as comment (disabled)
	 */
	protected void generateTypeDef(Root _root, String _typeName, TypeMapEntry _type, String _indent, boolean _asComment) {
		String typeKey = ":" + _typeName;
		if (this.wasDefHandled(_root, typeKey, true)) {
			return;
		}
		insertDeclComment(_root, _indent, typeKey);
		if (_type.isRecord()) {
			String indentPlus1 = _indent + this.getIndent();
			addCode("struct " + _type.typeName + " {", _indent, _asComment);
			for (Entry<String, TypeMapEntry> compEntry: _type.getComponentInfo(false).entrySet()) {
				addCode(transformTypeFromEntry(compEntry.getValue(), _type) + "\t" + compEntry.getKey() + ";",
						indentPlus1, _asComment);
			}
			addCode("};", _indent, _asComment);
		}
		else {
			addCode("typedef " + this.transformTypeFromEntry(_type, null) + " " + _typeName + ";",
					_indent, _asComment);					
		}
	}
	// END KGU#388 2017-09-26

	@Override
	protected void generateCode(Instruction _inst, String _indent) {

		if (!insertAsComment(_inst, _indent)) {

			// START KGU#424 2017-09-26: Avoid the comment here if the element contains mere declarations
			//insertComment(_inst, _indent);
			boolean commentInserted = false;
			// END KGU#424 2017-09-26

			boolean isDisabled = _inst.isDisabled();

			StringList lines = _inst.getUnbrokenText();
			for (int i = 0; i < lines.count(); i++) {
				// FIXME: We must distinguish for every line:
				// 1. assignment
				// 1.1 with declaration (mind record initializer!)
				// 1.1.1 as constant
				// 1.1.2 as variable
				// 1.2 without declaration 
				// 1.2.1 with record or array initializer
				// 1.2.2 without record initializer
				// 2. mere declaration
				// 2.1 as constant
				// 2.2 as variable
				// 3. type definition
				// 4. Input / output
				// START KGU#277/KGU#284 2016-10-13/16: Enh. #270 + Enh. #274
				//code.add(_indent + transform(lines.get(i)) + ";");
				// START KGU#504 2018-03-13: Bugfix #520/#521
				//String line = _inst.getText().get(i);
				String line = lines.get(i);
				// END KGU#504 2018-03-13
				// START KGU#261/KGU#332 2017-01-26: Enh. #259/#335
				//String codeLine = transform(line) + ";";
				//addCode(codeLine, _indent, isDisabled);
				// Things will get easier and more precise with tokenization
				// (which must be done based on the original line)
				StringList tokens = Element.splitLexically(line.trim(), true);
				StringList pureTokens = tokens.copy();
				StringList exprTokens = null;
				StringList pureExprTokens = null;
				pureTokens.removeAll(" ");
				String expr = null;	// Original expression
				int posAsgn = tokens.indexOf("<-");
				if (posAsgn < 0) {
					posAsgn = tokens.count();
				}
				else {
					exprTokens = tokens.subSequence(posAsgn + 1, tokens.count());
					pureExprTokens = pureTokens.subSequence(pureTokens.indexOf("<-")+1, pureTokens.count());
				}
				String codeLine = null;
				String varName = _inst.getAssignedVarname(pureTokens);
				boolean isDecl = Instruction.isDeclaration(line);
				//exprTokens.removeAll(" ");
				if (!this.suppressTransformation && (isDecl || exprTokens != null)) {
					// Cases 1 or 2
					// If there is an initialization then it must at least be generated
					// as assignment.
					// With declaration styles other than than C-like, this requires
					// cutting out the type specification together with the
					// specific keywords and separators ("var"+":" / "dim"+"as").
					// With C-style initializations, however, it depends on whether
					// code-internal declarations are allowed (C++, C#, Java) or not
					// (pure C): If allowed then we may just convert it as is, otherwise
					// we must cut off the type specification (i.e. all text preceding the
					// variable name).
					// START KGU#375 2017-04-12: Enh. #388 special treatment of constants
					if (pureTokens.get(0).equals("const")) {
						// Cases 1.1.1 or 2.1
						if (!this.isInternalDeclarationAllowed()) {
							// Should already have been defined
							continue;
						}
						// We try to enrich or accomplish defective type information
						Root root = Element.getRoot(_inst);
						if (root.constants.get(varName) != null) {
							this.insertDeclaration(root, varName, _indent, true);
							// START KGU#424 2017-09-26: Avoid the comment here if the element contains mere declarations
							commentInserted = true;
							// END KGU#424 2017-09-26
							continue;
						}	
					}
					// END KGU#375 2017-04-12
					if (isDecl && (this.isInternalDeclarationAllowed() || exprTokens != null)) {
						// cases 1.1.2 or 2.2
						if (tokens.get(0).equalsIgnoreCase("var") || tokens.get(0).equalsIgnoreCase("dim")) {
							// Case 1.1.2a/b or 2.2a/b (Pascal/BASIC declaration)
							String separator = tokens.get(0).equalsIgnoreCase("dim") ? "as" : ":";
							int posColon = tokens.indexOf(separator, 2, false);
							// Declaration well-formed?
							if (posColon > 0) {
								// Compose the lval without type
								codeLine = transform(tokens.subSequence(1, posColon).concatenate().trim());
								if (this.isInternalDeclarationAllowed()) {
									// Insert the type description
									String type = tokens.subSequence(posColon+1, posAsgn).concatenate().trim();
									// START KGU#561 2018-07-21: Bugfix #564
									//codeLine = transform(transformType(type, "")) + " " + codeLine;
									type = transformType(type, "");
									codeLine = this.transformArrayDeclaration(type, codeLine);
									// END KGU#561 2018-07-21
								}
							}
						}
						else {
							// Case 1.1.2c or 2.2c (2.2c not needed if internal declarations not allowed)
							// Must be C-style declaration
							if (this.isInternalDeclarationAllowed()) {
								// Case 2.2c (allowed) or 1.1.2c
								// START KGU#560 2018-07-22: Bugfix #564
								//codeLine = transform(tokens.subSequence(0, posAsgn).concatenate().trim());
								TypeMapEntry type = this.typeMap.get(varName);
								if (type != null && type.isArray()) {
									String elemType = type.getCanonicalType(true, false);
									codeLine = this.makeArrayDeclaration(this.transformType(elemType, "int"), varName, type);
								}
								else {
									// Combine type and variable as is
									codeLine = transform(tokens.subSequence(0, posAsgn).concatenate().trim());
								}
								// END KGU#560 2018-07-22
							}
							else if (exprTokens != null) {
								// Case 1.1.2c (2.2c not allowed)
								// Cut out leading type specification
								int posVar = tokens.indexOf(varName);
								// START KGU#560 2018-07-21: Bugfix #564 In case of an array declaration we must wipe off the array stuff
								//codeLine = transform(tokens.subSequence(posVar, posAsgn).concatenate().trim());
								int posEnd = tokens.indexOf("[", posVar+1);
								if (!isDecl || posEnd < 0 || posEnd > posAsgn) {
									posEnd = posAsgn;
								}
								codeLine = transform(tokens.subSequence(posVar, posEnd).concatenate().trim());
								// END KGU#560 2018-07-21
							}
//							// START KGU#375 2017-04-13: Enh. #388
//							//codeLine = transform(tokens.concatenate().trim());
//							else if (tokens.get(0).equals("const")) {
//								// We try to enrich or accomplish defective type information
//								Root root = Element.getRoot(_inst);
//								if (root.constants.get(varName) != null) {
//									this.insertDeclaration(root, varName, _indent, true);
//									// START KGU#424 2017-09-26: Avoid the comment here if the element contains mere declarations
//									commentInserted = true;
//									// END KGU#424 2017-09-26
//									continue;
//								}
//							}
						}
					}
					else if (!isDecl && exprTokens != null) {
						// Case 1.2
						// Combine variable access as is
						codeLine = transform(tokens.subSequence(0, posAsgn).concatenate()).trim();
					}
					// Now we care for a possible assignment
					if (codeLine != null && exprTokens != null && pureExprTokens.count() > 0) {
						// START KGU#560 2018-07-21: Bugfix #564 - several problems with array initializers
						int posBrace = pureExprTokens.indexOf("{");
						if (posBrace >= 0 && posBrace <= 1 && pureExprTokens.get(pureExprTokens.count()-1).equals("}")) {
							// Case 1.1 or 1.2.1 (either array or record initializer)
							if (posBrace == 1 && pureExprTokens.count() >= 3 && Function.testIdentifier(pureExprTokens.get(0), null)) {
								String typeName = pureExprTokens.get(0);							
								TypeMapEntry recType = this.typeMap.get(":"+typeName);
								if (isDecl && this.isInternalDeclarationAllowed() && recType != null) {
									// transforms the Structorizer record initializer into a C-conform one
									expr = this.transformRecordInit(exprTokens.concatenate().trim(), recType);
								}
								else {
									// In this case it's either no declaration or the declaration has already been generated
									// at the block beginning
									if (!commentInserted) {
										insertComment(_inst, _indent);
										commentInserted = true;
									}
									// END KGU#424 2017-09-26
									// FIXME: Possibly codeLine (the lval string) might be too much as first argument
									// START KGU#559 2018-07-20: Enh. #563
									//this.generateRecordInit(codeLine, pureExprTokens.concatenate(), _indent, isDisabled, null);
									this.generateRecordInit(codeLine, pureExprTokens.concatenate(), _indent, isDisabled, recType);
									// END KGU#559 2018-07-20
									// All done already
									continue;
								}
							}
							else {
								StringList items = Element.splitExpressionList(pureExprTokens.subSequence(1, pureExprTokens.count()), ",", true);
								String elemType = null;
								TypeMapEntry arrType = this.typeMap.get(varName);
								if (arrType != null && arrType.isArray()) {
									elemType = arrType.getCanonicalType(true, false);
									if (elemType != null && elemType.startsWith("@")) {
										elemType = elemType.substring(1);
									}
								}
								expr = this.generateArrayInit(codeLine, items.subSequence(0, items.count()-1), _indent, isDisabled, elemType, isDecl);
								if (expr == null) {
									continue;
								}
							}
						}
						// END KGU#560 2018-07-21
						else {
							expr = this.transform(exprTokens.concatenate()).trim();
						}
					}
					if (expr != null) {
						// In this case codeLine must be different from null
						codeLine += " = " + expr;
					}
				} // if (!this.suppressTransformation && (isDecl || exprTokens != null))
				// START KGU#388 2017-09-25: Enh. #423
				else if (!this.suppressTransformation && Instruction.isTypeDefinition(line, typeMap)) {
					// Attention! The following condition must not be combined with the above one! 
					if (this.isInternalDeclarationAllowed()) {
						tokens.removeAll(" ");
						int posEqu = tokens.indexOf("=");
						String typeName = null;
						if (posEqu == 2) {
							typeName = tokens.get(1);
						}
						TypeMapEntry type = this.typeMap.get(":" + typeName);
						Root root = Element.getRoot(_inst);
						if (type != null) {
							this.generateTypeDef(root, typeName, type, _indent, isDisabled);
							commentInserted = true;
							// CodeLine is not filled because the code has already been generated
						}
						else {
							// Hardly a recognizable type definition, just put it as is...
							codeLine = "typedef " + transform(tokens.concatenate(" ", posEqu + 1)) + " " + typeName;
						}
					}
				}
				// END KGU#388 2017-09-25
				else {
					// All other cases (e.g. input, output)
					codeLine = transform(line);
				}
				// Now append the codeLine in case it was composed and not already appended
				if (codeLine != null) {
					String lineEnd = ";";
					if (Instruction.isTurtleizerMove(line)) {
						codeLine = this.enhanceWithColor(codeLine, _inst);
						lineEnd = "";
					}
					// START KGU#424 2017-09-26: Avoid the comment here if the element contains mere declarations
					if (!commentInserted) {
						insertComment(_inst, _indent);
						commentInserted = true;
					}
					// END KGU#424 2017-09-26
					addCode(codeLine + lineEnd, _indent, isDisabled);
				}
				// END KGU#261 2017-01-26
				// END KGU#277/KGU#284 2016-10-13
			}

		}
		
	}

	protected String enhanceWithColor(String _codeLine, Instruction _inst) {
		return _codeLine + "; " + this.commentSymbolLeft() + " color = " + _inst.getHexColor();
	}

	@Override
	protected void generateCode(Alternative _alt, String _indent) {
		
		insertComment(_alt, _indent);
		
		// START KGU#453 2017-11-02: Issue #447
		//String condition = transform(_alt.getText().getLongString(), false).trim();
		String condition = transform(_alt.getUnbrokenText().getLongString(), false).trim();
		// END KGU#453 2017-11-02
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")"))
		if (!isParenthesized(condition))
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		
		insertBlockHeading(_alt, "if " + condition, _indent);
		generateCode(_alt.qTrue, _indent + this.getIndent());
		insertBlockTail(_alt, null, _indent);

		if (_alt.qFalse.getSize() != 0) {
			insertBlockHeading(_alt, "else", _indent);
			generateCode(_alt.qFalse, _indent + this.getIndent());
			insertBlockTail(_alt, null, _indent);
		}
	}

	@Override
	protected void generateCode(Case _case, String _indent) {
		
		boolean isDisabled = _case.isDisabled();
		insertComment(_case, _indent);
		
		// START KGU#453 2017-11-02: Issue #447
		//StringList lines = _case.getText();
		StringList lines = _case.getUnbrokenText();
		// END KGU#453 2017-11-02
		String condition = transform(lines.get(0), false);
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) {
		if (!isParenthesized(condition)) {
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		}

		insertBlockHeading(_case, "switch " + condition, _indent);

		for (int i = 0; i < _case.qs.size() - 1; i++) {
			// START KGU#15 2015-10-21: Support for multiple constants per
			// branch
			StringList constants = StringList.explode(lines.get(i + 1), ",");
			for (int j = 0; j < constants.count(); j++) {
				code.add(_indent + "case " + constants.get(j).trim() + ":");
			}
			// END KGU#15 2015-10-21
			// START KGU#380 2017-04-14: Bugfix #394 - Avoid redundant break instructions
			//generateCode((Subqueue) _case.qs.get(i), _indent + this.getIndent());
			//addCode(this.getIndent() + "break;", _indent, isDisabled);
			Subqueue sq = _case.qs.get(i);
			generateCode(sq, _indent + this.getIndent());
			Element lastEl = null;
			for (int j = sq.getSize() - 1; lastEl == null && j >= 0; j--) {
				if ((lastEl = sq.getElement(j)).disabled) {
					lastEl = null;
				}
			}
			Integer label = null;
			if (lastEl == null || !(lastEl instanceof Jump) || (label = this.jumpTable.get(lastEl)) != null && label == -1) {
				addCode(this.getIndent() + "break;", _indent, isDisabled);
			}
			// END KGU#380 2017-04-14
		}

		if (!lines.get(_case.qs.size()).trim().equals("%")) {
			addCode("default:", _indent, isDisabled);
			Subqueue squeue = (Subqueue) _case.qs.get(_case.qs.size() - 1);
			generateCode(squeue, _indent + this.getIndent());
			// START KGU#71 2015-11-10: For an empty default branch, at least a
			// semicolon is required
			if (squeue.getSize() == 0) {
				addCode(this.getIndent() + ";", _indent, isDisabled);
			}
			// END KGU#71 2015-11-10
		}
		
		insertBlockTail(_case, null, _indent);
	}

	// END KGU#18/#23 2015-10-20

	@Override
	protected void generateCode(For _for, String _indent) {

		insertComment(_for, _indent);
		
		// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
		if (_for.isForInLoop())
		{
			// There aren't many ideas how to implement this here in general,
			// but subclasses may have better chances to do so.
			if (generateForInCode(_for, _indent)) return;
		}
		// END KGU#61 2016-03-22

		String var = _for.getCounterVar();
		String decl = "";
		// START KGU#376 2017-09-27: Enh. #389
		if (this.isInternalDeclarationAllowed() && !wasDefHandled(Element.getRoot(_for), var, false)) {
			// We just insert a loop-local declaration
			decl = "int ";
		}
		// END KGU#376 2017-09-27
		int step = _for.getStepConst();
		String compOp = (step > 0) ? " <= " : " >= ";
		String increment = var + " += (" + step + ")";
		insertBlockHeading(_for, "for (" + decl + var + " = "
				+ transform(_for.getStartValue(), false) + "; " + var + compOp
				+ transform(_for.getEndValue(), false) + "; " + increment + ")",
				_indent);

		generateCode(_for.q, _indent + this.getIndent());

		insertBlockTail(_for, null, _indent);

	}
	
	// START KGU#61 2016-03-22: Enh. #84 - Support for FOR-IN loops
	/**
	 * We try our very best to create a working loop from a FOR-IN construct
	 * This will only work, however, if we can get reliable information about
	 * the size of the value list, which won't be the case if we obtain it e.g.
	 * via a variable.
	 * @param _for - the element to be exported
	 * @param _indent - the current indentation level
	 * @return true iff the method created some loop code (sensible or not)
	 */
	protected boolean generateForInCode(For _for, String _indent)
	{
		boolean done = false;
		String var = _for.getCounterVar();
		String valueList = _for.getValueList();
		TypeMapEntry typeInfo = this.typeMap.get(valueList);
		StringList items = this.extractForInListItems(_for);
		String itemVar = var;
		String itemType = "";
		String nameSuffix = Integer.toHexString(_for.hashCode());
		String arrayName = "array" + nameSuffix;
		String indexName = "index" + nameSuffix;
		String indent = _indent + this.getIndent();
		String startValStr = "0";
		String endValStr = "???";
		boolean isDisabled = _for.isDisabled();
		if (items != null)
		{
			// Good question is: how do we guess the element type and what do we
			// do if items are heterogenous? We will make use of the typeMap and
			// hope to get sensible information. Otherwise we add a TODO comment.
			int nItems = items.count();
			boolean allInt = true;
			boolean allDouble = true;
			boolean allString = true;
			StringList itemTypes = new StringList();
			for (int i = 0; i < nItems; i++)
			{
				String item = items.get(i);
				String type = Element.identifyExprType(this.typeMap, item, false);
				itemTypes.add(this.transformType(type, "int"));
				if (!type.equals("int") && !type.equals("boolean")) {
					allInt = false;
				}
				// START KGU#355 2017-03-30: #365 - allow type conversion
				//if (!type.equals("double")) {
				if (!type.equals("int") && !type.equals("boolean") && !type.equals("double")) {
				// END KGU#355 2017-03-30
					allDouble = false;
				}
				if (!type.equals("String")) {
					allString = false;
				}
			}
			if (allInt) itemType = "int";
			else if (allDouble) itemType = "double";
			else if (allString) itemType = "char*";
			String arrayLiteral = "{" + items.concatenate(", ") + "}";

			// Start an extra block to encapsulate the additional definitions
			addCode("{", _indent, isDisabled);
			
			if (itemType.isEmpty())
			{
				if (nItems <= 1) {
					itemType = "int";	// the default...
				}
				else {
					itemType = "union ItemTyp" + nameSuffix;
					// We create a dummy type definition
					String typeDef = itemType + " {";
					// START KGU#355 2017-03-30: #365 - initializers needs selectors
					// and we overwrite the array literal
					arrayLiteral = "{";
					// END KGU#355 2017-03-30
					for (int i = 0; i < nItems; i++) {
						typeDef += itemTypes.get(i) + " comp" + i + "; ";
						// START KGU#355 2017-03-30: #365 - initializers needs selectors
						if (i > 0) arrayLiteral += ", ";
						arrayLiteral += ".comp" + i + "<-" + items.get(i);
						// END KGU#355 2017-03-30
					}
					// START KGU#355 2017-03-30: #365 - initializers needs selectors
					//typeDef += "}";
					typeDef = typeDef.trim() + "};";
					arrayLiteral += "}";
					// END KGU#355 2017-03-30
					// START KGU#355 2017-03-30: #365 - it was not correct that types must be defined globally
					//this.addGlobalTypeDef(typeDef, "TODO: Define a sensible 'ItemType' for the loop further down", isDisabled);
					this.addCode(typeDef, indent, isDisabled);
					// END KGU#355 2017-03-30
					this.insertComment("TODO: Prepare the elements of the array according to defined type (or conversely).", indent);
				}
			}
			// We define a fixed array here
			addCode(itemType + " " + arrayName +  "[" + nItems + "] = "
					+ transform(arrayLiteral, false) + ";", indent, isDisabled);
			
			endValStr = Integer.toString(nItems);
		}
		else if (typeInfo != null && typeInfo.isArray()) {
			String limitName = "count" + nameSuffix;
			StringList typeDecls = getTransformedTypes(typeInfo, false);
			if (typeDecls.count() == 1) {
				itemType = typeDecls.get(0).substring(1);
				int lastAt = itemType.lastIndexOf('@');
				if (lastAt >= 0) {
					itemType = itemType.substring(lastAt+1);
					for (int i = 0; i <= lastAt; i++) {
						itemVar += "[]";
					}
				}
			}
			startValStr = Integer.toString(Math.max(0, typeInfo.getMinIndex(0)));
			int endVal = typeInfo.getMaxIndex(0);
			if (endVal > -1) {
				endValStr = Integer.toString(endVal + 1);
			}
			arrayName = valueList;
			
			// Start an extra block to encapsulate the additional definitions
			addCode("{", _indent, isDisabled);

			if (endValStr.equals("???")) {
				this.insertComment("TODO: Find out and fill in the number of elements of the array " + valueList + " here!", _indent);
			}
			addCode("int " + limitName + " = " + endValStr +";", indent, isDisabled);

			endValStr = limitName;
		}
		
		if (items != null || typeInfo != null) {
			
			// Definition of he loop index variable
			addCode("int " + indexName + ";", indent, isDisabled);

			// Creation of the loop header
			insertBlockHeading(
					_for, "for (" + indexName + " = " + startValStr + "; " +
					indexName + " < " + endValStr + "; " + indexName + "++)",
					indent);
			
			// Assignment of a single item to the given variable
			if (itemType.startsWith("union ")) {
				this.insertComment("TODO: Extract the value from the appropriate component here and care for type conversion!", _indent);
			}
			addCode(this.getIndent() + itemType + " " + itemVar + " = " +
					arrayName + "[" + indexName + "];", indent, isDisabled);

			// Add the loop body as is
			generateCode(_for.q, indent + this.getIndent());

			// Accomplish the loop
			insertBlockTail(_for, null, indent);

			// Close the extra block
			addCode("}", _indent, isDisabled);
			done = true;
		}
		else
		{
			// END KGU#355 2017-03-05
			// We have no strategy here, no idea how to find out the number and type of elements,
			// no idea how to iterate the members, so we leave it similar to C# and just add a TODO comment...
			this.insertComment("TODO: Rewrite this loop (there was no way to convert this automatically)", _indent);

			// Creation of the loop header
			insertBlockHeading(_for, "foreach (" + var + " in " + transform(valueList, false) + ")", _indent);
			// Add the loop body as is
			generateCode(_for.q, _indent + this.getIndent());
			// Accomplish the loop
			insertBlockTail(_for, null, _indent);
			
			done = true;
		}
		return done;
	}
	// END KGU#61 2016-03-22

	@Override
	protected void generateCode(While _while, String _indent) {
		
		insertComment(_while, _indent);
		

		String condition = transform(_while.getText().getLongString(), false)
				.trim();
		// START KGU#301 2016-12-01: Bugfix #301
		//if (!condition.startsWith("(") || !condition.endsWith(")")) {
		if (!isParenthesized(condition)) {
		// END KGU#301 2016-12-01
			condition = "(" + condition + ")";
		}

		insertBlockHeading(_while, "while " + condition, _indent);

		generateCode(_while.q, _indent + this.getIndent());

		insertBlockTail(_while, null, _indent);

	}

	@Override
	protected void generateCode(Repeat _repeat, String _indent) {
		
		insertComment(_repeat, _indent);

		insertBlockHeading(_repeat, "do", _indent);

		generateCode(_repeat.q, _indent + this.getIndent());

		// START KGU#301 2016-12-01: Bugfix #301
		//insertBlockTail(_repeat, "while (!(" 
		//		+ transform(_repeat.getText().getLongString()).trim() + "))", _indent);
		String condition = transform(_repeat.getText().getLongString()).trim();
		if (!isParenthesized(condition)) {
			condition = "(" + condition + ")";
		}
		insertBlockTail(_repeat, "while (!" + condition + ")", _indent);
		// END KGU#301 2016-12-01
	}

	@Override
	protected void generateCode(Forever _forever, String _indent) {
		
		insertComment(_forever, _indent);

		insertBlockHeading(_forever, "while (true)", _indent);

		generateCode(_forever.q, _indent + this.getIndent());

		insertBlockTail(_forever, null, _indent);
	}

	@Override
	protected void generateCode(Call _call, String _indent) {
 
		if (!insertAsComment(_call, _indent)) {

			boolean isDisabled = _call.isDisabled();
			insertComment(_call, _indent);
			// In theory, here should be only one line, but we better be prepared...
			StringList lines = _call.getText();
			for (int i = 0; i < lines.count(); i++) {
				String line = lines.get(i);
//				// START KGU#376 2017-04-13: Enh. #389 handle import calls - withdrawn here
//				if (!isDisabled && Call.isImportCall(lines.get(i))) {
//					generateImportCode(_call, line, _indent);
//				}
//				else
//				// END KGU#376 2017-04-13
				// Input or Output should not occur here
				addCode(transform(line, false) + ";", _indent, isDisabled);
			}
		}
		
	}

	// FIXME: Will have to be replaced by e.g. #include directives
//	// START KGU#376 2017-04-13: Enh. #389 support for import CALLS
//	/**
//	 * Subclassable code generator for an import CALL line. The CGenerator will
//	 * rely on the preamble generator to have already coded the important constant
//	 * definitions and variable declarations. So it just creates a comment line.
//	 * Subclasses may do something more meaningful here.
//	 * @param _call - the origination CALL element
//	 * @param _line - the current line with import CALL syntax
//	 * @param _indent - indentation string
//	 */
//	protected void generateImportCode(Call _call, String _line, String _indent) {
//		// Do nothing but place it as comment here. The important contents
//		// (constant definitions and variable declarations) will already have
//		// been put to the preamble.
//		boolean done = false;
//		String diagrName = _call.getSignatureString();
//		if (this.isInternalDeclarationAllowed() && Arranger.hasInstance()) {
//			Vector<Root> roots = Arranger.getInstance().findDiagramsByName(diagrName);
//			if (roots.size() == 1) {
//				Root imported = roots.get(0);
//				imported.getVarNames();	// This also initializes the constants information we may need here
//				insertComment("*** START " + _line + " *** ", _indent);		
//				generateCode(imported.children, _indent);
//				insertComment("*** END " + _line + " *** ", _indent);		
//				done = true;
//			}
//		}
//		if (!done) {
//			insertComment(_line, _indent);		
//		}
//	}
//	// END KGU#376 2017-04-13

	@Override
	protected void generateCode(Jump _jump, String _indent)
	{
		// START KGU 2015-10-18: The "export instructions as comments"
		// configuration had been ignored here
		// insertComment(_jump, _indent);
		// for(int i=0;i<_jump.getText().count();i++)
		// {
		// code.add(_indent+transform(_jump.getText().get(i))+";");
		// }
		if (!insertAsComment(_jump, _indent)) {
			
			boolean isDisabled = _jump.isDisabled();

			insertComment(_jump, _indent);

			// START KGU#380 2017-04-14: Bugfix #394 Done in another way now
			// KGU 2015-10-18: In case of an empty text generate a break
			// instruction by default.
			//boolean isEmpty = true;
			// END KGU#380 207-04-14
			
			StringList lines = _jump.getText();
			boolean isEmpty = lines.getLongString().trim().isEmpty();
			String preReturn = CodeParser.getKeywordOrDefault("preReturn", "return").trim();
			String preExit   = CodeParser.getKeywordOrDefault("preExit", "exit").trim();
			// START KGU#380 2017-04-14: Bugfix #394 - We don't consider superfluous lines anymore
			//String preLeave  = CodeParser.getKeywordOrDefault("preLeave", "leave").trim();
			//String preReturnMatch = Matcher.quoteReplacement(preReturn)+"([\\W].*|$)";
			//String preExitMatch   = Matcher.quoteReplacement(preExit)+"([\\W].*|$)";
			//String preLeaveMatch  = Matcher.quoteReplacement(preLeave)+"([\\W].*|$)";
			//for (int i = 0; isEmpty && i < lines.count(); i++) {
			//	String line = transform(lines.get(i)).trim();
			//	if (!line.isEmpty())
			//	{
			//		isEmpty = false;
			//	}
			String line = "";
			if (!isEmpty) {
				line = lines.get(0).trim();
			}
				// START KGU#74/KGU#78 2015-11-30: More sophisticated jump handling
				//code.add(_indent + line + ";");
				//if (line.matches(preReturnMatch))
				if (_jump.isReturn())
				{
					addCode("return " + line.substring(preReturn.length()).trim() + ";",
							_indent, isDisabled);
				}
				//else if (line.matches(preExitMatch))
				else if (_jump.isExit())
				{
					insertExitInstr(line.substring(preExit.length()).trim(), _indent, isDisabled);
				}
				// Has it already been matched with a loop? Then syntax must have been okay...
				else if (this.jumpTable.containsKey(_jump))
				{
					Integer ref = this.jumpTable.get(_jump);
					String label = this.labelBaseName + ref;
					if (ref.intValue() < 0)
					{
						insertComment("FIXME: Structorizer detected this illegal jump attempt:", _indent);
						insertComment(line, _indent);
						label = "__ERROR__";
					}
					addCode(this.getMultiLevelLeaveInstr() + " " + label + ";", _indent, isDisabled);
				}
				//else if (line.matches(preLeaveMatch))
				else if (_jump.isLeave())
				{
					// START KGU 2017-02-06: The "funny comment" was irritating and dubious itself
					// Seems to be an ordinary one-level break without need to concoct a jump statement
					// (Are there also strange cases - neither matched nor rejected? And how could this happen?)
					//addCode("break;\t// FIXME: Dubious occurrance of break instruction!", _indent, isDisabled);
					addCode("break;", _indent, isDisabled);
					// END KGU 2017-02-06
				}
				else if (!isEmpty)
				{
					insertComment("FIXME: jump/exit instruction of unrecognised kind!", _indent);
					insertComment(line, _indent);
				}
				// END KGU#74/KGU#78 2015-11-30
			}
//			if (isEmpty) {
//				addCode("break;", _indent, isDisabled);
//			}
//			// END KGU 2015-10-18
//		}
		// END KGU#380 207-04-14
	}

	// START KGU#47 2015-11-30: Offer at least a sequential execution (which is one legal execution order)
	protected void generateCode(Parallel _para, String _indent)
	{

		boolean isDisabled = _para.isDisabled();
		insertComment(_para, _indent);

		addCode("", "", isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================= START PARALLEL SECTION =================", _indent);
		insertComment("==========================================================", _indent);
		insertComment("TODO: add the necessary code to run the threads concurrently", _indent);
		addCode("{", _indent, isDisabled);

		for (int i = 0; i < _para.qs.size(); i++) {
			addCode("", "", isDisabled);
			insertComment("----------------- START THREAD " + i + " -----------------", _indent + this.getIndent());
			addCode("{", _indent + this.getIndent(), isDisabled);
			generateCode((Subqueue) _para.qs.get(i), _indent + this.getIndent() + this.getIndent());
			addCode("}", _indent + this.getIndent(), isDisabled);
			insertComment("------------------ END THREAD " + i + " ------------------", _indent + this.getIndent());
			addCode("", "", isDisabled);
		}

		addCode("}", _indent, isDisabled);
		insertComment("==========================================================", _indent);
		insertComment("================== END PARALLEL SECTION ==================", _indent);
		insertComment("==========================================================", _indent);
		addCode("", "", isDisabled);
	}
	// END KGU#47 2015-11-30
	


	/**
	 * Composes the heading for the program or function according to the
	 * C language specification.
	 * @param _root - The diagram root
	 * @param _indent - the initial indentation string
	 * @param _procName - the procedure name
	 * @param paramNames - list of the argument names
	 * @param paramTypes - list of corresponding type names (possibly null) 
	 * @param resultType - result type name (possibly null)
	 * @return the default indentation string for the subsequent stuff
	 */
	@Override
	protected String generateHeader(Root _root, String _indent, String _procName,
			StringList _paramNames, StringList _paramTypes, String _resultType)
	{
		// START KGU#178 2016-07-20: Enh. #160
		if (!topLevel)
		{
			code.add("");					
		}
		// END KGU#178 2016-07-20
		String pr = "program";
		if (_root.isSubroutine()) {
			pr = "function";
		} else if (_root.isInclude()) {
			pr = "includable";
		}
		insertComment(pr + " " + _root.getText().get(0), _indent);
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
		// END KGU#178 2016-07-20
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			insertCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			code.add("");
			// START KGU#236 2016-08-10: Issue #227
			//code.add("#include <stdio.h>");
			//code.add("");
			if (this.hasInput() || this.hasOutput() || this.usesFileAPI)
			{
				code.add("#define _CRT_SECURE_NO_WARNINGS");	// VisualStudio precaution 
				code.add("#include <stdio.h>");
				if (this.usesFileAPI) {
					code.add("#include <stdlib.h>");
					code.add("#include <string.h>");
					code.add("#include <errno.h>");
				}
				code.add("");
			}
			// START KGU#351 2017-02-26: Enh. #346 / KGU#3512017-03-17 had been mis-placed
			this.insertUserIncludes("");
			// START KGU#446 2017-10-27: Enh. #441
			this.includeInsertionLine = code.count();
			// END KGU#446 2017-10-27
			code.add("");
			// END KGU#351 2017-02-26
			// START KGU#376 2017-09-26: Enh. #389 - definitions from all included diagrams will follow
			insertGlobalDefinitions(_root, _indent, false);
			// END KGU#376 2017-09-26
			// END KGU#236 2016-08-10
			// START KGU#178 2016-07-20: Enh. #160
			subroutineInsertionLine = code.count();
			subroutineIndent = _indent;
			
			// START KGU#311 2016-12-22: Enh. #314 - insert File API routines if necessary
			if (this.usesFileAPI) {
				this.insertFileAPI("c");
			}
			// END KGU#311 2016-12-22
		}
		// END KGU#178 2016-07-20

		insertComment(_root, _indent);
		if (_root.isProgram())
			code.add("int main(void)");
		else {
			// Compose the function header
			this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo());
			String fnHeader = transformTypeWithLookup(_root.getResultType(),
					((this.returns || this.isResultSet || this.isFunctionNameSet) ? "int" : "void"));
			// START KGU#140 2017-01-31: Enh. #113 - improved type recognition and transformation
			boolean returnsArray = fnHeader.toLowerCase().contains("array") || fnHeader.contains("]");
			if (returnsArray) {
				fnHeader = transformArrayDeclaration(fnHeader, "");
			}
			// END KGU#140 2017-01-31
			fnHeader += " " + _procName + "(";
			for (int p = 0; p < _paramNames.count(); p++) {
				if (p > 0) { fnHeader += ", "; }
				// START KGU#140 2017-01-31: Enh. #113: Proper conversion of array types
				//fnHeader += (transformType(_paramTypes.get(p), "/*type?*/") + " " + 
				//		_paramNames.get(p)).trim();
				fnHeader += transformArrayDeclaration(transformTypeWithLookup(_paramTypes.get(p), "/*type?*/").trim(), _paramNames.get(p));
				// END KGU#140 2017-01-31
			}
			fnHeader += ")";
			insertComment("TODO: Revise the return type and declare the parameters.", _indent);
			// START KGU#140 2017-01-31: Enh. #113
			if (returnsArray) {
				insertComment("      C does not permit to return arrays - find an other way to pass the result!", _indent);
			}
			// END KGU#140 2017-01-31
			code.add(fnHeader);
		}
		code.add(_indent + "{");
		
		// START KGU#376 2017-09-26: Enh. #389 - insert the initialization code of the includables
		insertGlobalInitialisations(_indent + this.getIndent());
		// END KGU#376 2017-09-26
		
		return _indent + this.getIndent();
	}

	/**
	 * Generates some preamble (i.e. comments, language declaration section etc.)
	 * and adds it to this.code.
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param varNames - list of variable names introduced inside the body
	 */
	@Override
	protected String generatePreamble(Root _root, String _indent, StringList varNames)
	{
		insertComment("TODO: Check and accomplish variable declarations:", _indent);
        // START KGU#261/KGU#332 2017-01-26: Enh. #259/#335: Insert actual declarations if possible
		// START KGU#504 2018-03-13: Bugfix #520, #521: only insert declarations if conversion is allowed
		//insertDefinitions(_root, _indent, varNames, false);
		if (!this.suppressTransformation) {
			insertDefinitions(_root, _indent, varNames, false);
		}
		// END KGU#504 2018-03-13
		// END KGU#261/KGU#332 2017-01-26
		// START KGU#332 2017-01-30: Decomposed to ease sub-classing
		generateIOComment(_root, _indent);
		// END KGU#332 2017-01-30
		code.add(_indent);
		return _indent;
	}

	// START KGU#376 2017-09-26: Enh #389 - declaration stuff condensed to a method
	/**
	 * Inserts constant, type, and variable definitions for the passed-in {@link Root} {@code _root} 
	 * @param _root - the diagram the daclarations and definitions of are to be inserted
	 * @param _indent - the proper indentation as String
	 * @param _varNames - optionally the StringList of the variable names to be declared (my be null)
	 * @param _force - true means that the insertion is forced even if option {@link #isInternalDeclarationAllowed()} is set 
	 */
	protected void insertDefinitions(Root _root, String _indent, StringList _varNames, boolean _force) {
		// TODO: structured constants must be defined after the type definitions (see PasGenerator)!
		int lastLine = code.count();
		// START KGU#375 2017-04-12: Enh. #388 - we want to add new information but this is not to have an impact on _root 
		//this.typeMap = _root.getTypeInfo();
		this.typeMap = new HashMap<String, TypeMapEntry>(_root.getTypeInfo());
		// END KGU#375 2017-04-12
		// END KGU#261/KGU#332 2017-01-16
		// START KGU#375 2017-04-12: Enh. #388 special treatment of constants
		for (String constName: _root.constants.keySet()) {
			insertDeclaration(_root, constName, _indent, _force || !this.isInternalDeclarationAllowed());			
		}
		// END KGU#375 2017-04-12
		// START KGU#388 2017-09-26: Enh. #423 Place the necessary type definitions here
		if (_force || !this.isInternalDeclarationAllowed()) {
			this.generateTypeDefs(_root, _indent);
		}
		// END KGU#388 2017-09-26
        // START KGU 2015-11-30: List the variables to be declared (This will include merely declared variables!)
		for (int v = 0; v < _varNames.count(); v++) {
	        // START KGU#261/#332 2017-01-26: Enh. #259/#335: Insert actual declarations if possible
			//insertComment(varNames.get(v), _indent);
			String varName = _varNames.get(v);
			if (!_root.constants.containsKey(varName)) {
				insertDeclaration(_root, varName, _indent, _force || !this.isInternalDeclarationAllowed());
			}
			// END KGU#261/KGU#332 2017-01-16
		}
		// END KGU 2015-11-30
		// START KGU#376 2017-09-28: Enh. #423 - Specific care for merely declared (uninitialized) variables
		if (_root.isInclude()) {
			for (String id: this.typeMap.keySet()) {
				if (!id.startsWith(":") && !_varNames.contains(id)) {
					insertDeclaration(_root, id, _indent, _force || !this.isInternalDeclarationAllowed());
				}
			}
		}
		// END KGU#376 2017-09-28
		if (code.count() > lastLine) {
			code.add(_indent);
		}
	}
	// END KGU#376 2017-09-26
	
	// START KGU#375 2017-04-12: Enh. #388 common preparation of constants and variables
	/**
	 * Appends a definition or declaration, respectively, for constant or variable {@code _name}
	 * to {@code this.code}. If {@code _name} represents a constant, which is checked via {@link Root}
	 * {@code _root}, then its definition is introduced.
	 * @param _root - the owning diagram
	 * @param _name - the identifier of the variable or constant
	 * @param _indent - the current indentation (as String)
	 * @param _fullDecl - whether the declaration is to be forced in full format
	 */
	protected void insertDeclaration(Root _root, String _name, String _indent, boolean _fullDecl)
	{
		// START KGU#376 2017-09-26: Enh. #389
		if (wasDefHandled(_root, _name, false)) {
			return;
		}
		// END KGU#376 2017-09-26
		TypeMapEntry typeInfo = typeMap.get(_name);
		StringList types = null;
		String constValue = _root.constants.get(_name);
		String transfConst = transformType("const", "");
		if (typeInfo != null) {
			// START KGU#388 2017-09-30: Enh. #423
			//types = getTransformedTypes(typeInfo, true);
			if (typeInfo.isRecord()) {
				types = StringList.getNew(this.transformRecordTypeRef(typeInfo.typeName, false));
			}
			else {
				types = getTransformedTypes(typeInfo, true);
			}
			// END KGU#388 2017-09-30
		}
		// START KGU#375 2017-04-12: Enh. #388: Might be an imported constant
		// FIXME (KGU 2017-09-30): It should be extremely unlikely now that there isn't a typeMap entry
		else if (constValue != null) {
			getLogger().log(Level.WARNING, "insertDeclaration({0}, {1}, ...): MISSING TYPE MAP ENTRY FOR THIS CONSTANT!",
					new Object[]{_root, _name});
			String type = Element.identifyExprType(typeMap, constValue, false);
			if (!type.isEmpty()) {
				types = StringList.getNew(transformType(type, "int"));
				// We place a faked workaround entry
				typeMap.put(_name, new TypeMapEntry(type, null, _root, 0, true, false, true));
			}
		}
		// END KGU#375 2017-04-12
		// If the type is unambiguous and has no C-style declaration or may not be
		// declared between instructions then add the declaration here
		if (types != null && types.count() == 1 && 
				// FIXME: Replace isCStyleDeclared() with isDeclared()?
				//(typeInfo != null && !typeInfo.isCStyleDeclaredAt(null) || _fullDecl)) {			
				(typeInfo != null && !typeInfo.isDeclaredWithin(null) || _fullDecl)) {			
			String decl = types.get(0).trim();
			// START KGU#375 2017-04-12: Enh. #388 - types.get(0) doesn't contain anymore than e.g. "const"?
			if (decl.equals(transfConst) && constValue != null) {
				// The actual type spec is missing but we try to extract it from the value
				decl += " " + Element.identifyExprType(typeMap, constValue, false);
				decl = decl.trim();
			}
			// END KGU#375 2017-04-12
			if (decl.startsWith("@")) {
				decl = makeArrayDeclaration(decl, _name, typeInfo);
			}
			else {
				decl = decl + " " + _name;
			}
			// START KGU#375 2017-04-12: Enh. #388 support for constant definitions
			if (_root.constants.containsKey(_name)) {
				if (!decl.contains(transfConst + " ")) {
					decl = transfConst + " " + decl;
				}
				if (constValue != null) {
					// START KGU#388 2017-09-26: Enh. #423
					//decl += " = " + transform(constValue);
					if (constValue.contains("{") && constValue.endsWith("}") && typeInfo != null && typeInfo.isRecord()) {
						constValue = transformRecordInit(constValue, typeInfo);
					}
					else {
						constValue = transform(constValue);
					}
					decl += " = " + constValue;
					// END KGU#388 2017-09-26
				}
			}
			// END KGU#375 2017-04-12
			// START KGU#388 2017-09-27: Enh. #423
			if (typeInfo != null && typeInfo.isNamed()) {
				this.generateTypeDef(_root, typeInfo.typeName, typeInfo, _indent, false);
			}
			// END KGU#388 2017-09-27
			// START KGU#424 2017-09-26: Ensure the declaration comment doesn't get lost
			insertDeclComment(_root, _indent, _name);
			// Just ensure that the declaration is registered
			setDefHandled(_root.getSignatureString(false), _name);
			// END KGU#424 2017-09-26
			if (decl.contains("???")) {
				insertComment(decl + ";", _indent);
			}
			else {
				// START KGU#501 2018-02-22: Bugfix #517 In Java, C++, or C# we may need modifiers here
				//code.add(_indent + decl + ";");
				code.add(_indent + this.getModifiers(_root, _name) + decl + ";");
				// END KGU#501 2018-02-22
			}
		}
		// Add a comment if there is no type info or internal declaration is not allowed
		else if (types == null || _fullDecl){
			insertComment(_name + ";", _indent);
			// START KGU#424 2017-09-26: Ensure the declaration comment doesn't get lost
			setDefHandled(_root.getSignatureString(false), _name);
			// END KGU#424 2017-09-26
		}
		// END KGU#261/KGU#332 2017-01-16
	}
	// END KGU#375 2017-04-12
	
	// START KGU#501 2018-02-22: Bugfix #517
	/**
	 * Returns modifiers to be placed in front of the declaration OF {@code _name} for the
	 * diagram {@code _root}.<br/>
	 * Method is intended to be overridden by sub-classes. If the result is non-empty then
	 * it ought to be padded at the end.
	 * @param _root - the originating {@link Root} of the entity {@code _name}
	 * @param _name - the identifier  
	 * @return a sequence of appropriate modifiers like "private static " or an empty string
	 */
	protected String getModifiers(Root _root, String _name) {
		return "";
	}
	// END KGU#501 2018-02-22

	// START KGU#388 2017-09-26: Enh. #423
	/**
	 * Generates code that decomposes a record initializer into separate component assignments
	 * @param _lValue - the left side of the assignment (without modifiers!)
	 * @param _recordValue - the record initializer according to Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is o be commented out
	 * @param _typeEntry - used to interpret a simplified record initializer (may be null)
	 */
	// START KGU#559 2018-07-20: Enh. #563
	//protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled) {
	//	HashMap<String, String> comps = Instruction.splitRecordInitializer(_recordValue, null);
	protected void generateRecordInit(String _lValue, String _recordValue, String _indent, boolean _isDisabled, TypeMapEntry _typeEntry)
	{
		HashMap<String, String> comps = Instruction.splitRecordInitializer(_recordValue, _typeEntry);
	// END KGU#559 2018-07-20
		for (Entry<String, String> comp: comps.entrySet()) {
			String compName = comp.getKey();
			String compVal = comp.getValue();
			if (!compName.startsWith("§") && compVal != null) {
				// START KGU#560 2018-07-21: Enh. #564 - on occasion of #563, we fix recursive initializers, too
				//addCode(transform(_lValue + "." + compName + " <- " + compVal) + ";", _indent, _isDisabled);
				generateAssignment(_lValue + "." + compName, compVal, _indent, _isDisabled);
				// END KGU#560 2018-07-21
			}
		}
	}
	// END KGU#388 2017-09-26

	// START KGU#560 2018-07-21: Bugfix #564 Array initializers have to be decomposed if not occurring in a declaration
	/**
	 * Generates code that decomposes possible initializers into a series of separate assignments if
	 * there no compact translation, otherwise just adds appropriate transformed code.
	 * @param _lValue - the left side of the assignment (without modifiers!)
	 * @param _expr - the expression in Structorizer syntax
	 * @param _indent - current indentation level (as String)
	 * @param _isDisabled - indicates whether the code is o be commented out
	 */
	protected void generateAssignment(String _lValue, String _expr, String _indent, boolean _isDisabled) {
		if (_expr.contains("{") && _expr.endsWith("}")) {
			StringList pureExprTokens = Element.splitLexically(_expr, true);
			pureExprTokens.removeAll(" ");
			int posBrace = pureExprTokens.indexOf("{");
			if (pureExprTokens.count() >= 3 && posBrace <= 1) {
				if (posBrace == 1 && Function.testIdentifier(pureExprTokens.get(0), null)) {
					// Record initializer
					String typeName = pureExprTokens.get(0);							
					TypeMapEntry recType = this.typeMap.get(":"+typeName);
					this.generateRecordInit(_lValue, _expr, _indent, _isDisabled, recType);
				}
				else {
					// Array initializer
					StringList items = Element.splitExpressionList(pureExprTokens.subSequence(1, pureExprTokens.count()-1), ",", true);
					this.generateArrayInit(_lValue, items.subSequence(0, items.count()-1), _indent, _isDisabled, null, false);
				}
			}
			else {
				// FIXME Array initializers must be handled recursively!
				addCode(transform(_lValue + " <- " + _expr) + ";", _indent, _isDisabled);
			}
		}
		else {
			// FIXME Array initializers must be handled recursively!
			addCode(transform(_lValue + " <- " + _expr) + ";", _indent, _isDisabled);
		}
	}
	
	/**
	 * Generates code that decomposes an array initializer into a series of element assignments if there no
	 * compact translation.
	 * @param _lValue - the left side of the assignment (without modifiers!), i.e. the array name
	 * @param _arrayItems - the {@link StringList} of element expressions to be assigned (in index order)
	 * @param _indent - the current indentation level
	 * @param _isDisabled - whether the code is commented out
	 * @param _elemType - the {@link TypeMapEntry} of the element type is available
	 * @param _isDecl - if this is part of a declaration (i.e. a true initialization)
	 */
	protected String generateArrayInit(String _lValue, StringList _arrayItems, String _indent, boolean _isDisabled, String _elemType, boolean _isDecl)
	{
		if (_isDecl && this.isInternalDeclarationAllowed()) {
			return this.transform("{" + _arrayItems.concatenate(", ") + "}");
		}
		for (int i = 0; i < _arrayItems.count(); i++) {
			// initializers must be handled recursively!
			generateAssignment(_lValue + "[" + i + "]", _arrayItems.get(i), _indent, _isDisabled);
		}
		return null;
	}
	// END KGU#560 2018-07-21

	// START KGU#332 2017-01-30: Decomposition of generatePreamble() to ease sub-classing
	protected String makeArrayDeclaration(String _elementType, String _varName, TypeMapEntry typeInfo)
	{
		int nLevels = _elementType.lastIndexOf('@')+1;
		_elementType = (_elementType.substring(nLevels) + " " + _varName).trim();
		for (int i = 0; i < nLevels; i++) {
			int maxIndex = typeInfo.getMaxIndex(i);
			_elementType += "[" + (maxIndex >= 0 ? Integer.toString(maxIndex+1) : (i == 0 ? "" : "/*???*/") ) + "]";
		}
		return _elementType;
	}
	
	protected void generateIOComment(Root _root, String _indent)
	{
		// START KGU#236 2016-08-10: Issue #227 - don't express this information if not needed
		if (this.hasInput(_root)) {
		// END KGU#236 2016-08-10
			code.add(_indent);
			insertComment("TODO:", _indent);
			insertComment(
					"For any input using the 'scanf' function you need to fill the first argument.",
					_indent);
			insertComment(
					"http://en.wikipedia.org/wiki/Scanf#Format_string_specifications",
					_indent);
		// START KGU#236 2016-08-10: Issue #227
		}
		if (this.hasOutput(_root)) {
		// END KGU#236 2016-08-10
		code.add(_indent);
		insertComment("TODO:", _indent);
		insertComment(
				"For any output using the 'printf' function you need to fill the first argument:",
				_indent);
		insertComment(
				"http://en.wikipedia.org/wiki/Printf#printf_format_placeholders",
				_indent);
		// START KGU#236 2016-08-10: Issue #227
		}
		// END KGU#236 2016-08-10	
	}
	// START KGU#332 2017-01-30
	
	/**
	 * Creates the appropriate code for returning a required result and adds it
	 * (after the algorithm code of the body) to this.code)
	 * @param _root - the diagram root element
	 * @param _indent - the current indentation string
	 * @param alwaysReturns - whether all paths of the body already force a return
	 * @param varNames - names of all assigned variables
	 */
	@Override
	protected String generateResult(Root _root, String _indent, boolean alwaysReturns, StringList varNames)
	{
		if (_root.isProgram() && !alwaysReturns)
		{
			code.add(_indent);
			code.add(_indent + "return 0;");
		}
		else if (_root.isSubroutine() &&
				(returns || _root.getResultType() != null || isFunctionNameSet || isResultSet) && !alwaysReturns)
		{
			String result = "0";
			if (isFunctionNameSet)
			{
				result = _root.getMethodName();
			}
			else if (isResultSet)
			{
				int vx = varNames.indexOf("result", false);
				result = varNames.get(vx);
			}
			code.add(_indent);
			code.add(_indent + "return " + result + ";");
		}
		return _indent;
	}
	
	/**
	 * Method is to finish up after the text insertions of the diagram, i.e. to close open blocks etc. 
	 * @param _root 
	 * @param _indent
	 */
	@Override
	protected void generateFooter(Root _root, String _indent)
	{
		code.add(_indent + "}");		
	}

	
}
