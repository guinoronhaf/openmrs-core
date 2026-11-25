/*
 * Copyright (c) 2006, Stephen Kelvin Friedrich,  All rights reserved.
 *
 * This a BSD license. If you use or enhance the code, I'd be pleased if you sent a mail to s.friedrich@eekboom.com
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *       following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
         following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the "Stephen Kelvin Friedrich" nor the names of its contributors may be used to endorse
 *       or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.openmrs.util;

import java.text.Collator;
import java.util.Comparator;

/**
 * Utility class for natural String comparison operations
 * 
 * @since 1.5
 */
public final class NaturalStrings {
	
	/**
	 * This is a utility class (static methods only), don't instantiate.
	 */
	private NaturalStrings() {
	}
	
	/**
	 * Returns a comparator that compares contained numbers based on their numeric values and
	 * compares other parts using the current locale's order rules.
	 * <p>
	 * For example in German locale this will be a comparator that handles umlauts correctly and
	 * ignores upper/lower case differences.
	 * </p>
	 * 
	 * @return <p>
	 *         A string comparator that uses the current locale's order rules and handles embedded
	 *         numbers correctly.
	 *         </p>
	 * @see #getNaturalComparator(java.text.Collator)
	 */
	public static Comparator<String> getNaturalComparator() {
		Collator collator = Collator.getInstance();
		return getNaturalComparator(collator);
	}
	
	/**
	 * Returns a comparator that compares contained numbers based on their numeric values and
	 * compares other parts using the given collator.
	 * 
	 * @param collator used for locale specific comparison of text (non-number) subwords - must not
	 *            be null
	 * @return <p>
	 *         A string comparator that uses the given Collator to compare subwords and handles
	 *         embedded numbers correctly.
	 *         </p>
	 * @see #getNaturalComparator()
	 */
	public static Comparator<String> getNaturalComparator(final Collator collator) {
		if (collator == null) {
			// it's important to explicitly handle this here - else the bug will manifest anytime later in possibly
			// unrelated code that tries to use the comparator
			throw new NullPointerException("collator must not be null");
		}
		return (o1, o2) -> compareNatural(collator, o1, o2);
	}
	
	/**
	 * Returns a comparator that compares contained numbers based on their numeric values and
	 * compares other parts based on each character's Unicode value.
	 * 
	 * @return <p>
	 *         a string comparator that does case-sensitive comparisons on pure ascii strings and
	 *         handles embedded numbers correctly.
	 *         </p>
	 *         <b>Do not use</b> if your app might ever run on any locale that uses more than 7-bit
	 *         ascii characters.
	 * @see #getNaturalComparator()
	 * @see #getNaturalComparator(java.text.Collator)
	 */
	public static Comparator<String> getNaturalComparatorAscii() {
		return NaturalStrings::compareNaturalAscii;
	}
	
	/**
	 * Returns a comparator that compares contained numbers based on their numeric values and
	 * compares other parts based on each character's Unicode value while ignore upper/lower case
	 * differences. <b>Do not use</b> if your app might ever run on any locale that uses more than
	 * 7-bit ascii characters.
	 * 
	 * @return <p>
	 *         a string comparator that does case insensitive comparisons on pure ascii strings and
	 *         handles embedded numbers correctly.
	 *         </p>
	 * @see #getNaturalComparator()
	 * @see #getNaturalComparator(java.text.Collator)
	 */
	public static Comparator<String> getNaturalComparatorIgnoreCaseAscii() {
		return NaturalStrings::compareNaturalIgnoreCaseAscii;
	}
	
	/**
	 * <p>
	 * Compares two strings using the current locale's rules and comparing contained numbers based
	 * on their numeric values.
	 * </p>
	 * <p>
	 * This is probably the best default comparison to use.
	 * </p>
	 * <p>
	 * If you know that the texts to be compared are in a certain language that differs from the
	 * default locale's language, then get a collator for the desired locale (
	 * {@link java.text.Collator#getInstance(java.util.Locale)}) and pass it to
	 * {@link #compareNatural(java.text.Collator, String, String)}
	 * </p>
	 * 
	 * @param s first string
	 * @param t second string
	 * @return zero iff <code>s</code> and <code>t</code> are equal, a value less than zero iff
	 *         <code>s</code> lexicographically precedes <code>t</code> and a value larger than zero
	 *         iff <code>s</code> lexicographically follows <code>t</code>
	 */
	public static int compareNatural(String s, String t) {
		return compareNatural(s, t, false, Collator.getInstance());
	}
	
