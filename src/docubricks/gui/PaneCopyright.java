package docubricks.gui;

import java.util.ArrayList;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;

import docubricks.data.Author;
import docubricks.data.Unit;
import docubricks.data.DocubricksProject;

/**
 * 
 * Pane for one logical part
 * 
 * @author Johan Henriksson
 *
 */
public class PaneCopyright extends QVBoxLayout
	{
	private DocubricksProject proj;
	private Unit unit;
	
	private ArrayList<ComboAuthorRef> combosAuthor=new ArrayList<ComboAuthorRef>();
	private QVBoxLayout layAuthors=new QVBoxLayout();
	private QComboBox tfLicense=new QComboBox();
	private QPushButton bAddAuthor=new QPushButton(tr("Add author"));

	
	/**
	 * Constructor for list of logical parts
	 */
	public PaneCopyright(DocubricksProject proj, Unit unit)
		{
		this.proj=proj;
		this.unit=unit;

		//Set list of licenses
		tfLicense.addItem("");
		for(String s:LicensesUtil.licenses)
			tfLicense.addItem(s);
		tfLicense.setEditable(true);

		QGridLayout layGrid=new QGridLayout();

		layGrid.setMargin(0);
		setMargin(0);
		
		loadvalues();
		
		int row=0;
		layGrid.addWidget(new HeaderLabel(tr("Copyright")),row,0,1,2);
		row++;
		layGrid.addWidget(new QLabel(tr("License:")),row,0);
		layGrid.addWidget(tfLicense,row,1);
		row++;
		layGrid.addLayout(layAuthors,row,0,1,2);
		row++;
		
		bAddAuthor.clicked.connect(this,"actionAddAuthor()");
		
		addLayout(layGrid);
		addWidget(bAddAuthor);
		}
	

	/**
	 * Update the available entries in all combos
	 */
	public void updateAllCombos()
		{
		for(ComboAuthorRef p:combosAuthor)
			p.updateListOfEntries();
		}

	
	/**
	 * Load values from object
	 */
	public void loadvalues()
		{
		tfLicense.setEditText(unit.getLicense());   //TODO not working
		for(Author p:unit.authors)
			addAuthorWidget(p);
		}
	
	
	/**
	 * Add widget for author
	 */
	private void addAuthorWidget(Author a)
		{
		ComboAuthorRef combo=new ComboAuthorRef(proj, a){
			@SuppressWarnings("unused")
			public void cbDeleted(ComboAuthorRef a)
				{
				int index=combosAuthor.indexOf(this);
				unit.authors.remove(index);
				layAuthors.removeWidget(this);
				}
			@SuppressWarnings("unused")
			public void cbUpdated(ComboAuthorRef a)
				{
				int index=combosAuthor.indexOf(this);
				unit.authors.set(index,a.getCurrentAuthor());
				}
		};
		combo.sigDeleted.connect(combo,"cbDeleted(ComboAuthorRef)");
		combo.sigUpdated.connect(combo,"cbUpdated(ComboAuthorRef)");
		combosAuthor.add(combo);
		layAuthors.addWidget(combo);
		}

	
	/**
	 * Action: add author
	 */
	public void actionAddAuthor()
		{
		Author a=null;
		unit.authors.add(a);
		addAuthorWidget(a);
		}


	/**
	 * Store entries to model
	 */
	public void storevalues()
		{
		unit.setLicense(tfLicense.currentText());
		}
	
	

	}