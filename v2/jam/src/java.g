//
// This is the ANTLR grammar used by JAM to parse java source files.
//
// I would rather have this file live down in ...impl.jam.internal.parser,
// but that causes headaches during repackaging.
//
// This grammar was based on the work of those described in the comments
// below.
//
// Patrick Calahan <pcal@bea.com>
//

// weblogic:365 oam:66 wsee:8 jam:3

header {

package org.apache.xmlbeans.impl.jam_old.internal.parser.generated;

import org.apache.xmlbeans.impl.jam_old.*;
import org.apache.xmlbeans.impl.jam_old.internal.parser.ParamStructPool;
import org.apache.xmlbeans.impl.jam_old.editable.*;
import org.apache.xmlbeans.impl.jam_old.internal.elements.*;
import java.util.*;
import java.io.StringWriter;
import java.lang.reflect.Modifier;


}


/** Java 1.5/JSR14 Recognizer
 *
 * Run 'java Main [-showtree] directory-full-of-java-files'
 *
 * [The -showtree option pops up a Swing frame that shows
 *  the AST constructed from the parser.]
 *
 * Run 'java Main <directory full of java files>'
 *
 * Contributing authors:
 *		John Mitchell		johnm@non.net
 *		Terence Parr		parrt@magelang.com
 *		John Lilley			jlilley@empathy.com
 *		Scott Stanchfield	thetick@magelang.com
 *		Markus Mohnen       mohnen@informatik.rwth-aachen.de
 *      Peter Williams      pete.williams@sun.com
 *      Allan Jacobs        Allan.Jacobs@eng.sun.com
 *      Steve Messick       messick@redhills.com
 *      John Pybus			john@pybus.org
 *
 * Version 1.00 December 9, 1997 -- initial release
 * Version 1.01 December 10, 1997
 *		fixed bug in octal def (0..7 not 0..8)
 * Version 1.10 August 1998 (parrt)
 *		added tree construction
 *		fixed definition of WS,comments for mac,pc,unix newlines
 *		added unary plus
 * Version 1.11 (Nov 20, 1998)
 *		Added "shutup" option to turn off last ambig warning.
 *		Fixed inner class def to allow named class defs as statements
 *		synchronized requires compound not simple statement
 *		add [] after builtInType DOT class in primaryExpression
 *		"const" is reserved but not valid..removed from modifiers
 * Version 1.12 (Feb 2, 1999)
 *		Changed LITERAL_xxx to xxx in tree grammar.
 *		Updated java.g to use tokens {...} now for 2.6.0 (new feature).
 *
 * Version 1.13 (Apr 23, 1999)
 *		Didn't have (stat)? for else clause in tree parser.
 *		Didn't gen ASTs for interface extends.  Updated tree parser too.
 *		Updated to 2.6.0.
 * Version 1.14 (Jun 20, 1999)
 *		Allowed final/abstract on local classes.
 *		Removed local interfaces from methods
 *		Put instanceof precedence where it belongs...in relationalExpr
 *			It also had expr not type as arg; fixed it.
 *		Missing ! on SEMI in classBlock
 *		fixed: (expr) + "string" was parsed incorrectly (+ as unary plus).
 *		fixed: didn't like Object[].class in parser or tree parser
 * Version 1.15 (Jun 26, 1999)
 *		Screwed up rule with instanceof in it. :(  Fixed.
 *		Tree parser didn't like (expr).something; fixed.
 *		Allowed multiple inheritance in tree grammar. oops.
 * Version 1.16 (August 22, 1999)
 *		Extending an interface built a wacky tree: had extra EXTENDS.
 *		Tree grammar didn't allow multiple superinterfaces.
 *		Tree grammar didn't allow empty var initializer: {}
 * Version 1.17 (October 12, 1999)
 *		ESC lexer rule allowed 399 max not 377 max.
 *		java.tree.g didn't handle the expression of synchronized
 *		statements.
 * Version 1.18 (August 12, 2001)
 *      	Terence updated to Java 2 Version 1.3 by
 *		observing/combining work of Allan Jacobs and Steve
 *		Messick.  Handles 1.3 src.  Summary:
 *		o  primary didn't include boolean.class kind of thing
 *      	o  constructor calls parsed explicitly now:
 * 		   see explicitConstructorInvocation
 *		o  add strictfp modifier
 *      	o  missing objBlock after new expression in tree grammar
 *		o  merged local class definition alternatives, moved after declaration
 *		o  fixed problem with ClassName.super.field
 *      	o  reordered some alternatives to make things more efficient
 *		o  long and double constants were not differentiated from int/float
 *		o  whitespace rule was inefficient: matched only one char
 *		o  add an examples directory with some nasty 1.3 cases
 *		o  made Main.java use buffered IO and a Reader for Unicode support
 *		o  supports UNICODE?
 *		   Using Unicode charVocabulay makes code file big, but only
 *		   in the bitsets at the end. I need to make ANTLR generate
 *		   unicode bitsets more efficiently.
 * Version 1.19 (April 25, 2002)
 *		Terence added in nice fixes by John Pybus concerning floating
 *		constants and problems with super() calls.  John did a nice
 *		reorg of the primary/postfix expression stuff to read better
 *		and makes f.g.super() parse properly (it was METHOD_CALL not
 *		a SUPER_CTOR_CALL).  Also:
 *
 *		o  "finally" clause was a root...made it a child of "try"
 *		o  Added stuff for asserts too for Java 1.4, but *commented out*
 *		   as it is not backward compatible.
 *
 * Version 1.20 (October 27, 2002)
 *
 *      Terence ended up reorging John Pybus' stuff to
 *      remove some nondeterminisms and some syntactic predicates.
 *      Note that the grammar is stricter now; e.g., this(...) must
 *	be the first statement.
 *
 *      Trinary ?: operator wasn't working as array name:
 *          (isBig ? bigDigits : digits)[i];
 *
 *      Checked parser/tree parser on source for
 *          Resin-2.0.5, jive-2.1.1, jdk 1.3.1, Lucene, antlr 2.7.2a4,
 *	    and the 110k-line jGuru server source.
 *
 * Version 1.21.2 (March, 2003)
 *      Changes by Matt Quail to support generics (as per JDK1.5/JSR14)
 *      Notes:
 *      o We only allow the "extends" keyword and not the "implements"
 *        keyword, since thats what JSR14 seems to imply.
 *      o Thanks to Monty Zukowski for his help on the antlr-interest
 *        mail list.
 *      o Thanks to Alan Eliasen for testing the grammar over his
 *        Fink source base
 *
 *
 * This grammar is in the PUBLIC DOMAIN
 */