	/**
	 * <p>
	 * Compares two strings using the given collator and comparing contained numbers based on their
	 * numeric values.
	 * </p>
	 * 
	 * @param s first string
	 * @param t second string
	 * @return zero iff <code>s</code> and <code>t</code> are equal, a value less than zero iff
	 *         <code>s</code> lexicographically precedes <code>t</code> and a value larger than zero
	 *         iff <code>s</code> lexicographically follows <code>t</code>
	 */
	public static int compareNatural(Collator collator, String s, String t) {
		return compareNatural(s, t, true, collator);
	}
	
	/**
	 * <p>
	 * Compares two strings using each character's Unicode value for non-digit characters and the
	 * numeric values off any contained numbers.
	 * </p>
	 * <p>
	 * (This will probably make sense only for strings containing 7-bit ascii characters only.)
	 * </p>
	 * 
	 * @return zero iff <code>s</code> and <code>t</code> are equal, a value less than zero iff
	 *         <code>s</code> lexicographically precedes <code>t</code> and a value larger than zero
	 *         iff <code>s</code> lexicographically follows <code>t</code>
	 */
	public static int compareNaturalAscii(String s, String t) {
		return compareNatural(s, t, true, null);
	}
	
	/**
	 * <p>
	 * Compares two strings using each character's Unicode value - ignoring upper/lower case - for
	 * non-digit characters and the numeric values of any contained numbers.
	 * </p>
	 * <p>
	 * (This will probably make sense only for strings containing 7-bit ascii characters only.)
	 * </p>
	 * 
	 * @return zero iff <code>s</code> and <code>t</code> are equal, a value less than zero iff
	 *         <code>s</code> lexicographically precedes <code>t</code> and a value larger than zero
	 *         iff <code>s</code> lexicographically follows <code>t</code>
	 */
	public static int compareNaturalIgnoreCaseAscii(String s, String t) {
		return compareNatural(s, t, false, null);
	}
	
	/**
	 * @param s first string
	 * @param t second string
	 * @param caseSensitive treat characters differing in case only as equal - will be ignored if a
	 *            collator is given
	 * @param collator used to compare subwords that aren't numbers - if null, characters will be
	 *            compared individually based on their Unicode value
	 * @return zero iff <code>s</code> and <code>t</code> are equal, a value less than zero iff
	 *         <code>s</code> lexicographically precedes <code>t</code> and a value larger than zero
	 *         iff <code>s</code> lexicographically follows <code>t</code>
	 */
	private static int compareNatural(String s, String t, boolean caseSensitive, Collator collator) {
		Cursor cursor = new Cursor();
		
		int sLength = s.length();
		int tLength = t.length();
		
		while (true) {
			// both character indices are after a subword (or at zero)
			
			// Check if one string is at end
			if (cursor.sIndex == sLength && cursor.tIndex == tLength) {
				return 0;
			}
			if (cursor.sIndex == sLength) {
				return -1;
			}
			if (cursor.tIndex == tLength) {
				return 1;
			}
			
			// Compare sub word
			boolean sCharIsDigit = Character.isDigit(s.charAt(cursor.sIndex));
			boolean tCharIsDigit = Character.isDigit(t.charAt(cursor.tIndex));
			
			if (sCharIsDigit && tCharIsDigit) {
				// Compare numbers
				int result = compareNumberSubwords(s, t, cursor, sLength, tLength);
				if (result != 0) {
					return result;
				}
			} else {
				// Compare words
				int result = compareTextSubwords(s, t, cursor, sLength, tLength, caseSensitive, collator);
				if (result != 0) {
					return result;
				}
			}
		}
	}

