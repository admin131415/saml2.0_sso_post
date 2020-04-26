package com.bw.saml.cc.saml;


import com.bw.saml.cc.xml.PrettyPrinter;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSAny;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
Utility that uses OpenSAML to carry out common SAML tasks.
 使用OpenSAML执行常见SAML任务的实用程序。
@author Will Provost
*/
/*
Copyright 2009 by Will Provost.  
All rights reserved by Capstone Courseware, LLC.
*/
public class SAML
{
    /**
     * 文件制作工具
     */
    private DocumentBuilder builder;
    private String issuerURL;
    /**
     * 安全随机标识符生成器
     */
    private static SecureRandomIdentifierGenerator generator;
    private static final String CM_PREFIX = "urn:oasis:names:tc:SAML:2.0:cm:";
    
    /**
    Parse the command line for a filename to read, and optionally a filename
    to write (absent which the application will write to the console).
    Reads the given file as an XMLObject, and then dumps using a simple
    {@link cc.xml.PrettyPrinter pretty printer}.
     解析命令行以读取文件名，并可选地写入文件名（缺少应用程序将写入控制台的文件名）。
     将给定文件读取为XMLObject，然后使用简单的{@link cc.xml.PrettyPrinter漂亮打印机}转储。
    */
    public static void main (String[] args)
        throws Exception
    {
        if (args.length == 0)
        {
            System.out.println 
                ("Usage: java cc.saml.SAML <inputFile> [<outputFile>]");
            System.exit (-1);
        }
        
        // Unadvertised, and not used in course exercises; 
        // just some internal testing ...
        String command = args[0];
        if (command.equals ("generate"))
        {
            String type = args[1];

            SAML handler = new SAML ("http://saml.r.us/AssertingParty");

            if (type.equals ("authn"))
                //创建身份验证断言
                //sender-vouches发件人凭证
                handler.printToFile (handler.createAuthnAssertion 
                    (handler.createSubject 
                        ("harold_dt", null, "sender-vouches",null),
                            AuthnContext.PPT_AUTHN_CTX), 
                    null);

            else if (type.equals ("attr"))
            {
                Subject subject = handler.createSubject 
                    ("louisdraper@abc.gov", NameID.EMAIL, null,null);
                    
                Map<String,String> attributes = new HashMap<String,String> ();
                attributes.put ("securityClearance", "C2");
                attributes.put ("roles", "editor,reviewer");
                handler.printToFile 
                    (handler.createAttributeAssertion (subject, attributes), 
                        null);
            }
            else
            {
                System.out.println 
                    ("Usage: java cc.saml.SAML <generate> authn|attr");
                System.exit (-1);
            }
        }
        else
        {
            SAML handler = new SAML ();        
            handler.printToFile (handler.readFromFile (args[0]), 
                args.length > 1 ? args[1] : null);
        }
    }
        
    /**
    Any use of this class assures that OpenSAML is bootstrapped.
    Also initializes an ID generator.
     此类的任何使用都可以确保OpenSAML被引导。还初始化一个ID生成器。
    */
    static 
    {
        try
        {
            DefaultBootstrap.bootstrap ();
            generator = new SecureRandomIdentifierGenerator ();
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
        }
    }
    
    /**
    Initialize JAXP DocumentBuilder instance for later use and reuse.
     初始化JAXP DocumentBuilder实例，以供以后使用和重用
    */
    public SAML ()
    {
        this (null);
    }
    
    /**
    Initialize JAXP DocumentBuilder instance for later use and reuse, and
    establishes an issuer URL.
     初始化JAXP DocumentBuilder实例以供以后使用和重用，并建立颁发者URL。
    @param issuerURL This will be used in all generated assertions
    这将在所有生成的断言中使用
    */
    public SAML (String issuerURL)
    {
        try
        {
            DocumentBuilderFactory factory = 
                DocumentBuilderFactory.newInstance ();
            factory.setNamespaceAware (true);
            builder = factory.newDocumentBuilder ();
            
            this.issuerURL = issuerURL;
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
        }
    }
    
    /**
    <u>Slightly</u> easier way to create objects using OpenSAML's 
    builder system.
     <u>使用OpenSAML的构建器系统创建对象的方法</ u>稍微简单一些。
    */
    // cast to SAMLObjectBuilder<T> is caller's choice
    //转换为SAMLObjectBuilder <T>是调用者的选择
    @SuppressWarnings ("unchecked")
    public <T> T create (Class<T> cls, QName qname)
    {
        return (T) ((XMLObjectBuilder) 
            Configuration.getBuilderFactory ().getBuilder (qname))
                .buildObject (qname);
    }
    
