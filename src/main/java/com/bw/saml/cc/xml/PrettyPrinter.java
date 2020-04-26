package com.bw.saml.cc.xml;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;


/**
This class "pretty prints" an XML stream to something more human-readable.
It duplicates the character content with some modifications to whitespace, 
restoring line breaks and a simple pattern of indenting child elements.

This version of the class acts as a SAX 2.0 <code>DefaultHandler</code>,
so to provide the unformatted XML just pass a new instance to a SAX parser.
Its output is via the {@link #toString toString} method.

One major limitation:  we gather character data for elements in a single
buffer, so mixed-content documents will lose a lot of data!  This works
best with data-centric documents where elements either have single values
or child elements, but not both.
 此类将XML流“漂亮地打印”为更易于理解的内容。它通过对空格进行一些修改，恢复换行符和缩进子
 元素的简单模式来复制字符内容。该类的版本充当SAX 2.0 <code> DefaultHandler </ code>，
 因此要提供未格式化的XML，只需将新实例传递给SAX解析器即可。其输出通过{@link #toString toString}
 方法。一个主要限制：我们在单个缓冲区中收集元素的字符数据，因此混合内容文档将丢失大量数据！
 这对于以数据为中心的文档效果最佳，在文档中，元素要么具有单个值，要么具有子元素，
 但不能同时具有两个值。
@author Will Provost
*/
/*
Copyright 2002-2003 by Will Provost.
All rights reserved.
*/
public class PrettyPrinter
    extends DefaultHandler
{
    /**
    Convenience method to wrap pretty-printing SAX pass over existing content.
     包装精美打印的SAX传递现有内容的便捷方法。
    */
    public static String prettyPrint (byte[] content)
    {
        try
        {
            PrettyPrinter pretty = new PrettyPrinter ();
            //SAX解析器工厂
            SAXParserFactory factory = SAXParserFactory.newInstance ();
            factory.setFeature
                ("http://xml.org/sax/features/namespace-prefixes", true);
            factory.newSAXParser ().parse 
                (new ByteArrayInputStream (content), pretty);
            return pretty.toString ();
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
            return "EXCEPTION: " + ex.getClass ().getName () + " saying \"" +
                ex.getMessage () + "\"";
        }
    }
    
    /**
    Convenience method to wrap pretty-printing SAX pass over existing content.
     包装精美打印的SAX传递现有内容的便捷方法
    */
    public static String prettyPrint (String content)
    {
        try
        {
            PrettyPrinter pretty = new PrettyPrinter ();
            SAXParserFactory factory = SAXParserFactory.newInstance ();
            factory.setFeature
                ("http://xml.org/sax/features/namespace-prefixes", true);
            factory.newSAXParser ().parse (content, pretty);
            return pretty.toString ();
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
            return "EXCEPTION: " + ex.getClass ().getName () + " saying \"" +
                ex.getMessage () + "\"";
        }
    }
    
    /**
    Convenience method to wrap pretty-printing SAX pass over existing content.
    */
    public static String prettyPrint (InputStream content)
    {
        try
        {
            PrettyPrinter pretty = new PrettyPrinter ();
            SAXParserFactory factory = SAXParserFactory.newInstance ();
            factory.setFeature
                ("http://xml.org/sax/features/namespace-prefixes", true);
            factory.newSAXParser ().parse (content, pretty);
            return pretty.toString ();
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
            return "EXCEPTION: " + ex.getClass ().getName () + " saying \"" +
                ex.getMessage () + "\"";
        }
    }

    /**
    Convenience method to wrap pretty-printing SAX pass over existing content.
    */
    public static String prettyPrint (Document doc)
        throws TransformerException
    {
        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream ();
            TransformerFactory.newInstance ().newTransformer()
                .transform (new DOMSource (doc), new StreamResult (buffer));
            byte[] rawResult = buffer.toByteArray ();
            buffer.close ();
            
            return prettyPrint (rawResult);
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
            return "EXCEPTION: " + ex.getClass ().getName () + " saying \"" +
                ex.getMessage () + "\"";
        }
    }
    //流适配器
    public static class StreamAdapter
        extends OutputStream
    {
        public StreamAdapter (Writer finalDestination)
        {
            this.finalDestination = finalDestination;
        }
        
        public void write (int b)
        {
            out.write (b);
        }
        
        public void flushPretty ()
            throws IOException
        {
            PrintWriter finalPrinter = new PrintWriter (finalDestination);
            finalPrinter.println 
                (PrettyPrinter.prettyPrint (out.toByteArray ()));
            finalPrinter.close ();
            out.close ();
        }
        
        private ByteArrayOutputStream out = new ByteArrayOutputStream ();
        Writer finalDestination;
    }
    
    /**
    Call this to get the formatted XML post-parsing.
     调用它以获取格式化的XML后解析
    */
    public String toString ()
    {
        return output.toString ();
    }
    
    /**
    Prints the XML declaration.
     打印XML声明。
    */
    public void startDocument () 
        throws SAXException 
    {
        output.append ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
              .append (endLine);
    }
    
    /**
    Prints a blank line at the end of the reformatted document.
     在重新格式化的文档末尾打印空白行。
    */
    public void endDocument () throws SAXException 
    {
        output.append (endLine);
    }

    /**
    Writes the start tag for the element.
    Attributes are written out, one to a text line.  Starts gathering
    character data for the element.
     写入元素的开始标签。属性被写出，一个到文本行。开始收集元素的字符数据。
    */
    public void startElement 
            (String URI, String name, String qName, Attributes attributes) 
        throws SAXException 
    {
        if (justHitStartTag)
            output.append ('>');

        output.append (endLine)
              .append (indent)
              .append ('<')
              .append (qName);

        int length = attributes.getLength ();        
        for (int a = 0; a < length; ++a)
            output.append (endLine)
                  .append (indent)
                  .append (standardIndent)
                  .append (attributes.getQName (a))
                  .append ("=\"")
                  .append (attributes.getValue (a))
                  .append ('\"');
                  
        if (length > 0)
            output.append (endLine)
                  .append (indent);
            
        indent += standardIndent;
        currentValue = new StringBuffer ();
        justHitStartTag = true;
    }
    
    /**
    Checks the {@link #currentValue} buffer to gather element content.
    Writes this out if it is available.  Writes the element end tag.
     检查{@link #currentValue}缓冲区以收集元素内容。如果可用，将其写出。写入元素结束标签。
    */
    public void endElement (String URI, String name, String qName) 
        throws SAXException 
    {
        indent = indent.substring 
            (0, indent.length () - standardIndent.length ());
        
        if (currentValue == null)
            output.append (endLine)
                  .append (indent)
                  .append ("</")
                  .append (qName)
                  .append ('>');
        else if (currentValue.length () != 0)
            output.append ('>')
                  .append (currentValue.toString ())
                  .append ("</")
                  .append (qName)
                  .append ('>');
        else
            output.append ("/>");
              
        currentValue = null;
        justHitStartTag = false;
    }
        
    /**
    When the {@link #currentValue} buffer is enabled, appends character
    data into it, to be gathered when the element end tag is encountered.
     启用{@link #currentValue}缓冲区后，会将字符数据附加到其中，以便在遇到元素结束标记时收集。
    */
    public void characters (char[] chars, int start, int length) 
        throws SAXException 
    {
        if (currentValue != null)
            currentValue.append (escape (chars, start, length));
    }

    /**
    Filter to pass strings to output, escaping <b>&lt;</b> and <b>&amp;</b>
    characters to &amp;lt; and &amp;amp; respectively.
     过滤以将字符串传递到输出，将<b> << / b>和<b>＆</ b>字符转义为＆lt;。和＆amp;分别。
    */
    private static String escape (char[] chars, int start, int length)
    {
        StringBuffer result = new StringBuffer ();
        for (int c = start; c < start + length; ++c)
            if (chars[c] == '<')
                result.append ("&lt;");
            else if (chars[c] == '&')
                result.append ("&amp;");
            else
                result.append (chars[c]);
                
        return result.toString ();
    }
    
    /**
    This whitespace string is expanded and collapsed to manage the output
    indenting.
     将该空白字符串扩展和折叠以管理输出缩进。
    */
    private String indent = "";

    /**
    A buffer for character data.  It is &quot;enabled&quot; in 
    {@link #startElement startElement} by being initialized to a 
    new <b>StringBuffer</b>, and then read and reset to 
    <code>null</code> in {@link #endElement endElement}.
     字符数据的缓冲区。通过初始化为新的<b> StringBuffer </ b>在{@link #startElement startElement}中“启用”，
     然后在{@link #endElement endElement}中读取并重置为<code> null </ code> 。
    */
    private StringBuffer currentValue = null;

    /**
    The primary buffer for accumulating the formatted XML.
     累积格式化的XML的主要缓冲区。
    */
    private StringBuffer output = new StringBuffer ();    
    
    private boolean justHitStartTag;
    
    private static final String standardIndent = "  ";
    private static final String endLine = 
        System.getProperty ("line.separator");
}

