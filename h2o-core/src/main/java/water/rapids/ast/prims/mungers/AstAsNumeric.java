package water.rapids.ast.prims.mungers;

import water.fvec.Frame;
import water.fvec.Vec;
import water.rapids.Env;
import water.rapids.ast.Ast;
import water.rapids.vals.ValFrame;
import water.rapids.ast.AstFunction;
import water.util.VecUtils;

/**
 * Convert to a numeric
 */
public class AstAsNumeric extends AstFunction {
  @Override
  public String[] args() {
    return new String[]{"ary"};
  }

  @Override
  public int nargs() {
    return 1 + 1;
  } // (as.numeric col)

  @Override
  public String str() {
    return "as.numeric";
  }

  @Override
  public ValFrame apply(Env env, Env.StackHelp stk, Ast asts[]) {
    Frame fr = stk.track(asts[1].exec(env)).getFrame();
    Vec[] nvecs = new Vec[fr.numCols()];
    Vec vv;
    for (int c = 0; c < nvecs.length; ++c) {
      vv = fr.vec(c);
      try {
        nvecs[c] = vv.toNumericVec();
      } catch (Exception e) {
        VecUtils.deleteVecs(nvecs, c);
        throw e;
      }
    }
    return new ValFrame(new Frame(fr._names, nvecs));
  }
}
