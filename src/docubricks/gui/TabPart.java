package docubricks.gui;

import java.util.LinkedList;
import java.util.TreeMap;

import com.trolltech.qt.core.Qt.ScrollBarPolicy;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.gui.QMessageBox.StandardButton;
import com.trolltech.qt.gui.QMessageBox.StandardButtons;

import docubricks.data.MaterialUnit;
import docubricks.data.Part;
import docubricks.data.DocubricksProject;
import docubricks.gui.qt.QLineEditTODO;
import docubricks.gui.resource.ImgResource;

/**
 * 
 * Pane for one logical part
 * 
 * @author Johan Henriksson
 *
 */
public class TabPart extends QWidget
	{
	public static LinkedList<String> manufacturingMethods=new LinkedList<String>();
	
	public static void addLicense(String s)
		{
		manufacturingMethods.add(s);
		}
	
	static
		{
		manufacturingMethods.add("3D printing");
		manufacturingMethods.add("Laser cutting");
		manufacturingMethods.add("PCB/electronics");
		manufacturingMethods.add("CNC milling");
		manufacturingMethods.add("Other");
		}
		
	
	
	private DocubricksProject proj;
	public Part part;
	
	public Signal0 signalUpdated=new Signal0();
	public Signal1<TabPart> sigNameChanged=new Signal1<TabPart>();
	public Signal1<TabPart> sigRemove=new Signal1<TabPart>();
	
	private QLineEditTODO tfName=new QLineEditTODO();
	private QLineEditTODO tfSupplier=new QLineEditTODO();
	private QLineEditTODO tfSupplierPartNum=new QLineEditTODO();
	private QLineEditTODO tfManufacturerPartNum=new QLineEditTODO();
	private QLineEditTODO tfURL=new QLineEditTODO();

	private QLineEdit tfMaterialAmount=new QLineEdit();
	private QComboBox comboQuantityUnit=new QComboBox();
	
	private WidgetInstruction wInstruction;

	private QPushButton bRemovePart=new QPushButton(new QIcon(ImgResource.delete),"");
	private QHBoxLayout laybuttons=new QHBoxLayout();

	private PaneMediaSet mediapane;
	
	private TreeMap<String, MaterialUnit> mapMaterialUnitFWD=new TreeMap<String, MaterialUnit>();

	private QComboBox comboManufacturingMethod=new QComboBox();
	private QScrollArea scroll=new QScrollArea();

	private QTextEditResize tfDesc=new QTextEditResize();
	
	/**
	 * Constructor for one logical part pane
	 */
	public TabPart(DocubricksProject proj, Part part)
		{
		this.part=part;
		this.proj=proj;
		
		QVBoxLayout lay1=new QVBoxLayout();
		lay1.addWidget(scroll);
		scroll.setHorizontalScrollBarPolicy(ScrollBarPolicy.ScrollBarAlwaysOff);
		scroll.setVerticalScrollBarPolicy(ScrollBarPolicy.ScrollBarAlwaysOn);
		scroll.setWidgetResizable(true);
		lay1.setMargin(0);
		setLayout(lay1);

		
		QVBoxLayout lay=new QVBoxLayout();
		QWidget scrollwid=new QWidget();
		scrollwid.setObjectName("form");
		scrollwid.setLayout(lay);
		scroll.setWidget(scrollwid);

		
		//Set list of licenses
		comboManufacturingMethod.addItem("");
		for(String s:manufacturingMethods)
			comboManufacturingMethod.addItem(s);
		comboManufacturingMethod.setEditable(true);
		
		mapMaterialUnitFWD.put(tr("<none>"), MaterialUnit.NONE);
		mapMaterialUnitFWD.put(tr("kg"), MaterialUnit.KG);
		mapMaterialUnitFWD.put(tr("litres"), MaterialUnit.LITRES);
		for(String n:mapMaterialUnitFWD.keySet())
			comboQuantityUnit.addItem(n);
		
		
		mediapane=new PaneMediaSet(part.media);
		wInstruction=new WidgetInstruction(proj, null, part.instructions, tr("Manufacturing instructions"), false);
		
		QHBoxLayout layMaterial=new QHBoxLayout();
		layMaterial.addWidget(tfMaterialAmount);
		layMaterial.addWidget(comboQuantityUnit);

		QHBoxLayout layName=new QHBoxLayout();
		layName.addWidget(tfName);
		layName.addWidget(bRemovePart);

		QGridLayout layGrid=new QGridLayout();
		
		int row=0;
		layGrid.addWidget(new QLabel(tr("Name:")),row,0);
		layGrid.addLayout(layName,row,1);
		row++;
		layGrid.addWidget(new QLabel(tr("Manufacturing method:")),row,0);
		layGrid.addWidget(comboManufacturingMethod,row,1);
		row++;
		layGrid.addWidget(new QLabel(tr("Supplier:")),row,0);
		layGrid.addWidget(tfSupplier,row,1);
		row++;
		layGrid.addWidget(new QLabel(tr("Supplier part#:")),row,0);
		layGrid.addWidget(tfSupplierPartNum,row,1);
		row++;
		layGrid.addWidget(new QLabel(tr("Manufacturer part#:")),row,0);
		layGrid.addWidget(tfManufacturerPartNum,row,1);
		row++;
		layGrid.addWidget(new QLabel(tr("URL:")),row,0);
		layGrid.addWidget(tfURL,row,1);
		row++;
		layGrid.addWidget(new QLabel(tr("Material usage:")),row,0);
		layGrid.addLayout(layMaterial,row,1);
		row++;
		layGrid.addWidget(new QLabel(tr("Description:")),row,0);
		layGrid.addWidget(tfDesc,row,1);
		row++;
		
		layGrid.addWidget(new QLabel(tr("Design files and media:")),row,0);
		row++;
		layGrid.addWidget(mediapane,row,0,1,2);
		row++;
		layGrid.addWidget(wInstruction,row,0,1,2);
		row++;
		
		
		lay.addLayout(layGrid);
		lay.addLayout(laybuttons);
		
		loadvalues();

		tfName.textEdited.connect(this,"editvalues()");
		bRemovePart.clicked.connect(this,"actionRemovePart()");
		}
	
	/**
	 * Remove this part
	 */
	public void actionRemovePart()
		{
		StandardButton btn=QMessageBox.question(this, QtProgramInfo.programName, tr("Are you sure you want to delete this part?"), 
				new StandardButtons(StandardButton.Ok, StandardButton.Cancel));
		if(btn.equals(StandardButton.Ok))
			{
			proj.parts.remove(part);
			sigRemove.emit(this);
			}

		
		}
	
	public void loadvalues()
		{
		tfName.setText(part.name);
		comboManufacturingMethod.setEditText(part.manufacturingMethod);
		tfSupplier.setText(part.supplier);
		tfSupplierPartNum.setText(part.supplierPartNum);
		tfManufacturerPartNum.setText(part.manufacturerPartNum);
		tfURL.setText(part.url);
		tfDesc.setText(part.description);
		if(part.materialAmount!=null)
			tfMaterialAmount.setText(""+part.materialAmount);
		int i=0;
		for(String n:mapMaterialUnitFWD.keySet())
			{
			if(mapMaterialUnitFWD.get(n).equals(part.materialUnit))
				comboQuantityUnit.setCurrentIndex(i);
			i++;
			}
		}
	
	
	public void editvalues()
		{
		storevalues();
		sigNameChanged.emit(this);
		}
	
	public void storevalues()
		{
		part.name=tfName.text();
		part.supplier=tfSupplier.text();
		part.supplierPartNum=tfSupplierPartNum.text();
		part.manufacturerPartNum=tfManufacturerPartNum.text();
		part.url=tfURL.text();
		part.manufacturingMethod=comboManufacturingMethod.currentText();
		part.description=tfDesc.toPlainText();
		
		String ma=tfMaterialAmount.text();
		part.materialAmount=null;
		try
			{
			part.materialAmount=Double.parseDouble(ma);
			}
		catch (NumberFormatException e)
			{
			}
		part.materialUnit=mapMaterialUnitFWD.get(comboQuantityUnit.currentText());
		wInstruction.storevalues();
		}
		


	
	}
