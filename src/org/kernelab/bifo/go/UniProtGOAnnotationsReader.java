package org.kernelab.bifo.go;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.kernelab.basis.Tools;
import org.kernelab.basis.io.DataReader;
import org.kernelab.bifo.fasta.FastA;
import org.kernelab.bifo.util.Progressive;

/**
 * To read the description line of a go fasta item. Usually likes:<br />
 * >RGD|1565249 - symbol:Cd96 "CD96 molecule" species:10116 "Rattus norvegicus"
 * [GO:0005515 "protein binding" evidence=IEA] [GO:0005886 "plasma membrane"
 * evidence=ISO] [GO:0007155 "cell adhesion" evidence=IEA] [GO:0016020
 * "membrane" evidence=IEA] [GO:0016021 "integral to membrane" evidence=IEA]
 * RGD:1565249 GO:GO:0005515 GO:GO:0016021 InterPro:IPR007110 InterPro:IPR013783
 * InterPro:IPR003599 Gene3D:G3DSA:2.60.40.10 SMART:SM00409 PROSITE:PS50835
 * GO:GO:0007155 InterPro:IPR013162 Pfam:PF08205 EMBL:BC091206 IPI:IPI00388868
 * RefSeq:NP_001020203.1 UniGene:Rn.55482 SMR:Q5BK49 STRING:Q5BK49
 * Ensembl:ENSRNOT00000035485 GeneID:498079 KEGG:rno:498079 CTD:498079
 * eggNOG:roNOG09921 HOVERGEN:HBG004030 InParanoid:Q5BK49 NextBio:698607
 * ArrayExpress:Q5BK49 Genevestigator:Q5BK49 Uniprot:Q5BK49
 * 
 * @author Dilly King
 * 
 */
public class UniProtGOAnnotationsReader extends DataReader
{

	public static boolean	Read					= false;

	public static String	UniProtPattern			= "[\\s\\S]+Uniprot\\:[A-Z0-9]{6}$";

	public static char		GOAnnotationStartChar	= '[';

	public static String	GOAnnotationStartMark	= "[";

	public static char		GOAnnotationEndChar		= ']';

	public static String	GOAnnotationEndMark		= "]";

	public static String	GOAnnotationPattern		= "^\\"
															+ GOAnnotationStartMark
															+ "GO\\:[0-9]{7}\\s\\\"[\\s\\S]+\\\"\\sevidence\\=([A-Z]{2,}\\;*)+\\"
															+ GOAnnotationEndMark + "$";

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		UniProtGOAnnotationsReader reader = new UniProtGOAnnotationsReader();
		try
		{
			reader.setDataFile(new File("./dat/go_20100424-seqdb.fasta"));
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		reader.read();
		Tools.debug(GeneOntology.UniProtGOAnnotations.get("Q59MQ7"));
	}

	private Progressive	progress;

	@Override
	protected void readFinished()
	{
		Read = true;
		if (progress != null)
		{
			progress.resetProgress(0);
		}
	}

	@Override
	protected void readLine(CharSequence line)
	{
		if (Pattern.matches(FastA.FASTA_ITEM_BEGIN_REGEX, line))
		{

			if (Pattern.matches(UniProtPattern, line))
			{

				String uniProtId = line.subSequence(Tools.seekLastIndex(line, ':') + 1, line.length()).toString();

				Set<String> goAnnotations = GeneOntology.UniProtGOAnnotations.get(uniProtId);

				if (goAnnotations == null)
				{
					goAnnotations = new HashSet<String>();
					GeneOntology.UniProtGOAnnotations.put(uniProtId, goAnnotations);
				}

				int begin = -1;
				int end = 0;

				do
				{
					begin = Tools.seekIndex(line, GOAnnotationStartChar, end + 1);
					if (begin != -1)
					{
						end = Tools.dualMatchIndex(line, GOAnnotationStartChar, GOAnnotationEndChar, begin);
						if (end != -1)
						{
							end++;
							String annotation = line.subSequence(begin, end).toString();
							if (annotation.matches(GOAnnotationPattern))
							{
								String goId = annotation.substring(4, 11);
								goAnnotations.add(goId);
							}
							else
							{
								end = begin;
							}
						}
					}
				} while (begin != -1);

			}
		}
	}

	@Override
	protected void readPrepare()
	{
		if (progress != null)
		{
			progress.prepareProgress();
		}
		Read = false;
		GeneOntology.UniProtGOAnnotations.clear();
	}

	public void setProgress(Progressive progress)
	{
		this.progress = progress;
	}

}
