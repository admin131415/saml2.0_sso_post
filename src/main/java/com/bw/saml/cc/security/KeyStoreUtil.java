package com.bw.saml.cc.security;

import java.io.*;
import java.security.*;
import java.util.*;

/**
Simple utility for managing common KeyStore tasks.
 用于管理常见KeyStore任务的简单实用程序。
@author Will Provost
*/
/*
Copyright 2006 Will Provost.
All rights reserved by Capstone Courseware, LLC.
*/
public class KeyStoreUtil
{
    /**
    Get a KeyStore object given the keystore filename and password.
     给定密钥库文件名和密码，获取一个密钥库对象。
    */
    public static KeyStore getKeyStore (String filename, String password)
        throws KeyStoreException
    {
        KeyStore result = KeyStore.getInstance (KeyStore.getDefaultType ());
        
        try
        {
            FileInputStream in = new FileInputStream (filename);
            result.load (in, password.toCharArray ());
            in.close ();
        }
        catch (Exception ex)
        {
            System.out.println ("Failed to read keystore:");
            ex.printStackTrace ();
        }
        
        return result;
    }
    
    /**
    Get a KeyStore object given the keystore filename and password.
     给定密钥库文件名和密码，获取一个密钥库对象。
    */
    public static KeyStore getKeyStore (InputStream in, String password)
        throws KeyStoreException
    {
        KeyStore result = KeyStore.getInstance (KeyStore.getDefaultType ());
        
        try
        {
            result.load (in, password.toCharArray ());
        }
        catch (Exception ex)
        {
            System.out.println ("Failed to read keystore:");
            ex.printStackTrace ();
        }
        
        return result;
    }
    
    /**
    List all the key and certificate aliases in the keystore.
     列出密钥库中的所有密钥和证书别名。
    
    @return A list of Strings
    */
    public static List getAliases (KeyStore keystore)
        throws KeyStoreException
    {
        return Collections.list (keystore.aliases ());
    }
    
    /**
    Get a private key from the keystore by name and password.
     通过名称和密码从密钥库中获取私钥。
    */
    public static Key getKey (KeyStore keystore, String alias, String password)
        throws GeneralSecurityException
    {
        return keystore.getKey (alias, password.toCharArray ());
    }
    
    /**
    Get a certificate from the keystore by name.
     通过名称从密钥库获取证书。
    */
    public static java.security.cert.Certificate getCertificate 
            (KeyStore keystore, String alias)
        throws GeneralSecurityException
    {
        return keystore.getCertificate (alias);
    }
    
    /**
    Dump all data about the private key to the console.
     将有关私钥的所有数据转储到控制台。
    */
    public static String spillBeans (Key key)
    {
        StringBuffer buffer = new StringBuffer 
            ("Algorithm: " + key.getAlgorithm () + endLine +
             "Key value: " + endLine);

        appendHexValue (buffer, key.getEncoded ());

        return buffer.toString ();
    }
    
    /**
    Dump all data about the certificate to the console.
     将有关证书的所有数据转储到控制台。
    */
    public static String spillBeans (java.security.cert.Certificate cert)
        throws GeneralSecurityException
    {
        StringBuffer buffer = new StringBuffer 
            ("Certificate type: " + cert.getType () + endLine +
             "Encoded data: " + endLine);
        appendHexValue (buffer, cert.getEncoded ());

        return buffer.toString ();
    }
    
    /**
    Helper method to solicit a line of user input from the console.
     从控制台请求用户输入的帮助程序方法。
    */
    public static String getUserInput (String prompt)
        throws IOException
    {
        System.out.print (prompt);
        BufferedReader reader = new BufferedReader
            (new InputStreamReader (System.in));
        String result = reader.readLine ();
        
        return result;
    }
    
    /**
    Helper method that converts a single byte to a hex string representation.
     将单个字节转换为十六进制字符串表示形式的Helper方法。
    @param b byte Byte to convert
    @return StringBuffer with the two-digit hex string
    */
    public static void appendHexValue (StringBuffer buffer, byte b)
    {
        int[] digits = { (b >>> 4) & 0x0F, b & 0x0F };
        for (int d = 0; d < digits.length; ++d)
        {
            int increment = (int) ((digits[d] < 10) ? '0' : ('a' - 10));
            buffer.append ((char) (digits[d] + increment));
        }
    }
    
    /**
    Helper that appends a hex representation of a byte array to an
    existing StringBuffer.
     将字节数组的十六进制表示形式附加到现有StringBuffer的助手。
    */
    public static void appendHexValue (StringBuffer buffer, byte[] bytes)
    {
        for (int i = 0; i < bytes.length; ++i)
            appendHexValue (buffer, bytes[i]);
    }

    private static final String endLine = 
        System.getProperty ("line.separator");
        
    /**
    As an application, this class will operate something like the
    <b>keytool -list</b> command: it will read out every alias in the
    keystore, and with the user's provided password for a given key
    will write out all data on the key and/or certificate.
     作为应用程序，此类将像<b> keytool -list </ b>命令一样操作：
     它将读取密钥库中的每个别名，并使用用户提供的给定密钥的密码将密钥库中的所有数据写出。
     密钥和/或证书。
    */
    public static void main (String[] args)
        throws Exception
    {
        if (args.length < 2)
        {
            System.out.println 
                ("Usage: java cc.security.KeyStoreUtil <filename> <password>");
            System.exit (-1);
        }
        
        String filename = args[0];
        String password = args[1];
        
        KeyStore keystore = getKeyStore (filename, password);
        Iterator each = getAliases (keystore).iterator ();
        while (each.hasNext ())
            try
            {
                String alias = (String) each.next ();
                System.out.println ("Key or certificate alias: " + alias);
                
                Key key = getKey 
                    (keystore, alias, getUserInput ("Key password: "));
                System.out.println (spillBeans (key));
                
                java.security.cert.Certificate certificate = 
                    getCertificate (keystore, alias);
                System.out.println (spillBeans (certificate));
                System.out.println ();
            }
            catch (Exception ex)
            {
                System.out.println ("Couldn't read key.");
            }
    }
}

