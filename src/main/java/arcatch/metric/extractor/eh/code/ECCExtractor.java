package arcatch.metric.extractor.eh.code;

import arcatch.metric.Measure;
import arcatch.metric.Metric;
import arcatch.model.Model;
import arcatch.util.Util;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtBreak;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtContinue;
import spoon.reflect.code.CtDo;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtSwitch;
import spoon.reflect.code.CtThrow;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

public class ECCExtractor extends AbstractProcessor<CtClass<?>> {

	@Override
	public void process(CtClass<?> element) {
		if (Util.isValid(element)) {
			String qualifiedName = element.getQualifiedName();

			double exceptinalCyclomaticComplexity = 0.0;

			for (CtMethod<?> method : element.getMethods()) {
				if (method.getThrownTypes().size() > 0)
					exceptinalCyclomaticComplexity++;
			}

			exceptinalCyclomaticComplexity += element.getElements(new TypeFilter<CtThrow>(CtThrow.class)).size();

			for (CtTry tryBlock : element.getElements(new TypeFilter<CtTry>(CtTry.class))) {
				exceptinalCyclomaticComplexity += tryBlockCyclomaticComplexity(tryBlock);
			}

			for (CtCatch catchBlock : element.getElements(new TypeFilter<CtCatch>(CtCatch.class))) {
				exceptinalCyclomaticComplexity += catchBlockCyclomaticComplexity(catchBlock);
			}

			for (CtTry tryBlock : element.getElements(new TypeFilter<CtTry>(CtTry.class))) {
				if (tryBlock.getFinalizer() != null) {
					exceptinalCyclomaticComplexity += finallyBlockCyclomaticComplexity(tryBlock.getFinalizer());
				}
			}

			Model.addMeasure(qualifiedName, new Measure(Metric.ECC, exceptinalCyclomaticComplexity));
		}
	}

	private double tryBlockCyclomaticComplexity(CtTry block) {
		return 1 + cyclomaticComplexity(block.getBody());
	}

	private double catchBlockCyclomaticComplexity(CtCatch block) {
		return 1 + cyclomaticComplexity(block.getBody());
	}

	private double finallyBlockCyclomaticComplexity(CtBlock<?> block) {
		return 1 + cyclomaticComplexity(block);
	}

	private double cyclomaticComplexity(CtBlock<?> block) {

		double cyclomaticComplexity = block.getElements(new TypeFilter<CtIf>(CtIf.class)).size();

		for (CtIf ifstmt : block.getElements(new TypeFilter<CtIf>(CtIf.class))) {
			if (ifstmt.getElseStatement() != null) {
				cyclomaticComplexity++;
			}
		}

		cyclomaticComplexity += block.getElements(new TypeFilter<CtSwitch<?>>(CtSwitch.class)).size();

		cyclomaticComplexity += block.getElements(new TypeFilter<CtCase<?>>(CtCase.class)).size();

		cyclomaticComplexity += block.getElements(new TypeFilter<CtFor>(CtFor.class)).size();

		cyclomaticComplexity += block.getElements(new TypeFilter<CtForEach>(CtForEach.class)).size();

		cyclomaticComplexity += block.getElements(new TypeFilter<CtWhile>(CtWhile.class)).size();

		cyclomaticComplexity += block.getElements(new TypeFilter<CtDo>(CtDo.class)).size();

		cyclomaticComplexity += block.getElements(new TypeFilter<CtBreak>(CtBreak.class)).size();

		cyclomaticComplexity += block.getElements(new TypeFilter<CtContinue>(CtContinue.class)).size();

		for (CtBinaryOperator<?> operator : block
				.getElements(new TypeFilter<CtBinaryOperator<?>>(CtBinaryOperator.class))) {

			if (operator.getKind() == BinaryOperatorKind.AND || operator.getKind() == BinaryOperatorKind.OR) {
				cyclomaticComplexity++;
			}
		}
		cyclomaticComplexity += block.getElements(new TypeFilter<CtReturn<?>>(CtReturn.class)).size();
		return cyclomaticComplexity;
	}
}