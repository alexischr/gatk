/*
 * Copyright (c) 2010 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.broadinstitute.sting.gatk.filters;

import net.sf.samtools.SAMRecord;
import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.utils.sam.ReadUtils;

/**
 * Filter out PL matching reads.
 *
 * @author ebanks
 * @version 0.1
 */
public class PlatformFilter extends ReadFilter {
    @Argument(fullName = "PLFilterName", shortName = "PLFilterName", doc="Discard reads with RG:PL attribute containing this strign", required=false)
    protected String[] PLFilterNames;

    public boolean filterOut(SAMRecord rec) {
        for ( String name : PLFilterNames )
            if ( ReadUtils.isPlatformRead(rec, name.toUpperCase() ))
                return true;
        return false;
    }
}
