package org.csstudio.utility.adlparser.fileParser.widgets;

import java.util.ArrayList;

import org.csstudio.utility.adlparser.Activator;
import org.csstudio.utility.adlparser.IImageKeys;
import org.csstudio.utility.adlparser.fileParser.ADLResource;
import org.csstudio.utility.adlparser.fileParser.ADLWidget;
import org.csstudio.utility.adlparser.fileParser.FileLine;
import org.csstudio.utility.adlparser.fileParser.WrongADLFormatException;
import org.csstudio.utility.adlparser.fileParser.widgetParts.ADLLimits;
import org.csstudio.utility.adlparser.fileParser.widgetParts.ADLMonitor;
import org.csstudio.utility.adlparser.fileParser.widgetParts.ADLObject;
import org.csstudio.utility.adlparser.internationalization.Messages;

/**
 * 
 * @author hammonds
 *
 */
public class ShellCommand extends ADLAbstractWidget {
	private int _clr;
	private int _bclr;
    private boolean _isBackColorDefined;
    private boolean _isForeColorDefined;
	private String label = new String();

	public ShellCommand(ADLWidget adlWidget) {
		super(adlWidget);
		name = new String("shell command");
		descriptor = Activator.getImageDescriptor(IImageKeys.ADL_SHELL_COMMAND);
		try {
			for (ADLWidget childWidget : adlWidget.getObjects()) {
	        	if (childWidget.getType().equals("object")){
	        		_adlObject = new ADLObject(childWidget);
	        		if (_adlObject != null){
	        			_hasObject = true;
	        		}
	        		
	        	}
	        }
			for (FileLine fileLine : adlWidget.getBody()){
				String bodyPart = fileLine.getLine();
				String[] row = bodyPart.trim().split("=");
				if (row.length < 2){
					throw new WrongADLFormatException(Messages.Label_WrongADLFormatException_Parameter_Begin + bodyPart + Messages.Label_WrongADLFormatException_Parameter_End);
				}
	            if(FileLine.argEquals(row[0], "clr")){ //$NON-NLS-1$
	                set_clr(FileLine.getIntValue(row[1]));
	        		set_isForeColorDefined(true);
	            }else if(FileLine.argEquals(row[0], "bclr")){ //$NON-NLS-1$
	                set_bclr(FileLine.getIntValue(row[1]));
	        		set_isBackColorDefined(true);
	            }else if(FileLine.argEquals(row[0], "label")){ //$NON-NLS-1$
	                setLabel(FileLine.getTrimmedValue(row[1]));
	            }
            }
		}
		catch (WrongADLFormatException ex) {
			
		}
	}

	/**
	 * @param label the label to set
	 */
	private void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	@Override
	public Object[] getChildren() {
		ArrayList<Object> ret = new ArrayList<Object>();
		return ret.toArray();
	}

	/**
	 * @param _clr the _clr to set
	 */
	public void set_clr(int _clr) {
		this._clr = _clr;
	}

	/**
	 * @return the _clr
	 */
	public int get_clr() {
		return _clr;
	}

	/**
	 * @param _bclr the _bclr to set
	 */
	public void set_bclr(int _bclr) {
		this._bclr = _bclr;
	}

	/**
	 * @return the _bclr
	 */
	public int get_bclr() {
		return _bclr;
	}

	/**
	 * @param _isBackColorDefined the _isBackColorDefined to set
	 */
	public void set_isBackColorDefined(boolean _isBackColorDefined) {
		this._isBackColorDefined = _isBackColorDefined;
	}

	/**
	 * @return the _isBackColorDefined
	 */
	public boolean is_isBackColorDefined() {
		return _isBackColorDefined;
	}

	/**
	 * @param _isForeColorDefined the _isForeColorDefined to set
	 */
	public void set_isForeColorDefined(boolean _isForeColorDefined) {
		this._isForeColorDefined = _isForeColorDefined;
	}

	/**
	 * @return the _isForeColorDefined
	 */
	public boolean is_isForeColorDefined() {
		return _isForeColorDefined;
	}
}
