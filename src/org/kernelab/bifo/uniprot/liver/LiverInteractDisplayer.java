package org.kernelab.bifo.uniprot.liver;

import org.kernelab.bifo.interact.InteractDisplayer;

public class LiverInteractDisplayer extends InteractDisplayer
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

	public LiverInteractDisplayer()
	{
		super();
		this.setAbsoluteDegrees(false);
	}

	@Override
	protected ItemNode getNode(String id)
	{
		ItemNode node = super.getNodes().get(id);

		if (node == null) {

			LiverSwissProtItem item = Liver.QueryLiverSwissProtItem(id);

			if (item != null) {
				node = new ItemNode(item);
				super.getNodes().put(id, node);
			}
		}

		return node;
	}

	// @Override
	// protected ItemNode getNode(String id, boolean loadPeriphery)
	// {
	// ItemNode node = super.getNodes().get(id);
	//
	// if (node == null) {
	//
	// LiverSwissProtItem item = Liver.QueryLiverSwissProtItem(id);
	//
	// // if (item == null) {
	// //
	// // }
	//
	// if (item != null) {
	// node = new ItemNode(item);
	// super.getNodes().put(id, node);
	// }
	// }
	//
	// if (loadPeriphery) {
	// this.loadPeriphery(node);
	// }
	//
	// return node;
	// }

	// @Override
	// protected void loadPeriphery(ItemNode node)
	// {
	// node.setPeriphery(new HashSet<String>());
	// for (String ligand :
	// Interact.QueryInteractLigands(node.getItem().getId())) {
	// if (Liver.SwissProtItems.containsKey(ligand)) {
	// node.getPeriphery().add(ligand);
	// }
	// }
	// }

	// @Override
	// protected void makePeriphery(ItemNode node)
	// {
	// for (String id : node.getPeriphery()) {
	// ItemNode n = this.getNode(id, this.isAbsoluteDegrees());
	// if (n.getPeriphery() == null) {
	// n.setPeriphery(new HashSet<String>());
	// }
	// n.getPeriphery().add(node.getItem().getId());
	// ItemEdge e = new ItemEdge(node, n);
	// super.getEdges().put(e.toString(), e);
	// }
	// }

}
