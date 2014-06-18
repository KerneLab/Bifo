package org.kernelab.bifo.uniprot.liver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kernelab.basis.Tools;

public class LiverSwissProtItem extends LiverItem
{

	/**
	 * 
	 */
	private static final long	serialVersionUID			= 243553681063364709L;

	public static final String	LiverSwissProtItemRegex		= "^[\\s\\S]+\\|SWISS\\-PROT\\:[A-Z0-9\\-]+\\|[\\s\\S]+$";

	public static final Matcher	LiverSwissProtItemMatcher	= Pattern
																	.compile(
																			LiverSwissProtItemRegex)
																	.matcher("");

	public LiverSwissProtItem(String description)
	{
		super(description);
	}

	@Override
	protected String getId(String description)
	{
		String id = null;

		LiverSwissProtItemMatcher.reset(description);

		if (LiverSwissProtItemMatcher.matches()) {

			String s = Tools.splitCharSequence(description, "|SWISS-PROT:", 2)[1];

			id = s.split("\\|", 2)[0];
		}

		return id;
	}

}