class JavaParser extends Parser;
options {
	k = 1;
	exportVocab=Java;                // Call its vocabulary "Java"
	codeGenMakeSwitchThreshold = 2;  // Some optimizations
	codeGenBitsetTestThreshold = 3;
	defaultErrorHandler = true;
	buildAST = false;
}



{
  // ========================================================================
  // Constants

  private static final boolean VERBOSE = false;

  // ========================================================================
  // Variables

  private String mLastJavadoc = null;
  private String /*EPackage*/ mPackage;
  private List mErrors = null;
  private List mImports = new ArrayList();
  private List mClasses = new ArrayList();

  //private ParamStructPool mParamPool = new ParamStructPool();
  private List mExceptionList = new ArrayList();
  private JamClassLoader mLoader = null;

  // ========================================================================
  // Public methods

  public void setClassLoader(JamClassLoader l) {
    mLoader = l;
  }

  public EClass[] getResults() {
    EClass[] out = new EClass[mClasses.size()];
    mClasses.toArray(out);
    return out;
  }

  // returns a collection of Strings and Throwables that were reported
  // as errors
  public Collection getErrors() { return mErrors; }

  public void reportError(antlr.RecognitionException re) {
    if (mErrors == null) mErrors = new ArrayList();
    mErrors.add(re);
  }

  public void reportError(Exception re) {
    if (mErrors == null) mErrors = new ArrayList();
    mErrors.add(re);
  }

  public void reportError(String msg) {
    if (mErrors == null) mErrors = new ArrayList();
    mErrors.add(msg);
  }

  private void applyJavadocs(EMember member) {
    if (mLastJavadoc != null) {
      member.createComment().setText(mLastJavadoc);
      if (VERBOSE) {
        System.out.println("adding javadocs to "+member.getSimpleName()+":");
        System.out.println(mLastJavadoc);
      }
    }
    mLastJavadoc = null;
  }

//  public void reportWarning(String msg) {
//  }


    /**
     * Counts the number of LT seen in the typeArguments production.
     * It is used in semantic predicates to ensure we have seen
     * enough closing '>' characters; which actually may have been
     * either GT, SR or BSR tokens.
     */
    private int ltCounter = 0;

    private EClass newClass(String simpleName) {
    //FIXME more to do here
      //EClass clazz = mResult.addNewClass(mPackage,simpleName,mImports);
      String[] importSpecs = new String[mImports.size()];
      mImports.toArray(importSpecs);
      EClass clazz = null; //FIXMEnew EClassImpl(mPackage,simpleName,mLoader,importSpecs);
      mClasses.add(clazz);
      return clazz;
    }
}


