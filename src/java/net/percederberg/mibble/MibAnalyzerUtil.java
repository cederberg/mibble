/*
 * MibAnalyzerUtil.java
 *
 * This work is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * Copyright (c) 2004-2007 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import net.percederberg.grammatica.parser.Node;
import net.percederberg.grammatica.parser.Production;
import net.percederberg.grammatica.parser.Token;
import net.percederberg.mibble.asn1.Asn1Constants;

/**
 * Helper and utility functions for the MIB file analyzer.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.9
 */
class MibAnalyzerUtil {

    /**
     * Checks if a node corresponds to a bit value. This method is
     * used to distinguish between bit values and object identifier
     * values during the analysis.
     *
     * @param node           the parse tree node to check
     *
     * @return true if the node contains a bit value, or
     *         false otherwise
     */
    static boolean isBitValue(Node node) {
        if (node.getId() == Asn1Constants.COMMA) {
            return true;
        } else if (node.getId() == Asn1Constants.NAME_VALUE_LIST
                && node.getChildCount() < 4) {

            return true;
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                if (isBitValue(node.getChildAt(i))) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Returns all the comments before the specified node. If
     * there are multiple comment lines possibly separated by
     * whitespace, they will be concatenated into one string.
     *
     * @param node          the production or token node
     *
     * @return the comment string, or
     *         null if no comments were found
     */
    static String getCommentsBefore(Node node) {
        Token   token = getFirstToken(node);
        String  comment = "";

        if (token != null) {
            token = token.getPreviousToken();
        }
        while (token != null) {
            if (token.getId() == Asn1Constants.WHITESPACE) {
                comment = getLineBreaks(token.getImage()) + comment;
            } else if (token.getId() == Asn1Constants.COMMENT) {
                comment = token.getImage().substring(2).trim() + comment;
            } else {
                break;
            }
            token = token.getPreviousToken();
        }
        comment = comment.trim();
        return comment.length() <= 0 ? null : comment;
    }

    /**
     * Returns all the comments after the specified node. If
     * there are multiple comment lines possibly separated by
     * whitespace, they will be concatenated into one string.
     *
     * @param node           the production or token node
     *
     * @return the comment string, or
     *         null if no comments were found
     */
    static String getCommentsAfter(Node node) {
        Token   token = getLastToken(node);
        String  comment = "";

        if (token != null) {
            token = token.getNextToken();
        }
        while (token != null) {
            if (token.getId() == Asn1Constants.WHITESPACE) {
                comment += getLineBreaks(token.getImage());
            } else if (token.getId() == Asn1Constants.COMMENT) {
                comment += token.getImage().substring(2).trim();
            } else {
                break;
            }
            token = token.getNextToken();
        }
        comment = comment.trim();
        return comment.length() <= 0 ? null : comment;
    }

    /**
     * Returns the first comment token on the same line.
     *
     * @param node           the production node
     *
     * @return the first comment token on the same line
     */
    static Token getCommentTokenSameLine(Node node) {
        Token  last = getLastToken(node);
        Token  token;

        if (last == null) {
            return null;
        }
        token = last.getNextToken();
        while (token != null) {
            switch (token.getId()) {
            case Asn1Constants.WHITESPACE:
            case Asn1Constants.COMMA:
                // Skip to next
                break;
            case Asn1Constants.COMMENT:
                if (last.getEndLine() == token.getStartLine()) {
                    return token;
                } else {
                    return null;
                }
            default:
                return null;
            }
            token = token.getNextToken();
        }
        return null;
    }

    /**
     * Returns the first token in a production.
     *
     * @param node           the production or token node
     *
     * @return the first token in the production
     */
    private static Token getFirstToken(Node node) {
        if (node instanceof Production) {
            return getFirstToken(node.getChildAt(0));
        } else if (node instanceof Token) {
            return (Token) node;
        } else {
            return null;
        }
    }

    /**
     * Returns the last token in a production.
     *
     * @param node           the production or token node
     *
     * @return the last token in the production
     */
    private static Token getLastToken(Node node) {
        if (node instanceof Production) {
            return getLastToken(node.getChildAt(node.getChildCount() - 1));
        } else if (node instanceof Token) {
            return (Token) node;
        } else {
            return null;
        }
    }

    /**
     * Returns a string containing the line breaks of an input
     * string.
     *
     * @param str            the input string
     *
     * @return a string containing zero or more line breaks
     */
    private static String getLineBreaks(String str) {
        StringBuffer  res = new StringBuffer();

        if (str == null) {
            return null;
        }
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\n') {
                res.append('\n');
            }
        }
        return res.toString();
    }
}
