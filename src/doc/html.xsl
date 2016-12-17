<?xml version="1.0" encoding="UTF-8" ?>

<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE stylesheet [
<!ENTITY newline "<xsl:text>
</xsl:text>">
<!ENTITY indent "<xsl:text>  </xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- ### INPUT PARAMETERS ### -->
  <xsl:param name="year" select="'2011'" />
  <xsl:param name="date" select="'UNDEFINED'" />
  <xsl:param name="version" select="'UNDEFINED'" />
  <xsl:param name="style" select="''" />


  <!-- ### OUTPUT DECLARATION ### -->
  <xsl:output method="xml"
              version="1.0"
              encoding="UTF-8"
              doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
              doctype-system="DTD/xhtml1-strict.dtd" />


  <!-- ### DOCUMENT HEADER ### -->
  <xsl:template match="/">
    &newline;
    <xsl:comment> This file was automatically generated. DO NOT EDIT! </xsl:comment>
    &newline;&newline;
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <xsl:apply-templates />
    </html>
    &newline;
  </xsl:template>

  <xsl:template match="head">
    <head>
      &newline;&indent;&indent;
      <meta charset="utf-8" />
      &newline;&indent;&indent;
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      &newline;&indent;&indent;
      <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/normalize/5.0.0/normalize.min.css" />
      &newline;&indent;&indent;
      <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,700" />
      &newline;&indent;&indent;
      <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css" />
      &newline;&indent;&indent;
      <link rel="stylesheet" href="{$style}" />
      &newline;&indent;&indent;
      <title><xsl:value-of select="title" /></title>
      &newline;&indent;
    </head>
  </xsl:template>


  <!-- ### DOCUMENT BODY ### -->
  <xsl:template match="body">
    &newline;&indent;
    <body>
      &newline;&indent;&indent;
      <section class="header">
        &newline;&indent;&indent;&indent;
        <div class="banner">
          &newline;&indent;&indent;&indent;&indent;
          <header class="box">
            &newline;&indent;&indent;&indent;&indent;&indent;
            <h1 class="name"><xsl:value-of select="/doc/head/title" /></h1>
            &newline;&indent;&indent;&indent;&indent;&indent;
            <p class="tagline">
              <xsl:text>Mibble version </xsl:text>
              <xsl:value-of select="$version" />
              <xsl:text> (</xsl:text>
              <xsl:value-of select="$date" />
              <xsl:text>)</xsl:text>
            </p>
          &newline;&indent;&indent;&indent;&indent;
          </header>
        &newline;&indent;&indent;&indent;
        </div>
      &newline;&indent;&indent;
      </section>
      &newline;&indent;&indent;
      <section class="content">
        <xsl:apply-templates />
        &newline;&indent;&indent;&indent;
        <hr/>
        &newline;&newline;&indent;&indent;&indent;
        <p class="small">
          <xsl:text>Mibble </xsl:text>
          <xsl:value-of select="$version" />
          <xsl:text> (</xsl:text>
          <xsl:value-of select="$date" />
          <xsl:text>). See </xsl:text>
          <a href="https://www.mibble.org/">www.mibble.org</a>
          <xsl:text> for more information.</xsl:text>
        </p>
        &newline;&newline;&indent;&indent;&indent;
        <p class="small">
          <xsl:text disable-output-escaping="yes">Copyright &amp;copy; 2002-</xsl:text>
          <xsl:value-of select="$year" />
          <xsl:text disable-output-escaping="yes"> Per Cederberg. Permission
      is granted to copy this document verbatim in any medium, provided
      that this copyright notice is left intact.</xsl:text>
        </p>
      &newline;&newline;&indent;&indent;
      </section>
    &newline;&indent;
    </body>
  </xsl:template>

  <xsl:template match="h1">
    <h2><xsl:apply-templates /></h2>
  </xsl:template>

  <xsl:template match="p">
    <p><xsl:apply-templates /></p>
  </xsl:template>

  <xsl:template match="pre">
    <pre><xsl:apply-templates /></pre>
  </xsl:template>

  <xsl:template match="list">
    <ul class="li-fa-chevron li-margin-md">
      <xsl:apply-templates />
    </ul>
  </xsl:template>

  <xsl:template match="item">
    <li>
      <xsl:apply-templates />
    </li>
  </xsl:template>

  <xsl:template match="item/title">
    <b><xsl:apply-templates /></b>
    <br/>
  </xsl:template>

  <xsl:template match="item/text">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="code">
    <code><xsl:apply-templates /></code>
  </xsl:template>

  <xsl:template match="ref">
    <xsl:variable name="text">
      <xsl:apply-templates />
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="@url != ''">
        <a href="{@url}">
          <xsl:value-of select="$text" />
        </a>
      </xsl:when>
      <xsl:when test="@file != ''">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="substring-before(@file,'.')" />
            <xsl:text>.html</xsl:text>
          </xsl:attribute>
          <xsl:value-of select="$text" />
        </a>
      </xsl:when>
      <xsl:when test="@bug != ''">
        <a href="https://savannah.nongnu.org/bugs/index.php?func=detailitem&amp;item_id={@bug}">
          <xsl:text>Bug #</xsl:text>
          <xsl:value-of select="@bug" />
          <xsl:if test="string-length($text) &gt; 0">
            <xsl:text> - </xsl:text>
            <xsl:value-of select="$text" />
          </xsl:if>
        </a>
      </xsl:when>
      <xsl:when test="@issue != ''">
        <a href="https://github.com/cederberg/mibble/issues/{@issue}">
          <xsl:text>Issue #</xsl:text>
          <xsl:value-of select="@issue" />
          <xsl:if test="string-length($text) &gt; 0">
            <xsl:text> - </xsl:text>
            <xsl:value-of select="$text" />
          </xsl:if>
        </a>
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

</xsl:stylesheet>
