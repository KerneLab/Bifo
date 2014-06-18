package org.kernelab.bifo.fasta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface FastA
{
	public static final int		RECOMMENDED_LINE_LENGTH		= 80;

	public static final char	FASTA_ITEM_BEGIN_CHAR		= '>';

	public static final String	FASTA_ITEM_BEGIN_MARK		= ">";

	public static final String	FASTA_ITEM_BEGIN_REGEX		= "^" + FASTA_ITEM_BEGIN_MARK
																	+ "[\\d\\D]+?";

	public static final Pattern	FASTA_ITEM_BEGIN_PATTERN	= Pattern
																	.compile(FASTA_ITEM_BEGIN_REGEX);

	public static final Matcher	FASTA_ITEM_BEGIN_MATCHER	= FASTA_ITEM_BEGIN_PATTERN
																	.matcher("");

	public CharSequence getDescription();

	public CharSequence getSequenceData();
}
