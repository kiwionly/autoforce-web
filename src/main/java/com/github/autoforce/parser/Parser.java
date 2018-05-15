package com.github.autoforce.parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.apache.commons.io.IOUtils;
import apex.jorje.data.ast.CompilationUnit;
import apex.jorje.data.ast.CompilationUnit.ClassDeclUnit;
import apex.jorje.data.ast.CompilationUnit.EnumDeclUnit;
import apex.jorje.data.ast.CompilationUnit.InterfaceDeclUnit;
import apex.jorje.data.ast.CompilationUnit.TriggerDeclUnit;
import apex.jorje.data.errors.ApexUserException;
import apex.jorje.data.errors.SyntaxError.IllegalStringLiteral;
import apex.jorje.data.errors.SyntaxError.MissingClosingParenthesis;
import apex.jorje.data.errors.SyntaxError.MissingSemicolon;
import apex.jorje.data.errors.SyntaxError.NumberFormatError;
import apex.jorje.data.errors.SyntaxError.NumberNotInRange;
import apex.jorje.data.errors.SyntaxError.UnexpectedEOF;
import apex.jorje.data.errors.SyntaxError.UnexpectedSyntaxError;
import apex.jorje.data.errors.SyntaxError.UnexpectedToken;
import apex.jorje.data.errors.TypeError.MismatchedTypes;
import apex.jorje.data.errors.TypeError.TypeNotFound;
import apex.jorje.data.errors.SyntaxError;
import apex.jorje.data.errors.TypeError;
import apex.jorje.data.errors.UserError;
import apex.jorje.data.errors.UserError.Lexical;
import apex.jorje.data.errors.UserError.Semantic;
import apex.jorje.data.errors.UserError.Syntax;
import apex.jorje.data.errors.UserError.Type;
import apex.jorje.parser.impl.ApexLexerImpl;
import apex.jorje.parser.impl.ApexParserImpl;
import apex.jorje.parser.impl.CaseInsensitiveReaderStream;

import apex.jorje.data.Loc.RealLoc;

public class Parser
{
    private ApexParserImpl parser;

    public Parser(File file) throws Exception
    {
	String inputString = IOUtils.toString(new FileInputStream(file));

	createParser(inputString);
    }

    public Parser(String inputString) throws Exception
    {
	createParser(inputString);
    }

    private void createParser(String inputString)
    {
	CharStream stream = CaseInsensitiveReaderStream.create(inputString);
	ApexLexerImpl lexer = new ApexLexerImpl(stream);
	TokenStream tokenStream = new CommonTokenStream(lexer);

	parser = new ApexParserImpl(tokenStream);
    }

    public Map<String, Object> parse() throws Exception
    {
	final Map<String, Object> compileResult = new HashMap<String, Object>();

	CompilationUnit cu = null;

	try
	{
	    cu = parser.compilationUnit();
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}

	if (cu == null)
	{
	    System.out.println("compilationUnit is null");

	    return null;
	}

	boolean suc = checkCompileOK(cu);

	System.out.println("compile result = " + suc);

	List<ApexUserException> errors = parser.getSyntaxErrors();

	parseError(compileResult, errors);

	return compileResult;
    }

    private boolean checkCompileOK(CompilationUnit cu)
    {
	boolean suc = cu.match(new CompilationUnit.MatchBlockWithDefault<Boolean>() {

	    @Override
	    public Boolean _case(ClassDeclUnit x)
	    {
		return x.body != null;
	    }

	    @Override
	    public Boolean _case(EnumDeclUnit x)
	    {
		return x.body != null;
	    }

	    @Override
	    public Boolean _case(InterfaceDeclUnit x)
	    {
		System.out.println(x.toString());
		return x.body != null;
	    }

	    @Override
	    public Boolean _case(TriggerDeclUnit x)
	    {
		return x.name != null;
	    }

	    @Override
	    protected Boolean _default(CompilationUnit x)
	    {
		return false;
	    }
	});

	return suc;
    }

