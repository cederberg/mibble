<?xml version="1.0" encoding="ISO-8859-1" ?>

<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE stylesheet [
<!ENTITY newline "<xsl:text>
</xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- ### INPUT PARAMETERS ### -->
  <xsl:param name="date" select="'UNDEFINED'" />
  <xsl:param name="version" select="'UNDEFINED'" />


  <!-- ### OUTPUT DECLARATION ### -->
  <xsl:output method="text"
              encoding="ISO-8859-1" />

  <xsl:strip-space elements="doc head body h1 p list item title text" />


  <!-- ### DOCUMENT HEADER ### -->
  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="head">
    <xsl:variable name="text">
      <xsl:value-of select="translate(title,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
    </xsl:variable>
    <xsl:value-of select="$text" />
    &newline;
    <xsl:call-template name="replace-chars">
      <xsl:with-param name="str" select="$text" />
      <xsl:with-param name="char" select="'='" />
    </xsl:call-template>
    &newline;
  </xsl:template>


  <!-- ### DOCUMENT BODY ### -->
  <xsl:template match="body">
    <xsl:apply-templates />
    &newline;
    <xsl:text>_____________________________________________________________________

Mibble </xsl:text>
    <xsl:value-of select="$version" />
    <xsl:text> (</xsl:text>
    <xsl:value-of select="$date" />
    <xsl:text>). See http://www.mibble.org/ for more
information.

Copyright (c) 2002-2005 Per Cederberg. Permission is granted to copy
this document verbatim in any medium, provided that this copyright
notice is left intact.</xsl:text>
    &newline;
  </xsl:template>

  <xsl:template match="h1">
    <xsl:variable name="text">
      <xsl:apply-templates />
    </xsl:variable>
    &newline;
    <xsl:value-of select="$text" />
    &newline;
    <xsl:call-template name="replace-chars">
      <xsl:with-param name="str" select="$text" />
      <xsl:with-param name="char" select="'-'" />
    </xsl:call-template>
    &newline;&newline;
  </xsl:template>

  <xsl:template match="p">
    <xsl:variable name="text">
      <xsl:apply-templates />
    </xsl:variable>
    <xsl:call-template name="linebreakString">
      <xsl:with-param name="str" select="normalize-space($text)" />
      <xsl:with-param name="lineStart" select="'  '" />
      <xsl:with-param name="lineLength" select="70" />
    </xsl:call-template>
    &newline;&newline;
  </xsl:template>

  <xsl:template match="list">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="item">
    <xsl:text>    o </xsl:text>
    <xsl:apply-templates />
    &newline;&newline;
  </xsl:template>

  <xsl:template match="item/title">
    <xsl:apply-templates />
    &newline;
    <xsl:text>      </xsl:text>
  </xsl:template>

  <xsl:template match="item/text">
    <xsl:variable name="text">
      <xsl:apply-templates />
    </xsl:variable>
    <xsl:variable name="lines">
      <xsl:call-template name="linebreakString">
        <xsl:with-param name="str" select="normalize-space($text)" />
        <xsl:with-param name="lineStart" select="'      '" />
        <xsl:with-param name="lineLength" select="70" />
      </xsl:call-template>
    </xsl:variable>
    <xsl:value-of select="substring-after($lines,'      ')" />
  </xsl:template>

  <xsl:template match="code">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="ref">
    <xsl:variable name="text">
      <xsl:apply-templates />
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="@url != ''">
        <xsl:value-of select="$text" />
        <xsl:text> (</xsl:text>
        <xsl:value-of select="@url" />
        <xsl:text>)</xsl:text>
      </xsl:when>
      <xsl:when test="@file != ''">
        <xsl:value-of select="$text" />
        <xsl:text> (in </xsl:text>
        <xsl:value-of select="substring-before(@file,'.')" />
        <xsl:text>.txt)</xsl:text>
      </xsl:when>
      <xsl:when test="@bug != ''">
        <xsl:text>[Bug #</xsl:text>
        <xsl:value-of select="@bug" />
        <xsl:if test="string-length($text) &gt; 0">
          <xsl:text> - </xsl:text>
          <xsl:value-of select="$text" />
        </xsl:if>
        <xsl:text>]</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text" />
        <xsl:text> [UNDEFINED REFERENCE]</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="param">
    <xsl:choose>
      <xsl:when test="@name = 'date'">
        <xsl:value-of select="$date" />
      </xsl:when>
      <xsl:when test="@name = 'version'">
        <xsl:value-of select="$version" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>


  <!-- ### HELPER FUNCTIONS ### -->

  <xsl:template name="replace-chars">
    <xsl:param name="str" />
    <xsl:param name="char" />
    <xsl:if test="string-length($str) &gt; 0">
      <xsl:value-of select="$char" />
      <xsl:call-template name="replace-chars">
        <xsl:with-param name="str" select="substring($str,2)" />
        <xsl:with-param name="char" select="$char" />
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="linebreakString">
    <!-- Adapted from a function by Kevin Manley published in
         http://www-106.ibm.com/developerworks/xml/library/x-tiplnbrk.html -->
    <xsl:param name="str" />
    <xsl:param name="lineStart" />
    <xsl:param name="lineLength" />
    <xsl:variable name="maxStrLength"
                  select="$lineLength - string-length($lineStart)" />
    <xsl:choose>
      <xsl:when test="string-length($str) &lt;= $maxStrLength">
        <xsl:value-of select="$lineStart" />
        <xsl:value-of select="$str" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="strFirst">
          <xsl:call-template name="maxSubstringEndingWithBreak">
            <xsl:with-param name="str" select="substring($str,1,$maxStrLength)" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="strRest" select="substring-after($str,$strFirst)" />
        <xsl:value-of select="$lineStart" />
        <xsl:value-of select="$strFirst" />
        &newline;
        <xsl:call-template name="linebreakString">
          <xsl:with-param name="str" select="$strRest" />
          <xsl:with-param name="lineStart" select="$lineStart" />
          <xsl:with-param name="lineLength" select="$lineLength" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="maxSubstringEndingWithBreak">
    <!-- Slightly adapted from a function by Kevin Manley published in
         http://www-106.ibm.com/developerworks/xml/library/x-tiplnbrk.html -->
    <xsl:param name="str" />
    <xsl:variable name="len" select="string-length($str)" />
    <xsl:choose>
      <xsl:when test="len &lt;= 1 or substring($str,$len)=' ' or contains($str,' ')=false">
        <xsl:value-of select="$str" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="maxSubstringEndingWithBreak">
          <xsl:with-param name="str" select="substring($str, 1, $len - 1)" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