start : compilationUnit;

// Compilation Unit: In Java, this is a single file.  This is the start
//   rule for this parser
compilationUnit
{
  int modifiers;
}
	:	// A compilation unit starts with an optional package definition

		( ("package" pkg:IDENT { mPackage = pkg.getText(); } SEMI) |
		  (("import" spec:IDENT SEMI) { mImports.add(spec.getText()); }) |
		  javadoc |
		  SEMI |
      modifiers=modifiersList (classDefinition[modifiers] |
                               interfaceDefinition[modifiers])
    )*
		EOF
;

// Definition of a Java class
classDefinition[int modifiers]
	:	"class" tweener name:IDENT tweener {
      if (VERBOSE) System.out.println("creating class "+name.getText());
	    EClass clazz = newClass(name.getText());
	    clazz.setModifiers(modifiers);
	    clazz.setIsInterface(false);
	    applyJavadocs(clazz);
	  }
    // FIXME need to support generics
		("extends" tweener c1:IDENT tweener { clazz.setSuperclassUnqualified(c1.getText()); })?
	  ("implements" tweener c2:IDENT tweener { clazz.addInterfaceUnqualified(c2.getText()); }
	       (COMMA tweener c3:IDENT tweener { clazz.addInterfaceUnqualified(c3.getText()); })*
	  )?
		cb:classBlock[clazz] {}
	;

// Definition of a Java Interface
interfaceDefinition[int modifiers]
	:	"interface" tweener name:IDENT tweener {
		  EClass clazz = newClass(name.getText());
	    clazz.setModifiers(modifiers);
	    clazz.setIsInterface(true);
	    applyJavadocs(clazz);
	  }
    // FIXME need to support generics
	  ("extends" tweener c2:IDENT tweener { clazz.addInterfaceUnqualified(c2.getText()); }
	      (COMMA tweener c3:IDENT tweener { clazz.addInterfaceUnqualified(c3.getText()); }  )*
	  )?
		cb:classBlock[clazz] {}
	;


tweener
 : (ML_COMMENT)*
;

classBlock[EClass clazz]
	:	LCURLY
      (javadoc | member[clazz] | SEMI)*
		RCURLY
	;