    /**
    Helper method to add an XMLObject as a child of a DOM Element.
     将XMLObject添加为DOM元素的子项的Helper方法。
    */
    public static Element addToElement (XMLObject object, Element parent)
        throws IOException, MarshallingException, TransformerException
    {
        Marshaller out = 
            Configuration.getMarshallerFactory ().getMarshaller (object);
        return out.marshall (object, parent);
    }

    /**
    Helper method to get an XMLObject as a DOM Document.
     获取XMLObject作为DOM文档的Helper方法。
    */
    public Document asDOMDocument (XMLObject object)
        throws IOException, MarshallingException, TransformerException
    {
        Document document = builder.newDocument ();
        Marshaller out = 
            Configuration.getMarshallerFactory ().getMarshaller (object);
        out.marshall (object, document);
        return document;
    }

    /**
    Helper method to pretty-print any XML object to a file.
     用于将任何XML对象漂亮地打印到文件的Helper方法。
    */
    public void printToFile (XMLObject object, String filename)
        throws IOException, MarshallingException, TransformerException
    {
        Document document = asDOMDocument (object);
        
        String result = PrettyPrinter.prettyPrint (document);
        if (filename != null)
        {
            PrintWriter writer = new PrintWriter (new FileWriter (filename));
            writer.println (result);
            writer.close ();
        }
        else
            System.out.println (result);
    }

    /**
    Helper method to read an XML object from a DOM element.
     从DOM元素读取XML对象的Helper方法。
    */
    public static XMLObject fromElement (Element element)
        throws IOException, UnmarshallingException, SAXException
    {
        return Configuration.getUnmarshallerFactory ()
            .getUnmarshaller (element).unmarshall (element);    
    }

    /**
    Helper method to read an XML object from a file.
     从文件读取XML对象的Helper方法。
    */
    public XMLObject readFromFile (String filename)
        throws IOException, UnmarshallingException, SAXException
    {
        return fromElement (builder.parse (filename).getDocumentElement ());    
    }

