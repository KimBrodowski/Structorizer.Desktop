/*
    This file is part of Structorizer.

    Structorizer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Structorizer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

 ***********************************************************************

    KSH Source Code Generator

    Copyright (C) 2008 Jan Peter Klippel

    This file has been released under the terms of the GNU Lesser General
    Public License as published by the Free Software Foundation.

    http://www.gnu.org/licenses/lgpl.html

 */

package lu.fisch.structorizer.generators;

/******************************************************************************************************
 *
 *      Author:         Jan Peter Klippel
 *
 *      Description:    KSH Source Code Generator
 *
 ******************************************************************************************************
 *
 *      Revision List
 *
 *      Author					Date			Description
 *      ------                  ----			-----------
 *      Jan Peter Klippel       2008.04.11      First Issue
 *      Bob Fisch               2008.04.12      Added "Fields" section for generator to be used as plugin
 *      Bob Fisch               2011.11.07      Fixed an issue while doing replacements
 *      Kay Gürtzig             2015.11.02      Inheritance changed (because the code was nearly
 *                                              identical to BASHGenerator - so why do it twice?)
 *                                              Function argument handling improved
 *      Kay Gürtzig             2016.01.08      Bugfix #96 (= KG#129): Variable names fetched
 *      Kay Gürtzig             2016-07-20      Enh. #160 (option to involve referred subroutines)
 *      Kay Gürtzig             2016.08.12      Enh. #231: Additions for Analyser checks 18 and 19 (variable name collisions)
 *      Kay Gürtzig             2017.01.05      Enh. #314: File API TODO comments added, issue #234 chr/ord support
 *      Kay Gürtzig             2017.02.27      Enh. #346: Insertion mechanism for user-specific include directives
 *      Kay Gürtzig             2017.05.16      Enh. #372: Export of copyright information
 *
 ******************************************************************************************************
 *
 *      Comment:		LGPL license (http://www.gnu.org/licenses/lgpl.html).
 *
 *      2015-11-02 <Kay Gürtzig>
 *      - Inheritance changed from Generator to BASHGenerator - hope that doesn't spoil the plugin idea
 *      - Implemented a way to pass function arguments into the named parameters
 *      
 ******************************************************************************************************///


import lu.fisch.structorizer.elements.Element;
import lu.fisch.structorizer.elements.Root;
import lu.fisch.utils.StringList;


public class KSHGenerator extends BASHGenerator {

	/************ Fields ***********************/
	protected String getDialogTitle()
	{
		return "Export KSH Code ...";
	}
	
	protected String getFileDescription()
	{
		return "KSH Source Code";
	}
	
	protected String getIndent()
	{
		return " ";
	}
	
	protected String[] getFileExtensions()
	{
		String[] exts = {"ksh", "sh"};
		return exts;
	}
	
    // START KGU 2015-10-18: New pseudo field
    @Override
    protected String commentSymbolLeft()
    {
    	return "#";
    }
    // END KGU 2015-10-18

    /************ Code Generation **************/
	
//	// START KGU 2016-01-08: Possible replacement (portable shell code) for the inherited modern BASH code
//	@Override
//	protected void generateCode(For _for, String _indent) {
//
//		code.add("");
//		insertComment(_for, _indent);
//		String counterStr = _for.getCounterVar();
//		String startValueStr = transform(_for.getStartValue());
//		String endValueStr = transform(_for.getEndValue());
//		int stepValue = _for.getStepConst();
//		String compOpr = " -le ";
//		if (stepValue < 0) {
//			compOpr = " -ge ";
//		}
//		code.add(_indent + counterStr + "=" + startValueStr);
//		code.add(_indent+"while [[ $" + counterStr + compOpr + endValueStr + " ]]");
//		// END KGU#30 2015-10-18
//		code.add(_indent+"do");
//		generateCode(_for.q, _indent + this.getIndent());
//		code.add(_indent + this.getIndent() + "let " + counterStr + "=" + counterStr + ((stepValue >= 0) ? "+" : "") + stepValue);
//		code.add(_indent+"done");	
//		code.add("");
//
//	}
//	// END KGU 2016-01-08

	public String generateCode(Root _root, String _indent) {

		String indent = _indent;
		// START KGU#178 2016-07-20: Enh. #160
		if (topLevel)
		{
			code.add("#!/usr/bin/ksh");
			insertComment("Generated by Structorizer " + Element.E_VERSION, _indent);
			// START KGU#363 2017-05-16: Enh. #372
			insertCopyright(_root, _indent, true);
			// END KGU#363 2017-05-16
			// START KGU#351 2017-02-26: Enh. #346
			this.insertUserIncludes("");
			// END KGU#351 2017-02-26
			subroutineInsertionLine = code.count();
			code.add("");
			// START KGU#311 2017-01-05: Enh. #314: We should at least put some File API remarks
			if (this.usesFileAPI) {
				insertComment("TODO The exported algorithms made use of the Structorizer File API.", _indent);
				insertComment("     Unfortunately there are no comparable constructs in shell", _indent);
				insertComment("     syntax for automatic conversion.", _indent);
				insertComment("     The respective lines are marked with a TODO File API comment.", _indent);
				insertComment("     You might try something like \"echo value >> filename\" for output", _indent);
				insertComment("     or \"while ... do ... read var ... done < filename\" for input.", _indent);
			}
			// END KGU#311 2017-01-05
			// START KGU#150/KGU#241 2017-01-05: Issue #234 - Provisional support for chr and ord functions
			if (!this.suppressTransformation)
			{
				boolean builtInAdded = false;
				if (occurringFunctions.contains("chr"))
				{
					code.add(indent);
					insertComment("chr() - converts decimal value to its ASCII character representation", indent);
					code.add(indent + "chr() {");
					code.add(indent + this.getIndent() + "printf \\\\$(printf '%03o' $1)");
					code.add(indent + "}");
					builtInAdded = true;
				}
				if (occurringFunctions.contains("ord"))
				{
					code.add(indent);
					insertComment("ord() - converts ASCII character to its decimal value", indent);
					code.add(indent + "ord() {");
					code.add(indent + this.getIndent() + "printf '%d' \"'$1\"");
					code.add(indent + "}");
					builtInAdded = true;
				}
				if (builtInAdded) code.add(indent);
			}
			// END KGU#150/KGU#241 2017-01-05
		}
		else
		{
			code.add("");
		}
		// END KGU#178 2016-07-20

		// START KGU 2015-11-02: Comments added
		insertComment(_root, _indent);
		// END KGU 2015-11-02
		if( _root.isSubroutine() ) {
			// START KGU#53 2015-11-02: Shell functions obtain their arguments via $1, $2 etc.
			//code.add(_root.getText().get(0)+" () {");
			String header = _root.getMethodName() + "()";
			code.add(header + " {");
			indent = indent + this.getIndent();
			StringList paraNames = _root.getParameterNames();
			for (int i = 0; i < paraNames.count(); i++)
			{
				code.add(indent + paraNames.get(i) + "=$" + (i+1));
			}
			// END KGU#53 2015-11-02
		}
		
		// START KGU#129 2016-01-08: Bugfix #96 - Now fetch all variable names from the entire diagram
		varNames = _root.getVarNames();
		insertComment("TODO: Check and revise the syntax of all expressions!", _indent);
		// END KGU#129 2016-01-08
		code.add("");
		//insertComment("TODO declare your variables here", _indent);
		//code.add("");
		generateCode(_root.children, _root.isProgram() ? _indent : _indent + this.getIndent());
		
		if( _root.isSubroutine() ) {
			code.add("}");
		}
		
		return code.getText();
		
	}
	
}