// Now the various things that can be defined inside a class or interface...
// Note that not all of these are really valid in an interface (constructors,
// for example), and if this grammar were used for a compiler there would
// need to be some semantic checks to make sure we're doing the right thing...
member[EClass clazz]
{
  int modifiers;
}
:
		(modifiers=modifiersList
		  (modifiedMember[clazz,modifiers] |
		  classDefinition[modifiers] |
		  staticInitializer[modifiers]
		 ))
;

staticInitializer[int modifiers]
: (statement_block)
{
  if (modifiers != java.lang.reflect.Modifier.STATIC) {
    //throw new IllegalArgumentException("not valid static initializer");
  }
}
;



modifiedMember[EClass clazz, int modifiers]
 : token1:IDENT
(
  (constructor[clazz, modifiers, token1.getText()] |
   fieldOrMethod[clazz,modifiers, token1.getText()]
   )
)
;



constructor[EClass clazz, int modifiers, String name]
{
  ParamStructPool pool = new ParamStructPool();
}
  : (LPAREN (parameterList[pool])? RPAREN (throwsClause[mExceptionList])? statement_block)
{
  if (!name.equals(clazz.getSimpleName())) {
    throw new IllegalArgumentException("FIXME not a constructor '"+
    name+"'  '"+clazz.getSimpleName()+"'");
  }
  ConstructorImpl constr = (ConstructorImpl)clazz.addNewConstructor();
  constr.setModifiers(modifiers);
  pool.setParametersOn(constr);
  constr.setUnqualifiedThrows(mExceptionList);
  applyJavadocs(constr);

}
;

fieldOrMethod[EClass clazz, int modifiers, String type]
 {
   String name = null;
 }

 : (LBRACK RBRACK)* tweener  //FIXME array type handling is broken here
    n:IDENT tweener { name = n.getText(); }
   ((
     ((ASSIGN variable_value)? SEMI) {
       EField field = clazz.addNewField();
       field.setSimpleName(name);
       field.setUnqualifiedType(type);
       field.setModifiers(modifiers);
	     applyJavadocs(field);
	    }
	  )
	  |
	  (
	  { ParamStructPool pool = new ParamStructPool();  //FIXME can we reuse this please?
	  }
      (LPAREN (parameterList[pool])? RPAREN tweener (throwsClause[mExceptionList])? (statement_block | SEMI)) {
        if (VERBOSE) System.out.println("creating method "+name);
        MethodImpl method = (MethodImpl)clazz.addNewMethod();
        method.setSimpleName(name);
        method.setUnqualifiedReturnType(type);
        method.setModifiers(modifiers);
        pool.setParametersOn(method);
        method.setUnqualifiedThrows(mExceptionList);
        applyJavadocs(method);
      }
	  ))
;

variable_value
 : (options {greedy=false;} : (statement_block | .))*
;

static_initializer
: "static" tweener statement_block
;


// This is a list of exception classes that the method is declared to throw
throwsClause[List out]
	:	(

	  "throws" tweener t1:IDENT {
	    out.clear();
	    out.add(t1.getText());
	   } tweener
	   ( COMMA tweener t2:IDENT {
	     out.add(t2.getText());
	   } tweener)*

	   )
	;


// A list of formal parameters
parameterList[ParamStructPool pool]
	:	(parameter[pool] (COMMA parameter[pool])*)
;

// A formal parameter.
parameter[ParamStructPool pool]
{
  String type;
  String name;
}
: tweener ("final")? t:IDENT tweener n:IDENT tweener {
  pool.add(t.getText(),n.getText());
}
;


// we can ignore everything inside
statement_block
: (LCURLY
    (options {greedy=false;} : statement_block_contents )*
   RCURLY)
;

statement_block_contents
:  (statement_block |
    SL_COMMENT | ML_COMMENT |
    CHAR_LITERAL | STRING_LITERAL |
    .)
;


javadoc
: (c:ML_COMMENT { mLastJavadoc = c.getText(); }  )
;