    /**
    Helper method to spawn a new Issuer element based on our issuer URL.
     根据我们的发行者URL生成新的Issuer元素的Helper方法。
    */
    public Issuer spawnIssuer ()
    {
        Issuer result = null;
        if (issuerURL != null)
        {
            result = create (Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
            result.setValue (issuerURL);
        }
        
        return result;
    }
    
    /**
    Returns a SAML subject.
     返回一个SAML主题。
    @param username The subject name
    @param format If non-null, we'll set as the subject name format
    @param confirmationMethod If non-null, we'll create a SubjectConfirmation
        element and use this as the Method attribute; must be "sender-vouches"
        or "bearer", as HOK would require additional parameters and so is NYI
    如果不为null，我们将创建SubjectConfirmation元素并将其用作Method属性；
    必须为“发件人凭证”或“承担者”，因为HOK需要其他参数，NYI也是
    */
    public Subject createSubject
        (String username, String format, String confirmationMethod,String recipient)
    {
        NameID nameID = create (NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue (username);
        if (format != null)
            nameID.setFormat (format);
        
        Subject subject = create (Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID (nameID);
        
        if (confirmationMethod != null)
        {
            SubjectConfirmation confirmation = create 
                (SubjectConfirmation.class, 
                    SubjectConfirmation.DEFAULT_ELEMENT_NAME);
            confirmation.setMethod (CM_PREFIX + confirmationMethod);
            SubjectConfirmationData confirmationData = create(SubjectConfirmationData.class,SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
            confirmationData.setRecipient(recipient);
            confirmation.setSubjectConfirmationData(confirmationData);
            subject.getSubjectConfirmations ().add (confirmation);
        }

        return subject;        
    }
    
    /**
    Returns a SAML assertion with generated ID, current timestamp, given
    subject, and simple time-based conditions.
     返回具有生成的ID，当前时间戳，给定主题和简单的基于时间的条件的SAML声明。
    @param subject Subject of the assertion
    */
    public Assertion createAssertion (Subject subject)
    {
        Assertion assertion = 
            create (Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID (generator.generateIdentifier ());

        DateTime now = new DateTime ();
        assertion.setIssueInstant (now);
        
        if (issuerURL != null)
            assertion.setIssuer (spawnIssuer ());
        
        assertion.setSubject (subject);

        Conditions conditions = create 
            (Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore (now.minusSeconds (10));
        conditions.setNotOnOrAfter (now.plusMinutes (30));
        assertion.setConditions (conditions);

        return assertion;
    }
    
    /**
    Helper method to generate a response, based on a pre-built assertion.
     根据预先建立的断言生成响应的Helper方法。
    */
    public Response createResponse (Assertion assertion)
    {
        return createResponse (assertion, null);
    }
    
    /**
    Helper method to generate a shell response with a given status code
    and query ID.
     生成具有给定状态码和查询ID的Shell响应的Helper方法。
    */
    public Response createResponse (String statusCode, String inResponseTo)
    {
        return createResponse (statusCode, null, inResponseTo);
    }
    
    /**
    Helper method to generate a shell response with a given status code,
    status message, and query ID.
     使用给定状态代码，状态消息和查询ID生成Shell响应的Helper方法。
    */
    public Response createResponse 
            (String statusCode, String message, String inResponseTo)
    {
        Response response = create 
            (Response.class, Response.DEFAULT_ELEMENT_NAME);
        response.setID (generator.generateIdentifier ());

        if (inResponseTo != null)
            response.setInResponseTo (inResponseTo);
            
        DateTime now = new DateTime ();
        response.setIssueInstant (now);
        
        if (issuerURL != null)
            response.setIssuer (spawnIssuer ());
        
        StatusCode statusCodeElement = create 
            (StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
        statusCodeElement.setValue (statusCode);
        
        Status status = create (Status.class, Status.DEFAULT_ELEMENT_NAME);
        status.setStatusCode (statusCodeElement);
        response.setStatus (status);

        if (message != null)
        {
            StatusMessage statusMessage = create 
                (StatusMessage.class, StatusMessage.DEFAULT_ELEMENT_NAME);
            statusMessage.setMessage (message);
            status.setStatusMessage (statusMessage);
        }
        
        return response;
    }
    
    /**
    Helper method to generate a response, based on a pre-built assertion
    and query ID.
     根据预先建立的断言和查询ID生成响应的Helper方法。
    */
    public Response createResponse (Assertion assertion, String inResponseTo)
    {
        Response response = 
            createResponse (StatusCode.SUCCESS_URI, inResponseTo);

        response.getAssertions ().add (assertion);
        
        return response;
    }
    
    /**
    Returns a SAML authentication assertion.
     返回SAML身份验证声明。
    @param subject The subject of the assertion
    @param authnCtx The "authentication context class reference",
      e.g. AuthnContext.PPT_AUTHN_CTX
    “认证上下文类参考”，例如AuthnContext.PPT_AUTHN_CTX
    */
    public Assertion createAuthnAssertion (Subject subject, String authnCtx)
    {
        Assertion assertion = createAssertion (subject);

        AuthnContextClassRef ref = create (AuthnContextClassRef.class, 
            AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        ref.setAuthnContextClassRef (authnCtx);
        
        // As of this writing, OpenSAML doesn't model the wide range of
        // authentication context namespaces defined in SAML 2.0.
        // For a real project we'd probably move on to 
        //    XSAny objects, setting QNames and values each-by-each
        //    a JAXB mapping of the required schema
        //    DOM-building
        // For classroom purposes the road ends here ...
        //在撰写本文时，OpenSAML并未对SAML 2.0中定义的各种身份验证上下文名称空间进行建模。
        // 对于真实的项目，我们可能会继续
        // XSAny对象，逐个设置QNames和值
        // 所需模式的JAXB映射
        // DOM构建
        // 为了课堂目的，这条路到此为止...
        AuthnContext authnContext = create 
            (AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
        authnContext.setAuthnContextClassRef (ref);

        AuthnStatement authnStatement = create 
            (AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnContext (authnContext);
        
        assertion.getStatements ().add (authnStatement);
        
        return assertion;
    }
    
    /**
    Adds a SAML attribute to an attribute statement.
     将SAML属性添加到属性语句。
    @param statement Existing attribute statement
    @param name Attribute name
    @param value Attribute value
    */
    public void addAttribute 
        (AttributeStatement statement, String name, String value)
    {
        // Build attribute values as XMLObjects;
        //  there is an AttributeValue interface, but it's apparently dead code
        //将属性值构建为XMLObjects；
        // 有一个AttributeValue接口，但显然是死代码
        final XMLObjectBuilder builder =
            Configuration.getBuilderFactory ().getBuilder (XSAny.TYPE_NAME);

        XSAny valueElement = (XSAny) builder.buildObject 
            (AttributeValue.DEFAULT_ELEMENT_NAME);
        valueElement.setTextContent (value);

        Attribute attribute = create 
            (Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute.setName (name);
        attribute.getAttributeValues ().add (valueElement);

        statement.getAttributes ().add (attribute);
    }

    /**
    Returns a SAML attribute assertion.
     返回SAML属性断言
    @param subject Subject of the assertion
    @param attributes Attributes to be stated (may be null)
    */
    public Assertion createAttributeAssertion 
        (Subject subject, Map<String,String> attributes)
    {
        Assertion assertion = createAssertion (subject);
        
        AttributeStatement statement = create (AttributeStatement.class, 
            AttributeStatement.DEFAULT_ELEMENT_NAME);
        if (attributes != null)
            for (Map.Entry<String,String> entry : attributes.entrySet ())
                addAttribute (statement, entry.getKey (), entry.getValue ());

        assertion.getStatements ().add (statement);

        return assertion;
    }
}
