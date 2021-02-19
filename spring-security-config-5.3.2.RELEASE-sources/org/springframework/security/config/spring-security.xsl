<?xml version="1.0" encoding="UTF-8"?>

<!--
    XSL to manipulate trang's output XSD file. Contributed by Brian Ewins.

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
    <xsl:output method="xml"  indent="yes"/>

    <xsl:variable name="elts-to-inline">
        <xsl:text>,access-denied-handler,anonymous,session-management,concurrency-control,after-invocation-provider,authentication-provider,ldap-authentication-provider,user,port-mapping,openid-login,expression-handler,form-login,http-basic,intercept-url,logout,password-encoder,port-mappings,port-mapper,password-compare,protect,protect-pointcut,pre-post-annotation-handling,pre-invocation-advice,post-invocation-advice,invocation-attribute-factory,remember-me,salt-source,x509,add-headers,</xsl:text>
    </xsl:variable>

    <xsl:template match="xs:element">
        <xsl:choose>
            <xsl:when test="contains($elts-to-inline, concat(',',substring-after(current()/@ref, ':'),','))">
                <xsl:variable name="node" select="."/>
                <xsl:for-each select="/xs:schema/xs:element[@name=substring-after(current()/@ref, ':')]">
                    <xsl:copy>
                        <xsl:apply-templates select="$node/@*[local-name() != 'ref']"/>
                        <xsl:apply-templates select="@*|*"/>
                    </xsl:copy>
                </xsl:for-each>
            </xsl:when>
            <!-- Ignore global elements which have been inlined -->
            <xsl:when test="contains($elts-to-inline, concat(',',@name,','))">
            </xsl:when>

            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@*|*"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Copy any non-element content -->
    <xsl:template match="text()|@*|*">
        <xsl:copy>
            <xsl:apply-templates select="text()|@*|*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="xs:documentation">
       <xsl:element name="xs:documentation">
         <xsl:copy-of select="@*" />
        <xsl:value-of select="replace(concat(normalize-space(text()),' '), '(.{0,90}) ','$1&#xA;                ')"/>
       </xsl:element>
    </xsl:template>
</xsl:stylesheet>
