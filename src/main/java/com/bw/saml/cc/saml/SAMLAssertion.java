package com.bw.saml.cc.saml;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.schema.XSAny;

/**
Simple examples of coding to the OpenSAML API.
Methods here can write and parse each of the three
main assertion types: authentication, authorization decision, and attributes.
 编码到OpenSAML API的简单示例。这里的方法可以编写和解析以下三种主要的断言类型：
 身份验证，授权决策和属性。
@author Will Provost
*/
/*
Copyright 2006-2009 by Will Provost.  
All rights reserved by Capstone Courseware, LLC.
*/
public class SAMLAssertion
    extends SAML
{
    private static void die ()
    {
        System.out.println ("Usage: java cc.saml.SAMLAssertion ");
        System.out.println ("  <write|read> <authn|attr|authz> <filename>");
        System.exit (-1);
    }

    /**
    Creates a file whose contents are a SAML authentication assertion.
     创建一个内容为SAML身份验证断言的文件。
    */
    public Assertion createStockAuthnAssertion (String idpEntityId,String assertionId,String spEntityId)
    {
        DateTime now = new DateTime ();
        Issuer issuer = create (Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue (idpEntityId);
        
        /*NameID nameID = create (NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setValue ("harold_dt");
        
        Subject subject = create (Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID (nameID);*/
        
        Conditions conditions = create 
            (Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore (now.minusSeconds (15));
        conditions.setNotOnOrAfter (now.plusSeconds (30));
        AudienceRestriction audienceRestriction = create(AudienceRestriction.class,AudienceRestriction.DEFAULT_ELEMENT_NAME);
        Audience audience = create(Audience.class,Audience.DEFAULT_ELEMENT_NAME);
        audience.setAudienceURI(spEntityId);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        
        AuthnContextClassRef ref = create (AuthnContextClassRef.class, 
            AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        ref.setAuthnContextClassRef (AuthnContext.PPT_AUTHN_CTX);
        
        // As of this writing, OpenSAML doesn't model the wide range of
        // authentication context namespaces defined in SAML 2.0.
        // For a real project we'd probably move on to 
        //    XSAny objects, setting QNames and values each-by-each
        //    a JAXB mapping of the required schema
        //    DOM-building
        // For classroom purposes the road ends here ...
        //在撰写本文时，OpenSAML并未对SAML 2.0中定义的各种身份验证上下文名称空间进行建模。
        // 对于真实的项目，我们可能会继续//
        // XSAny对象，逐个设置QNames和值
        // 所需模式的JAXB映射
        // DOM构建//为了课堂目的，这条路到此为止...
        AuthnContext authnContext = create 
            (AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
        authnContext.setAuthnContextClassRef (ref);

        AuthnStatement authnStatement = create 
            (AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
        authnStatement.setAuthnContext (authnContext);
        authnStatement.setAuthnInstant(now);
        
        Assertion assertion = 
            create (Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID (assertionId);
        assertion.setIssueInstant (now);
        assertion.setIssuer (issuer);
        assertion.getStatements ().add (authnStatement);
        assertion.setConditions(conditions);
        
        return assertion;
    }
    
    /**
    Parses a SAML authentication assertion as found in a file,
    and prints out information about issuer, subject, and 
    authentication context.
     解析文件中找到的SAML身份验证断言，并打印出有关颁发者，
     主题和身份验证上下文的信息。
    */
    public void readAuthnAssertion (String filename)
        throws Exception
    {
        Assertion assertion = (Assertion) readFromFile (filename);
        NameID nameID = assertion.getSubject ().getNameID ();
        
        System.out.println ("Assertion issued by " +
            assertion.getIssuer ().getValue ());
        System.out.println ("Subject name: " + nameID.getValue ());
        System.out.println ("  (Format " + nameID.getFormat () + ")");
        
        System.out.println ("Authentication context classes found:");
        for (Statement statement : assertion.getStatements ())
            if (statement instanceof AuthnStatement)
                System.out.println ("  " + ((AuthnStatement) statement)
                    .getAuthnContext ().getAuthnContextClassRef ()
                    .getAuthnContextClassRef ());
    }
        
    /**
    Creates a file whose contents are a SAML attribute assertion.
     创建一个内容为SAML属性声明的文件。
    */
    public Assertion createStockAttributeAssertion ()
        throws Exception
    {
        DateTime now = new DateTime ();
        Issuer issuer = create (Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue ("http://mycom.com/MyJavaAttributeService");
        
        NameID nameID = create (NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setFormat (NameID.TRANSIENT);
        nameID.setValue ("ga489Slge8+0nio9=");
        
        Subject subject = create (Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID (nameID);
        
        Conditions conditions = create 
            (Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore (now.minusMinutes (15));
        conditions.setNotOnOrAfter (now.plusMinutes (30));
        
        // Build attribute values as XMLObjects;
        //  there is an AttributeValue interface, but it's apparently dead code
        //将属性值构建为XMLObjects；
        // 有一个AttributeValue接口，但显然是死代码
        XMLObjectBuilder builder = Configuration.getBuilderFactory ()
            .getBuilder (XSAny.TYPE_NAME);

        XSAny value1 = (XSAny) builder.buildObject 
            (AttributeValue.DEFAULT_ELEMENT_NAME);
        value1.setTextContent ("William Whitford Provost");
        
        Attribute attribute1 = create 
            (Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute1.setName ("FullName");
        attribute1.getAttributeValues ().add (value1);
        
        XSAny value2 = (XSAny) builder.buildObject 
            (AttributeValue.DEFAULT_ELEMENT_NAME);
        value2.setTextContent ("Grand Poobah");
        
        Attribute attribute2 = create 
            (Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
        attribute2.setName ("JobTitle");
        attribute2.getAttributeValues ().add (value2);
        
        AttributeStatement statement = create (AttributeStatement.class, 
            AttributeStatement.DEFAULT_ELEMENT_NAME);
        statement.getAttributes ().add (attribute1);
        statement.getAttributes ().add (attribute2);

        Assertion assertion = 
            create (Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID ("Assertion12345789");
        assertion.setIssueInstant (now);
        assertion.setIssuer (issuer);
        assertion.setSubject (subject);
        assertion.getStatements ().add (statement);
        
        return assertion;
    }
    
    /**
    Parses a SAML attribute assertion as found in a file,
    and prints out all attributes.
     解析文件中找到的SAML属性声明，并打印出所有属性。
    */
    public void readAttributeAssertion (String filename)
        throws Exception
    {
        Assertion assertion = (Assertion) readFromFile (filename);
        NameID nameID = assertion.getSubject ().getNameID ();
        
        System.out.println ("Assertion issued by " +
            assertion.getIssuer ().getValue ());
        System.out.println ("Subject name: " + nameID.getValue ());
        System.out.println ("  (Format " + nameID.getFormat () + ")");
        
        System.out.println ("Attributes found:");
        for (Statement statement : assertion.getStatements ())
            if (statement instanceof AttributeStatement)
                for (Attribute attribute : 
                        ((AttributeStatement) statement).getAttributes ())
                {
                    System.out.print ("  " + attribute.getName () + ": ");
                    for (XMLObject value : attribute.getAttributeValues ())
                        if (value instanceof XSAny)
                            System.out.print 
                                (((XSAny) value).getTextContent () + " ");
                    System.out.println ();
                }
    }
    
    /**
    Creates a file whose contents are a SAML authorization-decision assertion.
     创建一个内容为SAML授权决策断言的文件。
     */
    public Assertion createStockAuthzDecisionAssertion ()
        throws Exception
    {
        DateTime now = new DateTime ();
        Issuer issuer = create (Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue ("http://mycom.com/MyJavaAuthorizationService");
        
        NameID nameID = create (NameID.class, NameID.DEFAULT_ELEMENT_NAME);
        nameID.setFormat (NameID.TRANSIENT);
        nameID.setValue ("ga489Slge8+0nio9=");
        
        Subject subject = create (Subject.class, Subject.DEFAULT_ELEMENT_NAME);
        subject.setNameID (nameID);
        
        Conditions conditions = create 
            (Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
        conditions.setNotBefore (now.minusMinutes (15));
        conditions.setNotOnOrAfter (now.plusMinutes (30));
        
        Action action = create (Action.class, Action.DEFAULT_ELEMENT_NAME);
        action.setAction ("read");
        action.setNamespace (Action.RWEDC_NS_URI);
        
        AuthzDecisionStatement statement = create 
            (AuthzDecisionStatement.class, 
                AuthzDecisionStatement.DEFAULT_ELEMENT_NAME);
        statement.setResource ("http://mycom.com/Repository/Private");
        statement.setDecision (DecisionTypeEnumeration.PERMIT);
        statement.getActions ().add (action);

        Assertion assertion = 
            create (Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
        assertion.setID ("Assertion12345789");
        assertion.setIssueInstant (now);
        assertion.setIssuer (issuer);
        assertion.setSubject (subject);
        assertion.getStatements ().add (statement);
        
        return assertion;
    }
    
    /**
    Parses a SAML authorization-decision assertion as found in a file,
    and prints out the essential information.
     解析文件中找到的SAML授权决定断言，并打印出基本信息。
    */
    public void readAuthzDecisionAssertion (String filename)
        throws Exception
    {
        Assertion assertion = (Assertion) readFromFile (filename);
        NameID nameID = assertion.getSubject ().getNameID ();
        
        System.out.println ("Assertion issued by " +
            assertion.getIssuer ().getValue ());
        System.out.println ("Subject name: " + nameID.getValue ());
        System.out.println ("  (Format " + nameID.getFormat () + ")");
        
        System.out.println ("Decisions found:");
        for (Statement statement : assertion.getStatements ())
            if (statement instanceof AuthzDecisionStatement)
            {
                AuthzDecisionStatement authz = 
                    (AuthzDecisionStatement) statement;
                System.out.print ("  " + authz.getDecision () + " { ");
                for (Action action : authz.getActions ())
                    System.out.print (action.getAction () + " ");
                System.out.println ("} on " + authz.getResource ());
            }
    }
}