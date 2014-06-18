package org.kernelab.bifo.util;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class HintTextField extends JTextField
{

	public static interface HintListener
	{
		public void hint(String hint);
	}

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4505460792384978053L;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final HintTextField h = new HintTextField();

		h.setHintListener(new HintListener() {

			@Override
			public void hint(String hint)
			{
				for (int i = 0; i < hint.length(); i++) {
					h.addHint(hint);
				}
			}

		});
		f.add(h);

		f.setBounds(200, 100, 200, 100);
		f.setVisible(true);
	}

	private JPopupMenu		popup;

	private JList			list;

	private JScrollPane		listPanel;

	private HintListener	hintListener;

	public HintTextField()
	{
		super();
		this.popup = new JPopupMenu();
		this.list = new JList(new DefaultListModel());
		this.listPanel = new JScrollPane(list);
		this.listPanel.setPreferredSize(new Dimension(150, 100));
		this.popup.add(listPanel);

		this.config();
	}

	public void addHint(String hint)
	{
		this.getListModel().addElement(hint);
	}

	public void clearHint()
	{
		this.getListModel().removeAllElements();
	}

	private void config()
	{
		this.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				hint();
			}

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				hint();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				hint();
			}

		});

		this.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e)
			{
				hint();
			}

			@Override
			public void focusLost(FocusEvent e)
			{

			}

		});

		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e)
			{
				switch (e.getKeyCode())
				{
					case KeyEvent.VK_DOWN:
						hint();
						list.setSelectedIndex(0);
						list.requestFocus();
						break;

					case KeyEvent.VK_UP:
						hint();
						int last = list.getModel().getSize() - 1;
						list.setSelectedIndex(last);
						list.requestFocus();
						list.scrollRectToVisible(list.getCellBounds(last, last));
						break;

				}

			}

		});

		this.list.addKeyListener(new KeyAdapter() {

			@Override
			public void keyTyped(KeyEvent e)
			{
				if ((int) e.getKeyChar() == KeyEvent.VK_ENTER) {
					useSelectedHint();
				}
			}

		});

		this.list.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2) {
					useSelectedHint();
				}
			}

		});

	}

	public HintListener getHintListener()
	{
		return hintListener;
	}

	public JList getList()
	{
		return list;
	}

	public DefaultListModel getListModel()
	{
		return (DefaultListModel) list.getModel();
	}

	public JScrollPane getListPanel()
	{
		return listPanel;
	}

	public JPopupMenu getPopup()
	{
		return popup;
	}

	public void hideHint()
	{
		popup.setVisible(false);
	}

	public void hint()
	{
		this.clearHint();

		if (this.getText().length() == 0) {
			this.hideHint();
		} else {

			hintListener.hint(this.getText());

			if (this.getListModel().size() < 2) {
				this.hideHint();
			} else {
				popup.setPopupSize(this.getWidth(), Math.min(
						listPanel.getPreferredSize().height, this.getList().getModel()
								.getSize() * 18 + 6));
				if (!popup.isVisible()) {
					popup.show(this, 0, this.getHeight() - 1);
					this.requestFocus();
				}
			}
		}
	}

	public void setHintListener(HintListener hintListener)
	{
		this.hintListener = hintListener;
	}

	private void useSelectedHint()
	{
		this.setText(list.getSelectedValue().toString());
	}

}