modifiersList returns [int out]
{
  out = 0;
}
:
  (
    ("private"      { out = out | Modifier.PRIVATE; })      |
    ("public"       { out = out | Modifier.PUBLIC; })       |
    ("protected"    { out = out | Modifier.PROTECTED; })    |
    ("abstract"     { out = out | Modifier.ABSTRACT; })     |
    ("static"       { out = out | Modifier.STATIC; })       |
    ("final"        { out = out | Modifier.FINAL; })        |
    ("transient"    { out = out | Modifier.TRANSIENT; })    |
    ("synchronized" { out = out | Modifier.SYNCHRONIZED; }) |
    ("volatile"     { out = out | Modifier.VOLATILE; })     |
    ("native"       { out = out | Modifier.NATIVE; })       |
    ("strictfp"     { out = out | Modifier.STRICT; })
  )*
;

constant
	:	NUM_INT
	|	CHAR_LITERAL
	|	STRING_LITERAL
	|	NUM_FLOAT
	|	NUM_LONG
	|	NUM_DOUBLE
	;





//----------------------------------------------------------------------------
// The Java scanner
//----------------------------------------------------------------------------
class JavaLexer extends Lexer;

options {
	exportVocab=Java;      // call the vocabulary "Java"
	testLiterals=false;    // don't automatically test for literals
	k=4;                   // four characters of lookahead
	charVocabulary='\u0003'..'\uFFFF';
	// without inlining some bitset tests, couldn't do unicode;
	// I need to make ANTLR generate smaller bitsets; see
	// bottom of JavaLexer.java
	codeGenBitsetTestThreshold=20;
}



// OPERATORS

LPAREN			:	'('		;
RPAREN			:	')'		;
LBRACK			:	'['		;
RBRACK			:	']'		;
LCURLY			:	'{'		;
RCURLY			:	'}'		;
COLON			:	':'		;
COMMA			:	','		;
//DOT			  :	'.'		;
ASSIGN			:	'='		;
SEMI			:	';'		;

QUESTION		:	'?'		;
EQUAL			:	"=="	;
LNOT			:	'!'		;
BNOT			:	'~'		;
NOT_EQUAL		:	"!="	;
DIV				:	'/'		;
DIV_ASSIGN		:	"/="	;
PLUS			:	'+'		;
PLUS_ASSIGN		:	"+="	;
INC				:	"++"	;
MINUS			:	'-'		;
MINUS_ASSIGN	:	"-="	;
DEC				:	"--"	;
STAR			:	'*'		;
STAR_ASSIGN		:	"*="	;
MOD				:	'%'		;
MOD_ASSIGN		:	"%="	;
SR				:	">>"	;
SR_ASSIGN		:	">>="	;
BSR				:	">>>"	;
BSR_ASSIGN		:	">>>="	;
GE				:	">="	;
GT				:	">"		;
SL				:	"<<"	;
SL_ASSIGN		:	"<<="	;
LE				:	"<="	;
LT				:	'<'		;
BXOR			:	'^'		;
BXOR_ASSIGN		:	"^="	;
BOR				:	'|'		;
BOR_ASSIGN		:	"|="	;
LOR				:	"||"	;
BAND			:	'&'		;
BAND_ASSIGN		:	"&="	;
LAND			:	"&&"	;



// Whitespace -- ignored
WS	:	(	' '
		|	'\t'
		|	'\f'
			// handle newlines
		|	(	options {generateAmbigWarnings=false;}
			:	"\r\n"  // Evil DOS
			|	'\r'    // Macintosh
			|	'\n'    // Unix (the right way)
			)
			{ newline(); }
		)+
		{ _ttype = Token.SKIP; }
	;



// Single-line comments
SL_COMMENT
	:	"//"
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)
		{$setType(Token.SKIP); newline();}
	;

