package water.rapids.ast.prims.mungers;

import water.fvec.Frame;
import water.fvec.Vec;
import water.rapids.Env;
import water.rapids.vals.ValNum;
import water.rapids.ast.AstFunction;
import water.rapids.ast.Ast;

/**
 *
 */
public class AstNLevels extends AstFunction {
  @Override
  public String[] args() {
    return new String[]{"ary"};
  }

  @Override
  public int nargs() {
    return 1 + 1;
  } // (nlevels x)

  @Override
  public String str() {
    return "nlevels";
  }

  @Override
  public ValNum apply(Env env, Env.StackHelp stk, Ast asts[]) {
    int nlevels;
    Frame fr = stk.track(asts[1].exec(env)).getFrame();
    if (fr.numCols() == 1) {
      Vec v = fr.anyVec();
      nlevels = v != null && v.isCategorical() ? v.domain().length : 0;
      return new ValNum(nlevels);
    } else throw new IllegalArgumentException("nlevels applies to a single column. Got: " + fr.numCols());
  }
}
