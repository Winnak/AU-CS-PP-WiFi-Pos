package org.pi4.locutil.trace.macfilter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.pi4.locutil.MACAddress;

//import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * MacFilterExclude excludes given MAC addresses from a set of MAC addresses.
 * This class is supposed to be used for the online as well as the offline set.
 * 
 * @author king
 */
public class MacFilterExclude implements MacFilter
{
    HashSet<MACAddress> excludedMacs;

    public MacFilterExclude()
    {
        excludedMacs = new HashSet<MACAddress>();
    }

    public void add(MACAddress mac)
    {
        excludedMacs.add(mac);
    }

    public boolean contains(MACAddress mac)
    {
        return !excludedMacs.contains(mac);
    }

    public boolean isEmpty()
    {
        return excludedMacs.isEmpty();
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("MacFilterExclude: ");
        if (isEmpty())
        {
            sb.append("Empty");
        } else
        {
            Iterator<MACAddress> it = excludedMacs.iterator();
            while (it.hasNext())
            {
                sb.append(it.next());
                if (it.hasNext())
                    sb.append(", ");
            }
        }
        return sb.toString();
    }

    @Override
    public Set<MACAddress> getMacs()
    {
        return null;
        // throw new NotImplementedException();
    }
}