    private void parseError(final Map<String, Object> compileResult, List<ApexUserException> errors)
    {
	for (ApexUserException e : errors)
	{
	    if (isDisplayableError(e))
	    {
		UserError error = e.getError();

		error.match(new UserError.MatchBlockWithDefault<Map<String, Object>>() {

		    @Override
		    public Map<String, Object> _case(Lexical lexical)
		    {
			return null;
		    }

		    @Override
		    public Map<String, Object> _case(Syntax syntaxError)
		    {
			Map<String, Object> error = extractSyntaxError(syntaxError);
			compileResult.put("syntax", error);

			return error;
		    }

		    @Override
		    public Map<String, Object> _case(Type type)
		    {
			Map<String, Object> error = extractTypeError(type);
			compileResult.put("type", error);

			return error;
		    }

		    @Override
		    public Map<String, Object> _case(Semantic sematic)
		    {
			return null;
		    }

		    @Override
		    protected Map<String, Object> _default(UserError userError)
		    {
			return null;
		    }
		});
	    }

	}
    }

    private Map<String, Object> extractTypeError(Type type)
    {
	final Map<String, Object> error = new HashMap<String, Object>();

	TypeError se = type.error;

	se.match(new TypeError.MatchBlock<Map<String, Object>>() {

	    @Override
	    public Map<String, Object> _case(MismatchedTypes ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);
		value.setMessage("expecting " + ex.expecting + ", but found " + ex.found);

		error.put("MismatchedTypes", value);

		return error;
	    }

	    @Override
	    public Map<String, Object> _case(TypeNotFound ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);
		value.setMessage(ex.notFound + " not found");

		error.put("TypeNotFound", value);

		return error;
	    }

	});

	return error;
    }

    private Map<String, Object> extractSyntaxError(Syntax syntax)
    {
	final Map<String, Object> error = new HashMap<String, Object>();

	SyntaxError se = syntax.error;

	se._switch(new SyntaxError.SwitchBlock() {

	    @Override
	    public void _case(UnexpectedToken ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);
		value.setMessage(ex.token);

		error.put("UnexpectedToken", value);
	    }

	    @Override
	    public void _case(NumberFormatError ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);
		value.setMessage(ex.text);

		error.put("NumberFormatError", value);
	    }

	    @Override
	    public void _case(NumberNotInRange ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);
		value.setMessage(ex.lowerBound + " " + ex.upperBound);

		error.put("NumberNotInRange", value);
	    }

	    @Override
	    public void _case(IllegalStringLiteral ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);
		value.setMessage(ex.message);

		error.put("IllegalStringLiteral", value);
	    }

	    @Override
	    public void _case(MissingClosingParenthesis ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);

		error.put("MissingClosingParenthesis", value);
	    }

	    @Override
	    public void _case(MissingSemicolon ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);

		error.put("MissingSemicolon", value);
	    }

	    @Override
	    public void _case(UnexpectedEOF ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);

		error.put("UnexpectedEOF", value);
	    }

	    @Override
	    public void _case(UnexpectedSyntaxError ex)
	    {
		RealLoc realloc = (RealLoc) ex.loc;
		ErrorObject value = new ErrorObject(realloc);
		value.setMessage(ex.message);

		error.put("UnexpectedSyntaxError", value);
	    }
	});

	return error;
    }

    private static boolean isDisplayableError(ApexUserException apexException)
    {
	return apexException.getCause() instanceof RecognitionException;
    }

    public static void main(String[] args) throws Exception
    {
	Parser parser = new Parser(new File("ap.cls"));

	Map<String, Object> compileResult = parser.parse();

	if (compileResult == null)
	{
	    System.out.println("no compilation result");
	    return;
	}

	if (compileResult.isEmpty())
	{
	    System.out.println("no syntax error");

	    return;
	}

	// get error here
	Set<String> key = compileResult.keySet();

	for (String k : key)
	{
	    // System.out.println(k);

	    @SuppressWarnings("unchecked")
	    Map<String, Object> error = (Map<String, Object>) compileResult.get(k);

	    Set<String> er = error.keySet();

	    for (String ke : er)
	    {
		System.out.println(ke);

		ErrorObject errorObject = (ErrorObject) error.get(ke);
		System.out.println(errorObject);
	    }
	}
    }

}