// multiple-line comments
ML_COMMENT
	:	"/*"
		(	/*	'\r' '\n' can be matched in one alternative or by matching
				'\r' in one iteration and '\n' in another.  I am trying to
				handle any flavor of newline that comes in, but the language
				that allows both "\r\n" and "\r" and "\n" to all be valid
				newline is ambiguous.  Consequently, the resulting grammar
				must be ambiguous.  I'm shutting this warning off.
			 */
			options {
				generateAmbigWarnings=false;
			}
		:
			{ LA(2)!='/' }? '*'
		|	'\r' '\n'		{newline();}
		|	'\r'			{newline();}
		|	'\n'			{newline();}
		|	~('*'|'\n'|'\r')
		)*
		"*/"
//		{$setType(Token.SKIP);}
	;


// character literals
CHAR_LITERAL
	:	'\'' ( ESC | ~'\'' ) '\''
	;

// string literals
STRING_LITERAL
	:	'"' (ESC|~('"'|'\\'))* '"'
	;


// escape sequence -- note that this is protected; it can only be called
//   from another lexer rule -- it will not ever directly return a token to
//   the parser
// There are various ambiguities hushed in this rule.  The optional
// '0'...'9' digit matches should be matched here rather than letting
// them go back to STRING_LITERAL to be matched.  ANTLR does the
// right thing by matching immediately; hence, it's ok to shut off
// the FOLLOW ambig warnings.
protected
ESC
	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
		|	'0'..'3'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
				(
					options {
						warnWhenFollowAmbig = false;
					}
				:	'0'..'7'
				)?
			)?
		|	'4'..'7'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
			)?
		)
	;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
	:	('0'..'9'|'A'..'F'|'a'..'f')
	;


// a dummy rule to force vocabulary to be all characters (except special
//   ones that ANTLR uses internally (0 to 2)
protected
VOCAB
	:	'\3'..'\377'
	;

// a numeric literal
NUM_INT
	{boolean isDecimal=false; Token t=null;}
    :   '.'
            (	('0'..'9')+ (EXPONENT)? (f1:FLOAT_SUFFIX {t=f1;})?
                {
				if (t != null && t.getText().toUpperCase().indexOf('F')>=0) {
                	_ttype = NUM_FLOAT;
				}
				else {
                	_ttype = NUM_DOUBLE; // assume double
				}
				}
            )?

	|	(	'0' {isDecimal = true;} // special case for just '0'
			(	('x'|'X')
				(											// hex
					// the 'e'|'E' and float suffix stuff look
					// like hex digits, hence the (...)+ doesn't
					// know when to stop: ambig.  ANTLR resolves
					// it correctly by matching immediately.  It
					// is therefor ok to hush warning.
					options {
						warnWhenFollowAmbig=false;
					}
				:	HEX_DIGIT
				)+
			|	('0'..'7')+									// octal
			)?
		|	('1'..'9') ('0'..'9')*  {isDecimal=true;}		// non-zero decimal
		)
		(	('l'|'L') { _ttype = NUM_LONG; }

		// only check to see if it's a float if looks like decimal so far
		|	{isDecimal}?
            (   '.' ('0'..'9')* (EXPONENT)? (f2:FLOAT_SUFFIX {t=f2;})?
            |   EXPONENT (f3:FLOAT_SUFFIX {t=f3;})?
            |   f4:FLOAT_SUFFIX {t=f4;}
            )
            {
			if (t != null && t.getText().toUpperCase() .indexOf('F') >= 0) {
                _ttype = NUM_FLOAT;
			}
            else {
	           	_ttype = NUM_DOUBLE; // assume double
			}
			}
        )?
	;


// A very loose java identifier, including '*' import identifiers
IDENT
	options {testLiterals=true;}
	:	('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$'|'.')*
	  (('*') | ( '[' | ']' ) )* // also covers wildcard imports and array types
	;


// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
	:	('e'|'E') ('+'|'-')? ('0'..'9')+
	;


protected
FLOAT_SUFFIX
	:	'f'|'F'|'d'|'D'
	;