	/**
	 * Compares the number subwords at the cursor position. Returns 0 if the subwords are equal
	 * (advancing the cursor past them) or a non-zero comparison result.
	 * 
	 * @param s first string
	 * @param t second string
	 * @param cursor current position in both strings, advanced past the compared number subwords
	 * @param sLength length of the first string
	 * @param tLength length of the second string
	 * @return zero iff the number subwords are equal, a non-zero comparison result otherwise
	 */
	private static int compareNumberSubwords(String s, String t, Cursor cursor, int sLength, int tLength) {
		char sChar = s.charAt(cursor.sIndex);
		char tChar = t.charAt(cursor.tIndex);
		
		// skip leading 0s
		int sLeadingZeroCount = 0;
		while (sChar == '0') {
			++sLeadingZeroCount;
			++cursor.sIndex;
			if (cursor.sIndex == sLength) {
				break;
			}
			sChar = s.charAt(cursor.sIndex);
		}
		int tLeadingZeroCount = 0;
		while (tChar == '0') {
			++tLeadingZeroCount;
			++cursor.tIndex;
			if (cursor.tIndex == tLength) {
				break;
			}
			tChar = t.charAt(cursor.tIndex);
		}
		boolean sAllZero = cursor.sIndex == sLength || !Character.isDigit(sChar);
		boolean tAllZero = cursor.tIndex == tLength || !Character.isDigit(tChar);
		if (sAllZero && tAllZero) {
			return 0;
		}
		if (sAllZero && !tAllZero) {
			return -1;
		}
		if (tAllZero) {
			return 1;
		}
		
		int diff = 0;
		do {
			if (diff == 0) {
				diff = sChar - tChar;
			}
			++cursor.sIndex;
			++cursor.tIndex;
			if (cursor.sIndex == sLength && cursor.tIndex == tLength) {
				return diff != 0 ? diff : sLeadingZeroCount - tLeadingZeroCount;
			}
			if (cursor.sIndex == sLength) {
				if (diff == 0) {
					return -1;
				}
				return Character.isDigit(t.charAt(cursor.tIndex)) ? -1 : diff;
			}
			if (cursor.tIndex == tLength) {
				if (diff == 0) {
					return 1;
				}
				return Character.isDigit(s.charAt(cursor.sIndex)) ? 1 : diff;
			}
			sChar = s.charAt(cursor.sIndex);
			tChar = t.charAt(cursor.tIndex);
			boolean sCharIsDigit = Character.isDigit(sChar);
			boolean tCharIsDigit = Character.isDigit(tChar);
			if (!sCharIsDigit && !tCharIsDigit) {
				// both number sub words have the same length
				if (diff != 0) {
					return diff;
				}
				return 0;
			}
			if (!sCharIsDigit) {
				return -1;
			}
			if (!tCharIsDigit) {
				return 1;
			}
		} while (true);
	}

	/**
	 * Compares the non-number subwords at the cursor position. Returns 0 if the subwords are equal
	 * (advancing the cursor past them) or a non-zero comparison result.
	 * 
	 * @param s first string
	 * @param t second string
	 * @param cursor current position in both strings, advanced past the compared subwords
	 * @param sLength length of the first string
	 * @param tLength length of the second string
	 * @param caseSensitive treat characters differing in case only as equal - ignored if a collator
	 *            is given
	 * @param collator used to compare subwords that aren't numbers - if null, characters will be
	 *            compared individually based on their Unicode value
	 * @return zero iff the subwords are equal, a non-zero comparison result otherwise
	 */
	private static int compareTextSubwords(String s, String t, Cursor cursor, int sLength, int tLength,
	        boolean caseSensitive, Collator collator) {
		if (collator != null) {
			// To use the collator the whole subwords have to be compared - character-by-character comparison
			// is not possible. So find the two subwords first
			int aw = cursor.sIndex;
			int bw = cursor.tIndex;
			do {
				++cursor.sIndex;
			} while (cursor.sIndex < sLength && !Character.isDigit(s.charAt(cursor.sIndex)));
			do {
				++cursor.tIndex;
			} while (cursor.tIndex < tLength && !Character.isDigit(t.charAt(cursor.tIndex)));
			
			String as = s.substring(aw, cursor.sIndex);
			String bs = t.substring(bw, cursor.tIndex);
			int subwordResult = collator.compare(as, bs);
			if (subwordResult != 0) {
				return subwordResult;
			}
		} else {
			// No collator specified. All characters should be ascii only. Compare character-by-character.
			char sChar = s.charAt(cursor.sIndex);
			char tChar = t.charAt(cursor.tIndex);
			boolean sCharIsDigit = Character.isDigit(sChar);
			boolean tCharIsDigit = Character.isDigit(tChar);
			do {
				if (sChar != tChar) {
					if (caseSensitive) {
						return sChar - tChar;
					}
					sChar = Character.toUpperCase(sChar);
					tChar = Character.toUpperCase(tChar);
					if (sChar != tChar) {
						sChar = Character.toLowerCase(sChar);
						tChar = Character.toLowerCase(tChar);
						if (sChar != tChar) {
							return sChar - tChar;
						}
					}
				}
				++cursor.sIndex;
				++cursor.tIndex;
				if (cursor.sIndex == sLength && cursor.tIndex == tLength) {
					return 0;
				}
				if (cursor.sIndex == sLength) {
					return -1;
				}
				if (cursor.tIndex == tLength) {
					return 1;
				}
				sChar = s.charAt(cursor.sIndex);
				tChar = t.charAt(cursor.tIndex);
				sCharIsDigit = Character.isDigit(sChar);
				tCharIsDigit = Character.isDigit(tChar);
			} while (!sCharIsDigit && !tCharIsDigit);
		}
		return 0;
	}
	
	/**
	 * Mutable index pair used while comparing subwords.
	 */
	private static final class Cursor {
		int sIndex;
		int tIndex;
	}
}
