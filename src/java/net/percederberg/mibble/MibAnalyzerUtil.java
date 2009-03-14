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

import java.util.ArrayList;
import java.util.HashMap;

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
     * An internal hash map containing all the used comment tokens.
     * When a comment string is returned by the getComments() method,
     * the corresponding tokens will be added to this hash map and
     * not returned on subsequent calls.
     */
    private static HashMap commentTokens = new HashMap();

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
     * Returns all the comments associated with a specified node. If
     * there are multiple comment lines, these will be concatenated
     * into a single string. This method handles comments before,
     * inside and after (starting on the same line) as the specified
     * node. It also updates the comment token cache to avoid
     * returning the same comments twice.
     *
     * @param node           the production or token node
     *
     * @return the comment string, or
     *         null if no comments were found
     */
    static String getComments(Node node) {
        String  comment = "";
        String  str;
        Token   token;

        str = getCommentsBefore(node);
        if (str != null) {
            comment = str;
        }
        str = getCommentsInside(node);
        if (str != null) {
            if (comment.length() > 0) {
                comment += "\n\n";
            }
            comment += str;
        }
        token = getCommentTokenSameLine(node);
        if (token != null) {
            if (comment.length() > 0) {
                comment += "\n\n";
            }
            token = token.getPreviousToken();
            comment += getCommentsAfter(token);
        }
        return comment.length() <= 0 ? null : comment;
    }

    /**
     * Returns all the footer comments after the specified node. This
     * method also clears the comment cache and should be called
     * exactly once after each MIB file parsed, in order to free
     * memory used by the comment token cache.
     *
     * @param node           the production or token node
     *
     * @return the comment string, or
     *         null if no comments were found
     */
    static String getCommentsFooter(Node node) {
        String  comment = getCommentsAfter(node);

        commentTokens.clear();
        return comment;
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
    private static String getCommentsBefore(Node node) {
        Token         token = getFirstToken(node);
        ArrayList     comments = new ArrayList();
        StringBuffer  buffer = new StringBuffer();
        String        res = "";

        if (token != null) {
            token = token.getPreviousToken();
        }
        while (token != null) {
            if (token.getId() == Asn1Constants.WHITESPACE) {
                comments.add(getLineBreaks(token.getImage()));
            } else if (token.getId() == Asn1Constants.COMMENT &&
                       !commentTokens.containsKey(token)) {

                commentTokens.put(token, null);
                comments.add(token.getImage().substring(2).trim());
            } else {
                break;
            }
            token = token.getPreviousToken();
        }
        for (int i = comments.size() - 1; i >= 0; i--) {
            buffer.append(comments.get(i));
        }
        res = buffer.toString().trim();
        return res.length() <= 0 ? null : res;
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
    private static String getCommentsAfter(Node node) {
        Token         token = getLastToken(node);
        StringBuffer  comment = new StringBuffer();
        String        res;

        if (token != null) {
            token = token.getNextToken();
        }
        while (token != null) {
            if (token.getId() == Asn1Constants.WHITESPACE) {
                comment.append(getLineBreaks(token.getImage()));
            } else if (token.getId() == Asn1Constants.COMMENT &&
                       !commentTokens.containsKey(token)) {

                commentTokens.put(token, null);
                comment.append(token.getImage().substring(2).trim());
            } else {
                break;
            }
            token = token.getNextToken();
        }
        res = comment.toString().trim();
        return res.length() <= 0 ? null : res;
    }

    /**
     * Returns all the unhandled comments inside the specified node.
     * Note that only comment tokens not present in the token cache
     * will be returned by this method.
     *
     * @param node           the production or token node
     *
     * @return the comment string, or
     *         null if no comments were found
     */
    private static String getCommentsInside(Node node) {
        Token         token = getFirstToken(node);
        Token         last = getLastToken(node);
        StringBuffer  comment = new StringBuffer();
        String        res;

        while (token != null && token != last) {
            if (token.getId() == Asn1Constants.COMMENT &&
                !commentTokens.containsKey(token)) {

                commentTokens.put(token, null);
                comment.append(token.getImage().substring(2).trim());
                comment.append("\n");
            }
            token = token.getNextToken();
        }
        res = comment.toString().trim();
        return res.length() <= 0 ? null : res;
    }

    /**
     * Returns the first comment token on the same line.
     *
     * @param node           the production node
     *
     * @return the first comment token on the same line
     */
    private static Token getCommentTokenSameLine(Node node) {
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
     * @return the first token in the production, or
     *         null if none was found
     */
    private static Token getFirstToken(Node node) {
        while (node instanceof Production) {
            node = node.getChildAt(0);
        }
        return (Token) node;
    }

    /**
     * Returns the last token in a production.
     *
     * @param node           the production or token node
     *
     * @return the last token in the production, or
     *         null if none was found
     */
    private static Token getLastToken(Node node) {
        while (node instanceof Production) {
            node = node.getChildAt(node.getChildCount() - 1);
        }
        return (Token) node;
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
