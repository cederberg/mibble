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
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.util.HashSet;

import net.percederberg.grammatica.parser.Node;
import net.percederberg.grammatica.parser.Production;
import net.percederberg.grammatica.parser.Token;
import net.percederberg.mibble.asn1.Asn1Constants;

/**
 * Helper and utility functions for the MIB file analyzer.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.9
 */
class MibAnalyzerUtil {

    /**
     * An internal hash set containing all the used comment tokens.
     * When a comment string is returned by the getComments() method,
     * the corresponding tokens will be added to this set and not
     * returned on subsequent calls.
     */
    private static HashSet<Token> commentTokens = new HashSet<Token>();

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
     * Returns the MIB file reference for a production node.
     *
     * @param file           the MIB file
     * @param node           the production or token node
     *
     * @return the MIB file reference
     */
    static MibFileRef getFileRef(File file, Node node) {
        MibFileRef ref = new MibFileRef(file,
                                        node.getStartLine(),
                                        node.getStartColumn());
        Token comment = findCommentTokenBefore(node);
        if (comment != null) {
            ref.lineCommentStart = comment.getStartLine();
        }
        ref.lineEnd = node.getEndLine();
        return ref;
    }

    /**
     * Returns a string containing the raw input text for a node.
     * This is created by concatenating the sequence of tokens that
     * makes up the production.
     *
     * @param node           the production or token node
     *
     * @return the input string
     *
     * @since 2.10
     */
    static String getText(Node node) {
        StringBuilder buffer = new StringBuilder();
        Token token = findCommentTokenBefore(node);
        if (token == null) {
            token = findFirstToken(node);
        }
        Token lastToken = findLastToken(node);
        while (token != null && token != lastToken) {
            buffer.append(token.getImage());
            token = token.getNextToken();
        }
        if (lastToken != null) {
            buffer.append(lastToken.getImage());
        }
        return buffer.toString();
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
        String comment = "";
        String str = processComments(findCommentTokenBefore(node));
        if (str != null) {
            comment = str;
        }
        str = processCommentsInside(node);
        if (str != null) {
            if (comment.length() > 0) {
                comment += "\n\n";
            }
            comment += str;
        }
        str = processComments(findCommentTokenAfter(node, true));
        if (str != null) {
            if (comment.length() > 0) {
                comment += "\n\n";
            }
            comment += str;
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
        String comment = processComments(findCommentTokenAfter(node, false));
        commentTokens.clear();
        return comment;
    }

    /**
     * Reads comment tokens (and whitespace). Reading is stopped on
     * the first non-comment or whitespace token, or if a token is
     * marked as already consumed.
     *
     * @param token          the starting comment token
     *
     * @return the comment text (without '--' prefixes), or
     *         null if no comment text remained after trimming
     */
    private static String processComments(Token token) {
        StringBuilder buffer = new StringBuilder();
        while (token != null && !commentTokens.contains(token)) {
            if (token.getId() == Asn1Constants.COMMENT) {
                commentTokens.add(token);
                buffer.append(token.getImage().substring(2).trim());
            } else if (token.getId() == Asn1Constants.WHITESPACE) {
                buffer.append(getLineBreaks(token.getImage()));
            } else {
                break;
            }
            token = token.getNextToken();
        }
        String res = buffer.toString().trim();
        return res.length() <= 0 ? null : res;
    }

    /**
     * Reads all unmarked comment tokens inside the specified node.
     * Note that only comment tokens not present in the token cache
     * will be returned by this method.
     *
     * @param node           the production or token node
     *
     * @return the comment text (without '--' prefixes), or
     *         null if no comments were found
     */
    private static String processCommentsInside(Node node) {
        StringBuilder buffer = new StringBuilder();
        Token token = findFirstToken(node);
        Token last = findLastToken(node);
        while (token != null && token != last) {
            if (token.getId() == Asn1Constants.COMMENT &&
                !commentTokens.contains(token)) {

                commentTokens.add(token);
                buffer.append(token.getImage().substring(2).trim());
                buffer.append("\n");
            }
            token = token.getNextToken();
        }
        String res = buffer.toString().trim();
        return res.length() <= 0 ? null : res;
    }

    /**
     * Returns the first token in a production.
     *
     * @param node           the production or token node
     *
     * @return the first token in the production, or
     *         null if none was found
     */
    private static Token findFirstToken(Node node) {
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
    private static Token findLastToken(Node node) {
        while (node instanceof Production) {
            node = node.getChildAt(node.getChildCount() - 1);
        }
        return (Token) node;
    }

    /**
     * Returns the first comment token before the specified node.
     *
     * @param node           the production node
     *
     * @return the first comment token found, or
     *         null if no comment token found
     */
    private static Token findCommentTokenBefore(Node node) {
        Token comment = null;
        Token token = findFirstToken(node);
        if (token == null) {
            return null;
        }
        token = token.getPreviousToken();
        while (token != null && !commentTokens.contains(token)) {
            if (token.getId() == Asn1Constants.COMMENT) {
                comment = token;
            } else if (token.getId() == Asn1Constants.WHITESPACE) {
                // Ignore token, check next
            } else {
                break;
            }
            token = token.getPreviousToken();
        }
        return comment;
    }


    /**
     * Returns the first comment token after the specified node.
     * Optionally, only tokens on the same line are checked.
     *
     * @param node           the production node
     * @param sameline       the same line number flag
     *
     * @return the first comment token found, or
     *         null if no comment token found
     */
    private static Token findCommentTokenAfter(Node node, boolean sameline) {
        Token token = findLastToken(node);
        if (token == null) {
            return null;
        }
        int lineNo = token.getEndLine();
        token = token.getNextToken();
        while (token != null && !commentTokens.contains(token)) {
            if (sameline && lineNo != token.getStartLine()) {
                return null;
            } else if (token.getId() == Asn1Constants.COMMENT) {
                return token;
            } else if (token.getId() == Asn1Constants.WHITESPACE ||
                       token.getId() == Asn1Constants.COMMA) {
                // Ignore token, check next
            } else {
                return null;
            }
            token = token.getNextToken();
        }
        return null;
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
        if (str == null) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\n') {
                res.append('\n');
            }
        }
        return res.toString();
    }
}
