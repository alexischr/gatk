package org.broadinstitute.sting.gatk.walkers.qc;

import org.broadinstitute.sting.commandline.Argument;
import org.broadinstitute.sting.commandline.Output;
import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.datasources.rmd.ReferenceOrderedDataSource;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broadinstitute.sting.gatk.refdata.utils.GATKFeature;
import org.broadinstitute.sting.gatk.walkers.Reference;
import org.broadinstitute.sting.gatk.walkers.RodWalker;
import org.broadinstitute.sting.gatk.walkers.Window;
import org.broadinstitute.sting.utils.exceptions.ReviewedStingException;
import org.broadinstitute.sting.utils.variantcontext.VariantContext;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;

/**
 * a walker for validating (in the style of validating pile-up) the ROD system.
 */
@Reference(window=@Window(start=-40,stop=40))
public class RodSystemValidationWalker extends RodWalker<Integer,Integer> {

    // the divider to use in some of the text output
    private static final String DIVIDER = ",";

    @Output
    public PrintStream out;

    @Argument(fullName="PerLocusEqual",required=false,doc="Should we check that all records at the same site produce equivilent variant contexts")
    public boolean allRecordsVariantContextEquivalent = false;
    
    // used to calculate the MD5 of a file
    MessageDigest digest = null;

    // we sometimes need to know what rods the engine's seen
    List<ReferenceOrderedDataSource> rodList;

    /**
     * emit the md5 sums for each of the input ROD files (will save up a lot of time if and when the ROD files change
     * underneath us).
     */
    public void initialize() {
        // setup the MD5-er
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ReviewedStingException("Unable to find MD5 checksumer");
        }
        out.println("Header:");
        // enumerate the list of ROD's we've loaded
        rodList = this.getToolkit().getRodDataSources();
        for (ReferenceOrderedDataSource rod : rodList) {
            out.println(rod.getName() + DIVIDER + rod.getType());
            out.println(rod.getName() + DIVIDER + rod.getFile());
            out.println(rod.getName() + DIVIDER + md5sum(rod.getFile()));
        }
        out.println("Data:");
    }

    /**
     *
     * @param tracker the ref meta data tracker to get RODs
     * @param ref reference context
     * @param context the reads
     * @return an 1 for each site with a rod(s), 0 otherwise
     */
    @Override
    public Integer map(RefMetaDataTracker tracker, ReferenceContext ref, AlignmentContext context) {
        int ret = 0;
        if (tracker != null && tracker.getAllRods().size() > 0) {
            out.print(context.getLocation() + DIVIDER);
            Collection<GATKFeature> features = tracker.getAllRods();
            for (GATKFeature feat : features)
                out.print(feat.getName() + DIVIDER);
            out.println(";");
            ret++;
        }

        // if the argument was set, check for equivalence
        if (allRecordsVariantContextEquivalent && tracker != null) {
            Collection<VariantContext> col = tracker.getAllVariantContexts(ref);
            VariantContext con = null;
            for (VariantContext contextInList : col)
                if (con == null) con = contextInList;
                else if (!con.equals(col)) out.println("FAIL: context " + col + " doesn't match " + con);
        }
        return ret;
    }

    /**
     * Provide an initial value for reduce computations.
     *
     * @return Initial value of reduce.
     */
    @Override
    public Integer reduceInit() {
        return 0;
    }

    /**
     * Reduces a single map with the accumulator provided as the ReduceType.
     *
     * @param value result of the map.
     * @param sum   accumulator for the reduce.
     * @return accumulator with result of the map taken into account.
     */
    @Override
    public Integer reduce(Integer value, Integer sum) {
        return value + sum;
    }

    @Override
    public void onTraversalDone(Integer result) {
        // Double check traversal result to make count is the same.
        // TODO: Is this check necessary?
        out.println("[REDUCE RESULT] Traversal result is: " + result);
    }        

    // shamelessly absconded and adapted from http://www.javalobby.org/java/forums/t84420.html
    private String md5sum(File f) {
        InputStream is;
        try {
            is = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            return "Not a file";
        }
        byte[] buffer = new byte[8192];
        int read = 0;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            return bigInt.toString(16);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
            }
        }
    }
}
