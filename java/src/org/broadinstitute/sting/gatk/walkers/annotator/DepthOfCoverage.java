package org.broadinstitute.sting.gatk.walkers.annotator;

import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.contexts.StratifiedAlignmentContext;
import org.broadinstitute.sting.utils.genotype.Variation;
import org.broadinstitute.sting.utils.genotype.vcf.VCFRecord;
import org.broadinstitute.sting.utils.genotype.vcf.VCFInfoHeaderLine;

import java.util.Map;


public class DepthOfCoverage extends StandardVariantAnnotation {

    public String annotate(ReferenceContext ref, Map<String, StratifiedAlignmentContext> stratifiedContexts, Variation variation) {
        int depth = 0;
        for ( String sample : stratifiedContexts.keySet() )
            depth += stratifiedContexts.get(sample).getContext(StratifiedAlignmentContext.StratifiedContextType.COMPLETE).size();
        return String.format("%d", depth);
    }

    public String getKeyName() { return VCFRecord.DEPTH_KEY; }

    public VCFInfoHeaderLine getDescription() { return new VCFInfoHeaderLine(getKeyName(), 1, VCFInfoHeaderLine.INFO_TYPE.Integer, "Total Depth"); }
}
