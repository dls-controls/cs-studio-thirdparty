package org.csstudio.trends.databrowser.archiveview;

import org.csstudio.platform.data.ITimestamp;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/** A table label provider for NameTableItem-type data
 *  @author Kay Kasemir
 */
public class NameTableItemLabelProvider extends LabelProvider
                                        implements ITableLabelProvider
{
    public static final int NAME = 0;
    public static final int ARCHIVE = 1;
    public static final int START = 2;
    public static final int END = 3;

    /** No column images ... */
    public Image getColumnImage(Object element, int column)
    {
        return null;
    }

    /** @return Returns the name, key, .... */
    public String getColumnText(final Object element, final int column)
    {
        final NameTableItem item = (NameTableItem) element;
        switch (column)
        {
        case NAME:
            return item.getName();
        case ARCHIVE:
            return item.getArchiveName();
        case START:
            return getTimeInfo(item.getStart());
        case END:
            return getTimeInfo(item.getEnd());
        }
        return null;
    }

    /** @return String for time, also handling <code>null</code> time */
    private String getTimeInfo(final ITimestamp time)
    {
        if (time == null)
            return ""; //$NON-NLS-1$
        return time.format(ITimestamp.Format.DateTimeSeconds);
    }
}
