
/* 
 * Copyright (c) 2011 Stiftung Deutsches Elektronen-Synchrotron, 
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS. 
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED 
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND 
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE 
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR 
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE. 
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, 
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION, 
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS 
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY 
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 *
 */

package org.csstudio.archive.sdds.server.conversion.handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.csstudio.archive.sdds.server.Activator;
import org.csstudio.archive.sdds.server.command.header.DataRequestHeader;
import org.csstudio.archive.sdds.server.data.EpicsRecordData;
import org.csstudio.archive.sdds.server.internal.ServerPreferenceKey;
import org.csstudio.archive.sdds.server.sdds.SDDSType;
import org.csstudio.archive.sdds.server.util.DataException;
import org.csstudio.platform.logging.CentralLogger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

/**
 * TODO (mmoeller) : 
 * 
 * @author mmoeller
 * @version 
 * @since 01.03.2011
 */
public class AverageHandler extends AlgorithmHandler {
    
    /** The logger for this class */
    private Logger logger;
    
    /** Max. allowed difference of the last allowed record (in seconds)*/ 
    @SuppressWarnings("unused")
	private long validRecordBeforeTime;

    /**
     * @param maxSamples
     */
    public AverageHandler(int maxSamples) {
        
        super(maxSamples);
        
        logger = CentralLogger.getInstance().getLogger(this);
        
        IPreferencesService pref = Platform.getPreferencesService();
        validRecordBeforeTime = pref.getLong(Activator.PLUGIN_ID, ServerPreferenceKey.P_VALID_RECORD_BEFORE, 3600, null);
        
        logger.info(this.getClass().getSimpleName() + " created. Max. samples per request: " + maxSamples);
    }
    
    /* (non-Javadoc)
     * @see org.csstudio.archive.sdds.server.conversion.handler.AlgorithmHandler#handle(org.csstudio.archive.sdds.server.command.header.DataRequestHeader, org.csstudio.archive.sdds.server.data.EpicsRecordData[])
     */
    @Override
    public Iterable<EpicsRecordData> handle(DataRequestHeader header, EpicsRecordData[] data)
    throws DataException, AlgorithmHandlerException, MethodNotImplementedException {
        
        if (data == null) {
            return new ArrayList<EpicsRecordData>(0);
        } else if (data.length == 0){
            return new ArrayList<EpicsRecordData>(0);
        }
        
        // Get the number of requested samples
        int resultLength = header.getMaxNumOfSamples();
        
        // More then max. allowed number of samples?
        if(resultLength > this.getMaxSamplesPerRequest()) {
            resultLength = this.getMaxSamplesPerRequest();
        }
        
        long intervalStart = header.getFromSec();
        long intervalEnd = header.getToSec();

        // Get the current time...
        GregorianCalendar cal = new GregorianCalendar();
        
        // ...and substract 2 months
        cal.add(Calendar.MONTH, -3);
        if ((intervalEnd * 1000L) > cal.getTimeInMillis()) {
        	intervalEnd = (cal.getTimeInMillis() / 1000L);
        }
        
        long deltaTime = (intervalEnd - intervalStart) / (long) resultLength;
        if(deltaTime == 0) {
            
            // Requested region very short --> only 1 point per sec
            deltaTime = 1;
            header.setMaxNumOfSamples((int)(intervalEnd - intervalStart));
        }

        // Get the first data sample with the valid time stamp within the request time interval
        int index = 0;
        float avg = Float.NaN;
        
        for(EpicsRecordData o : data) {
            
            if(o.getTime() >= intervalStart) {
                break;
            } else {
                if(o.isValueValid()) {
                    avg = ((Float)o.getValue()).floatValue();
                }
            }
            
            index++;
        }
        
        if (index >= data.length) {
            return new ArrayList<EpicsRecordData>(0);
        }
        
        // The variable index now contains the index of the first data sample
        // in the requested time interval
        
        
        List<EpicsRecordData> resultData = new ArrayList<EpicsRecordData>(header.getMaxNumOfSamples());
        
        long nextIntervalStep = 0;
        long sampleTimestamp = 0;
        float sum = 0.0f;
        float count = 0.0f;
        
        long curTime = intervalStart;
        boolean foundSample;
        
        // Iterate through the complete time interval
        do {

            // Beginn of the next subinterval
            nextIntervalStep = curTime + deltaTime;

            EpicsRecordData curData = null;
            sum = 0.0f;
            count = 0.0f;
            foundSample = false;
            
            // Iterate over data samples in the time subinterval
            do {
                
                curData = data[index];
                sampleTimestamp = curData.getTime();
                if ((sampleTimestamp >= curTime) && (sampleTimestamp < nextIntervalStep)) {

                    if(curData.isValueValid()) {
                        sum += (Float) curData.getValue();
                        count += 1.0f;
                        foundSample = true;
                    }
                    
                    // Increment the index only if we are not at the end of the array
                    if(index < (data.length - 1)) {
                        index++;
                    } else {
                    	// Leave the loop if we reached the end of the sample array
                    	break;
                    }
                } else {
                	break;
                }
                
            } while (sampleTimestamp < nextIntervalStep);
            
            // We have a sum of sample values from the subinterval
            // Otherwise keep the current average value
            if (foundSample) {
            	// Calculate the average value
            	avg = sum / count;
            }
            
            if (Float.isNaN(avg) == false) {
				EpicsRecordData newData = new EpicsRecordData(curTime, 0L, 0L, new Double(String.valueOf(avg)), SDDSType.SDDS_DOUBLE);
				resultData.add(newData);
				logger.debug(newData.toString());
				newData = null;
            }
            
            curTime += deltaTime;
            
        } while (curTime < intervalEnd);
        
        return resultData;
    }
}
